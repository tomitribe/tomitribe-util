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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Reflection {


    public static Iterable<Method> methods(final Class<?> clazz) {
        return new ArrayList<Method>(Arrays.asList(clazz.getMethods()));
    }

    public static Iterable<Parameter> params(final Method method) {
        return new Iterable<Parameter>() {
            @Override
            public Iterator<Parameter> iterator() {
                return new Iterator<Parameter>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < method.getParameterTypes().length;
                    }

                    @Override
                    public Parameter next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return new Parameter(method.getParameterAnnotations()[index], method.getParameterTypes()[index], method.getGenericParameterTypes()[index++]);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static Iterable<Parameter> params(final Constructor constructor) {
        return new Iterable<Parameter>() {
            @Override
            public Iterator<Parameter> iterator() {
                return new Iterator<Parameter>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < constructor.getParameterTypes().length;
                    }

                    @Override
                    public Parameter next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return new Parameter(constructor.getParameterAnnotations()[index], constructor.getParameterTypes()[index], constructor.getGenericParameterTypes()[index++]);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
