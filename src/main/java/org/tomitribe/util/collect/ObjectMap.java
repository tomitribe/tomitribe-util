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
package org.tomitribe.util.collect;


import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.SetAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ObjectMap extends AbstractMap<String, Object> {
    private static final Pattern GETTER_PREFIX = Pattern.compile("(get|is|find)");
    private static final Method IS_RECORD;
    static {
        Method isRecord = null;
        try {
            isRecord = Class.class.getMethod("isRecord");
        } catch (NoSuchMethodException e) {
            // no-op
        }
        IS_RECORD = isRecord;
    }

    private final Object object;
    private final Map<String, Entry<String, Object>> attributes;
    private final Set<Entry<String, Object>> entries;

    public ObjectMap(final Object object) {
        this(object.getClass(), object);
    }

    public ObjectMap(final Class clazz) {
        this(clazz, null);
    }

    public ObjectMap(final Class<?> clazz, final Object object) {
        this.object = object;

        attributes = new HashMap<String, Entry<String, Object>>();

        final boolean record = isRecord(clazz);
        if (!record) {
            initPOJO(clazz);
        } else {
            initRecord(clazz);
        }

        entries = Collections.unmodifiableSet(new HashSet<Entry<String, Object>>(attributes.values()));
    }

    private void initRecord(final Class<?> clazz) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                attributes.put(field.getName(), new MethodEntry(
                        "set" + field.getName(), clazz.getMethod(field.getName()), null));
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void initPOJO(final Class<?> clazz) {
        for (final Field field : clazz.getFields()) {
            if (field.isEnumConstant()) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            final FieldEntry entry = new FieldEntry(field);
            attributes.put(entry.getKey(), entry);
        }

        methodAccessors(clazz);

        for (final Class<?> intrface : clazz.getInterfaces()) {
            methodAccessors(intrface);
        }
    }

    private void methodAccessors(final Class<?> clazz) {
        for (final Method getter : clazz.getMethods()) {
            if (!isValidGetter(getter)) continue;

            final String name = GETTER_PREFIX.matcher(getter.getName()).replaceFirst("set");
            final Method setter = getOptionalMethod(clazz, name, getter.getReturnType());

            final MethodEntry entry = new MethodEntry(name, getter, setter);

            attributes.put(entry.getKey(), entry);
        }
    }

    private boolean isRecord(final Class<?> clazz) {
        if (IS_RECORD == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(IS_RECORD.invoke(clazz));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    private boolean isValidGetter(Method m) {
        if (!m.getDeclaringClass().isInterface() && Modifier.isAbstract(m.getModifiers())) return false;
        if (Modifier.isStatic(m.getModifiers())) return false;

        // Void methods are not valid getters
        if (Void.TYPE.equals(m.getReturnType())) return false;

        // Must have no parameters
        if (m.getParameterTypes().length != 0) return false;

        // Must start with "get" or "is"
        if (m.getName().startsWith("get") && m.getName().length() > 3) return true;
        if (m.getName().startsWith("find") && m.getName().length() > 4) return true;
        if (!m.getName().startsWith("is")) return false;

        // If it starts with "is" it must return boolean
        if (m.getReturnType().equals(Boolean.class)) return true;
        return m.getReturnType().equals(Boolean.TYPE);
    }

    private Method getOptionalMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException thisIsOk) {
            return null;
        }
    }

    @Override
    public Object get(final Object key) {
        final Entry<String, Object> entry = attributes.get(key);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    @Override
    public Object put(final String key, final Object value) {
        final Entry<String, Object> entry = attributes.get(key);
        if (entry == null) return null;
        return entry.setValue(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return entries;
    }

    public class FieldEntry implements Member {

        private final Field field;

        public FieldEntry(final Field field) {
            this.field = field;
        }

        @Override
        public String getKey() {
            return field.getName();
        }

        @Override
        public Object getValue() {
            try {
                return field.get(object);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object setValue(Object value) {
            try {
                final Object replaced = getValue();
                value = Converter.convert(value, field.getType(), getKey());
                field.set(object, value);
                return replaced;
            } catch (final IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return field.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return field.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return field.getDeclaredAnnotations();
        }

    }

    public interface Member extends Entry<String, Object>, java.lang.reflect.AnnotatedElement {
        Class<?> getType();

        boolean isReadOnly();
    }

    public class MethodEntry implements Member {
        private final String key;
        private final Method getter;
        private final Method setter;

        public MethodEntry(final String methodName, final Method getter, final Method setter) {
            final StringBuilder name = new StringBuilder(methodName);

            // remove 'set' or 'get'
            name.delete(0, 3);

            // lowercase first char
            name.setCharAt(0, Character.toLowerCase(name.charAt(0)));

            this.key = name.toString();
            this.getter = getter;
            this.setter = setter;
        }

        protected Object invoke(final Method method, final Object... args) {
            if (!Modifier.isPublic(method.getModifiers())) {
                SetAccessible.on(method);
            }

            try {
                return method.invoke(object, args);
            } catch (final InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            } catch (final Exception e) {
                throw new IllegalStateException(String.format("Key: %s, Method: %s", key, method.toString()), e);
            }
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return invoke(getter);
        }

        @Override
        public Object setValue(Object value) {
            if (setter == null) throw new IllegalArgumentException(String.format("'%s' is read-only", key));
            final Object original = getValue();
            value = Converter.convert(value, setter.getParameterTypes()[0], getKey());
            invoke(setter, value);
            return original;
        }

        @Override
        public Class<?> getType() {
            return getter.getReturnType();
        }

        @Override
        public boolean isReadOnly() {
            return setter != null;
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return getter.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return getter.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getter.getDeclaredAnnotations();
        }
    }


}
