/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Generics {

    public static Type getType(Field field) {
        return getTypeParameters(field.getType(), field.getGenericType())[0];
    }

    public static Type getReturnType(Method method) {
        return getTypeParameters(method.getReturnType(), method.getGenericReturnType())[0];
    }

    public static Type[] getTypeParameters(Class genericClass, Type type) {
        if (type instanceof Class) {
            Class rawClass = (Class) type;

            // if this is the collection class we're done
            if (genericClass.equals(type)) {
                return null;
            }

            for (Type intf : rawClass.getGenericInterfaces()) {
                Type[] collectionType = getTypeParameters(genericClass, intf);
                if (collectionType != null) {
                    return collectionType;
                }
            }

            Type[] collectionType = getTypeParameters(genericClass, rawClass.getGenericSuperclass());
            return collectionType;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type rawType = parameterizedType.getRawType();
            if (genericClass.equals(rawType)) {
                Type[] argument = parameterizedType.getActualTypeArguments();
                return argument;
            }
            Type[] collectionTypes = getTypeParameters(genericClass, rawType);
            if (collectionTypes != null) {
                for (int i = 0; i < collectionTypes.length; i++) {
                    if (collectionTypes[i] instanceof TypeVariable) {
                        TypeVariable typeVariable = (TypeVariable) collectionTypes[i];
                        TypeVariable[] rawTypeParams = ((Class) rawType).getTypeParameters();
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
