/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
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
 * Can convert anything with a:
 * - PropertyEditor
 * - Constructor that accepts String
 * - public static method that returns itself and takes a String
 */
public class Converter {

    private Converter() {
        // no-op
    }

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
                if (Collection.class == raw || List.class == raw) {
                    values = new ArrayList(split.length);
                } else if (Set.class == raw) {
                    values = SortedSet.class == raw ? new TreeSet() : new HashSet(split.length);
                } else {
                    throw new IllegalArgumentException(targetType + " collection type not supported");
                }

                for (final String val : split) {
                    values.add(convert(val, argType, name));
                }

                return values;
            } else if (Map.class.isAssignableFrom(rawClass)) {
                final Map map;
                if (SortedMap.class == raw) {
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

    public static Object convert(final Object value, Class<?> targetType, final String name) {
        if (value == null) {
            if (targetType.equals(Boolean.TYPE)) return false;
            return null;
        }

        final Class<?> actualType = value.getClass();

        if (targetType.isPrimitive()) targetType = boxPrimitive(targetType);

        if (targetType.isAssignableFrom(actualType)) return value;

        if (Number.class.isAssignableFrom(actualType) && Number.class.isAssignableFrom(targetType)) {
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

        try {
            final Constructor<?> constructor = type.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (final NoSuchMethodException e) {
            // fine
        } catch (final Exception e) {
            final String message = String.format("Cannot convert string '%s' to %s.", value, type);
            throw new IllegalArgumentException(message, e);
        }

        try {
            final Constructor<?> constructor = type.getConstructor(CharSequence.class);
            return constructor.newInstance(value);
        } catch (final NoSuchMethodException e) {
            // fine
        } catch (final Exception e) {
            final String message = String.format("Cannot convert string '%s' to %s.", value, type);
            throw new IllegalArgumentException(message, e);
        }

        final List<Method> methods = Stream.of(type.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
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
