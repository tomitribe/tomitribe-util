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
package org.tomitribe.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Generics {

    private Generics() {
        // no-op
    }

    public static Type getType(final Field field) {
        return getTypeParameters(field.getType(), field.getGenericType())[0];
    }

    public static Type getType(final Parameter parameter) {
        return getTypeParameters(parameter.getType(), parameter.getGenericType())[0];
    }

    public static Type getReturnType(final Method method) {
        return getTypeParameters(method.getReturnType(), method.getGenericReturnType())[0];
    }

    public static Type[] getTypeParameters(final Class genericClass, final Type type) {
        if (type instanceof Class) {
            final Class rawClass = (Class) type;

            // if this is the collection class we're done
            if (genericClass.equals(type)) return null;

            for (final Type intf : rawClass.getGenericInterfaces()) {
                final Type[] collectionType = getTypeParameters(genericClass, intf);

                if (collectionType != null) return collectionType;
            }

            final Type[] collectionType = getTypeParameters(genericClass, rawClass.getGenericSuperclass());
            return collectionType;

        } else if (type instanceof ParameterizedType) {

            final ParameterizedType parameterizedType = (ParameterizedType) type;

            final Type rawType = parameterizedType.getRawType();

            if (genericClass.equals(rawType)) {

                final Type[] argument = parameterizedType.getActualTypeArguments();
                return argument;

            }

            final Type[] collectionTypes = getTypeParameters(genericClass, rawType);

            if (collectionTypes != null) {

                for (int i = 0; i < collectionTypes.length; i++) {

                    if (collectionTypes[i] instanceof TypeVariable) {

                        final TypeVariable typeVariable = (TypeVariable) collectionTypes[i];
                        final TypeVariable[] rawTypeParams = ((Class) rawType).getTypeParameters();

                        for (int j = 0; j < rawTypeParams.length; j++) {

                            if (typeVariable.getName().equals(rawTypeParams[j].getName())) {
                                collectionTypes[i] = parameterizedType.getActualTypeArguments()[j];
                            }
                        }
                    }
                }
            }

            return collectionTypes;
        }

        return null;
    }

}
