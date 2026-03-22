/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.util.editor;


import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts values between Java types using several strategies, tried in order:
 *
 * <h3>String to Java type</h3>
 * <ol>
 *   <li><strong>PropertyEditor</strong> — uses a registered {@link java.beans.PropertyEditor}
 *       for the target type, if one exists. Custom editors can be registered via
 *       {@link java.beans.PropertyEditorManager#registerEditor}.</li>
 *   <li><strong>Enum</strong> — matches by {@link Enum#valueOf}, falling back to
 *       uppercase then lowercase if the exact case does not match.</li>
 *   <li><strong>Constructor</strong> — a public constructor accepting a single
 *       {@code String} or {@code CharSequence} parameter. {@code String} is preferred
 *       over {@code CharSequence} when both exist.</li>
 *   <li><strong>Static factory method</strong> — a public static method accepting a
 *       single {@code String} or {@code CharSequence} and returning the target type.
 *       {@code String} is preferred over {@code CharSequence}; methods are then
 *       sorted alphabetically by name.</li>
 * </ol>
 *
 * <h3>Java type to String</h3>
 * <ol>
 *   <li><strong>PropertyEditor</strong> — uses {@link java.beans.PropertyEditor#getAsText()}
 *       if an editor is registered for the type.</li>
 *   <li><strong>Enum</strong> — uses {@link Enum#name()} rather than {@code toString()}
 *       to ensure round-trip fidelity with {@code Enum.valueOf}.</li>
 *   <li><strong>{@code toString()}</strong> — falls back to {@link Object#toString()}.
 *       Types that convert from String via a constructor should ensure their
 *       {@code toString()} produces a value that constructor can accept.</li>
 * </ol>
 *
 * <h3>Number to Number</h3>
 * Numeric values are converted between {@code Byte}, {@code Short}, {@code Integer},
 * {@code Long}, {@code Float}, and {@code Double} using the corresponding
 * {@link Number} methods (e.g. {@link Number#intValue()}).
 *
 * <h3>Parameterized Collections and Maps</h3>
 * The {@link #convertString} method supports parameterized types:
 * <ul>
 *   <li><strong>Collection/List/Set</strong> — splits comma-separated values and
 *       converts each element to the specified generic type.</li>
 *   <li><strong>Map</strong> — parses {@code key=value} lines (properties format)
 *       and converts keys and values independently.</li>
 * </ul>
 *
 * <h3>Error handling</h3>
 * All conversion failures throw {@link IllegalArgumentException} with a message
 * that includes the source value, target type, and the {@code name} parameter
 * when provided. The {@code name} serves as diagnostic context — for example,
 * a field name, column header, or parameter name — so that errors can be traced
 * back to the specific value that failed.
 */
public class Converter {

    private Converter() {
        // no-op
    }

    /**
     * Converts a comma-separated or properties-formatted string into a parameterized
     * Collection or Map type.
     *
     * <p>For collections, values are split on commas and each element is converted
     * to the collection's generic type. For maps, the string is parsed as
     * {@code key=value} lines and both keys and values are converted.
     *
     * @param value      the string to convert
     * @param targetType the parameterized target type (e.g. {@code List<Integer>},
     *                   {@code Map<String, Duration>})
     * @param name       diagnostic context included in error messages, such as a
     *                   field or parameter name; may be {@code null}
     * @return the converted collection or map
     * @throws IllegalArgumentException if the type is not supported or conversion fails
     */
    public static Object convertString(final String value, final Type targetType, final String name) {
        if (Class.class.isInstance(targetType)) {
            return convert(value, Class.class.cast(targetType), name);
        }
        if (ParameterizedType.class.isInstance(targetType)) {
            final ParameterizedType parameterizedType = ParameterizedType.class.cast(targetType);
            final Type raw = parameterizedType.getRawType();
            if (!Class.class.isInstance(raw)) {
                throw new IllegalArgumentException("not supported parameterized type: " + targetType);
            }

            final Class<?> rawClass = Class.class.cast(raw);
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (Collection.class.isAssignableFrom(rawClass)) {
                final Class<?> argType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[0]);
                final String[] split = value.split(" *, *");

                final Collection values;
                if (Collection.class == raw || List.class == raw || ArrayList.class == raw) {
                    values = new ArrayList(split.length);
                } else if (SortedSet.class == raw || TreeSet.class == raw) {
                    values = new TreeSet();
                } else if (Set.class == raw || HashSet.class == raw) {
                    values = new HashSet(split.length);
                } else {
                    throw new IllegalArgumentException(targetType + " collection type not supported");
                }

                for (final String val : split) {
                    values.add(convert(val, argType, name));
                }

                return values;
            } else if (Map.class.isAssignableFrom(rawClass)) {
                final Map map;
                if (SortedMap.class == raw || TreeMap.class == raw) {
                    map = new TreeMap();
                } else {
                    map = new HashMap();
                }
                final Properties p = new Properties();
                try {
                    p.load(new ByteArrayInputStream(value.getBytes()));
                } catch (final IOException e) {
                    // can't occur
                }
                final Class<?> keyType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[0]);
                final Class<?> valueType = actualTypeArguments.length == 0 ? String.class : toClass(actualTypeArguments[1]);
                for (final String k : p.stringPropertyNames()) {
                    map.put(convert(k, keyType, name), convert(p.getProperty(k), valueType, name));
                }
                return map;
            }
        }
        throw new IllegalArgumentException("not supported type: " + targetType);
    }

    private static Class<?> toClass(final Type type) {
        try {
            return Class.class.cast(type);
        } catch (final Exception e) {
            throw new IllegalArgumentException(type + " not supported");
        }
    }

    /**
     * Converts a value to its String representation.
     *
     * <p>Equivalent to {@code convert(value, String.class, null)}. When diagnostic
     * context is available, prefer {@link #convert(Object, Class, String)} with
     * {@code String.class} as the target type.
     *
     * @param value the object to convert; may be {@code null}
     * @return the string representation, or {@code null} if value is {@code null}
     * @throws IllegalArgumentException if the conversion fails
     */
    public static String convert(final Object value) {
        return (String) convert(value, String.class, null);
    }

    /**
     * Converts a value to the specified target type.
     *
     * <p>Equivalent to {@code convert(value, targetType, null)}. When diagnostic
     * context is available, prefer {@link #convert(Object, Class, String)}.
     *
     * @param value      the value to convert; may be {@code null}
     * @param targetType the desired target type
     * @return the converted value, or {@code null} if value is {@code null}
     * @throws IllegalArgumentException if the conversion fails
     */
    public static Object convert(final Object value, final Class<?> targetType) {
        return convert(value, targetType, null);
    }

    /**
     * Converts a value to the specified target type.
     *
     * <p>This is the primary conversion method. All other {@code convert} overloads
     * delegate to this method.
     *
     * @param value      the value to convert; may be {@code null}
     * @param targetType the desired target type
     * @param name       diagnostic context included in error messages, such as a
     *                   field name, column header, or parameter name; may be {@code null}
     * @return the converted value, or {@code null} if value is {@code null}
     *         (except for {@code boolean}/{@code Boolean}, which returns {@code false})
     * @throws IllegalArgumentException if the conversion fails
     */
    public static Object convert(final Object value, Class<?> targetType, final String name) {
        if (value == null) {
            if (targetType.equals(Boolean.TYPE)) return false;
            return null;
        }

        final Class<?> actualType = value.getClass();

        if (targetType.isPrimitive()) targetType = boxPrimitive(targetType);

        if (targetType.isAssignableFrom(actualType)) return value;

        if (targetType == String.class) return convertToString(value, name);

        if (Number.class.isAssignableFrom(actualType) && Number.class.isAssignableFrom(targetType)) {
            final Number number = (Number) value;
            if (targetType == Byte.class) return number.byteValue();
            if (targetType == Short.class) return number.shortValue();
            if (targetType == Integer.class) return number.intValue();
            if (targetType == Long.class) return number.longValue();
            if (targetType == Float.class) return number.floatValue();
            if (targetType == Double.class) return number.doubleValue();
            return value;
        }

        if (!(value instanceof String)) {
            final String message = String.format("Expected type '%s' for '%s'. Found '%s'", targetType.getName(), name, actualType.getName());
            throw new IllegalArgumentException(message);
        }

        final String stringValue = (String) value;

        try {
            // Force static initializers to run
            Class.forName(targetType.getName(), true, targetType.getClassLoader());
        } catch (final ClassNotFoundException e) {
            // no-op
        }

        final PropertyEditor editor = Editors.get(targetType);

        if (editor == null) {
            final Object result = create(targetType, stringValue);

            if (result != null) return result;
        }

        if (editor == null) {
            final String message = String.format("Cannot convert to '%s' for '%s'. No PropertyEditor", targetType.getName(), name);
            throw new IllegalArgumentException(message);
        }

        editor.setAsText(stringValue);
        return editor.getValue();
    }

    private static Object create(final Class<?> type, final String value) {

        if (Enum.class.isAssignableFrom(type)) {
            final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            try {
                return Enum.valueOf(enumType, value);
            } catch (final IllegalArgumentException e) {
                try {
                    return Enum.valueOf(enumType, value.toUpperCase());
                } catch (final IllegalArgumentException e1) {
                    return Enum.valueOf(enumType, value.toLowerCase());
                }
            }
        }

        final Function<Executable, Integer> types = method -> {
            final Class<?> arg = method.getParameterTypes()[0];
            if (String.class.equals(arg)) return 0;
            if (CharSequence.class.equals(arg)) return 1;
            return 2;
        };


        final List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                .filter(constructor -> constructor.getParameterTypes().length == 1)
                .filter(constructor -> isStringAssignable(constructor.getParameterTypes()[0]))
                .sorted(Comparator.comparing(types))
                .collect(Collectors.toList());

        if (constructors.size() > 0) {
            try {
                final Constructor<?> constructor = constructors.get(0);
                return constructor.newInstance(value);
            } catch (InvocationTargetException e) {
                final Throwable cause = e.getCause();
                final String message = String.format("Cannot convert string '%s' to %s. Cause: %s", value, type, cause.getMessage());
                throw new IllegalArgumentException(message, cause);
            } catch (Exception e) {
                final String message = String.format("Cannot convert string '%s' to %s. Cause: %s", value, type, e.getMessage());
                throw new IllegalArgumentException(message, e);
            }
        }

        final List<Method> methods = Stream.of(type.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterTypes().length == 1)
                .filter(method -> method.getReturnType().equals(type))
                .filter(method -> isStringAssignable(method.getParameterTypes()[0]))
                .sorted(Comparator.comparing(Method::getName))
                .sorted(Comparator.comparing(types))
                .collect(Collectors.toList());

        if (methods.size() > 0) {
            final Method method = methods.get(0);
            try {
                return method.invoke(null, value);
            } catch (final Exception e) {
                final String message = String.format("Cannot convert string '%s' to %s.", value, type);
                throw new IllegalStateException(message, e);
            }
        }

        return null;
    }

    private static String convertToString(final Object value, final String name) {
        final Class<?> type = value.getClass();

        try {
            final PropertyEditor editor = Editors.get(type);
            if (editor != null) {
                editor.setValue(value);
                return editor.getAsText();
            }

            if (value instanceof Enum) {
                return ((Enum<?>) value).name();
            }

            return value.toString();
        } catch (final Exception e) {
            final String message = name != null
                    ? String.format("Cannot convert %s to String for '%s'. Cause: %s", type.getName(), name, e.getMessage())
                    : String.format("Cannot convert %s to String. Cause: %s", type.getName(), e.getMessage());
            throw new IllegalArgumentException(message, e);
        }
    }

    private static boolean isStringAssignable(final Class<?> t) {
        if (String.class.equals(t)) return true;
        if (CharSequence.class.equals(t)) return true;
        return false;
    }

    private static Class<?> boxPrimitive(final Class<?> targetType) {
        if (targetType == byte.class) return Byte.class;
        if (targetType == char.class) return Character.class;
        if (targetType == short.class) return Short.class;
        if (targetType == int.class) return Integer.class;
        if (targetType == long.class) return Long.class;
        if (targetType == float.class) return Float.class;
        if (targetType == double.class) return Double.class;
        if (targetType == boolean.class) return Boolean.class;
        return targetType;
    }
}
