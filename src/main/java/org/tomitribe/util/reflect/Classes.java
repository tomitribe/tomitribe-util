/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.reflect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classes {

    private Classes() {
        // no-op
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<Class<?>, Class<?>>();
    private static final HashMap<String, Class> PRIMITIVES = new HashMap<String, Class>();

    static {
        PRIMITIVES.put("boolean", boolean.class);
        PRIMITIVES.put("byte", byte.class);
        PRIMITIVES.put("char", char.class);
        PRIMITIVES.put("short", short.class);
        PRIMITIVES.put("int", int.class);
        PRIMITIVES.put("long", long.class);
        PRIMITIVES.put("float", float.class);
        PRIMITIVES.put("double", double.class);

        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPERS.put(char.class, Character.class);
        PRIMITIVE_WRAPPERS.put(double.class, Double.class);
        PRIMITIVE_WRAPPERS.put(float.class, Float.class);
        PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVE_WRAPPERS.put(long.class, Long.class);
        PRIMITIVE_WRAPPERS.put(short.class, Short.class);
    }

    public static Class forName(String string, final ClassLoader classLoader) throws ClassNotFoundException {
        int arrayDimentions = 0;
        while (string.endsWith("[]")) {
            string = string.substring(0, string.length() - 2);
            arrayDimentions++;
        }

        Class clazz = PRIMITIVES.get(string);

        if (clazz == null) clazz = Class.forName(string, true, classLoader);

        if (arrayDimentions == 0) {
            return clazz;
        }
        return Array.newInstance(clazz, new int[arrayDimentions]).getClass();
    }

    public static String packageName(final Class clazz) {
        return packageName(clazz.getName());
    }

    public static String packageName(final String clazzName) {
        final int i = clazzName.lastIndexOf('.');
        if (i > 0) {
            return clazzName.substring(0, i);
        } else {
            return "";
        }
    }

    public static List<String> getSimpleNames(final Class... classes) {
        final List<String> list = new ArrayList<String>();
        for (final Class aClass : classes) {
            list.add(aClass.getSimpleName());
        }

        return list;
    }

    public static Class<?> deprimitivize(final Class<?> fieldType) {
        return fieldType.isPrimitive() ? PRIMITIVE_WRAPPERS.get(fieldType) : fieldType;
    }

    /**
     * Creates a list of the specified class and all its parent classes
     *
     * @param clazz
     * @return
     */
    public static List<Class<?>> ancestors(Class clazz) {
        final ArrayList<Class<?>> ancestors = new ArrayList<Class<?>>();

        while (clazz != null && !clazz.equals(Object.class)) {
            ancestors.add(clazz);
            clazz = clazz.getSuperclass();
        }

        return ancestors;
    }
}
