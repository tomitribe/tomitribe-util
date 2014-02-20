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

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

public class ReflectionTest extends Assert {
    @Test
    public void testConstructorParams() throws Exception {
        final Iterator<Parameter> params = Reflection.params(Color.class.getConstructor(String.class, int.class, boolean.class, Object.class)).iterator();

        {
            final Parameter parameter = params.next();
            assertEquals(String.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Red.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(int.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Green.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(boolean.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Blue.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(Object.class, parameter.getType());
            assertEquals(3, parameter.getAnnotations().length);
            assertEquals(Red.class, parameter.getAnnotations()[0].annotationType());
            assertEquals(Green.class, parameter.getAnnotations()[1].annotationType());
            assertEquals(Blue.class, parameter.getAnnotations()[2].annotationType());
        }

        assertFalse(params.hasNext());
    }

    @Test
    public void testMethodParams() throws Exception {
        final Iterator<Parameter> params = Reflection.params(Color.class.getMethod("foo", String.class, int.class, boolean.class, Object.class)).iterator();

        {
            final Parameter parameter = params.next();
            assertEquals(String.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Green.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(int.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Blue.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(boolean.class, parameter.getType());
            assertEquals(1, parameter.getAnnotations().length);
            assertEquals(Red.class, parameter.getAnnotations()[0].annotationType());
        }
        {
            final Parameter parameter = params.next();
            assertEquals(Object.class, parameter.getType());
            assertEquals(3, parameter.getAnnotations().length);
            assertEquals(Red.class, parameter.getAnnotations()[0].annotationType());
            assertEquals(Green.class, parameter.getAnnotations()[1].annotationType());
            assertEquals(Blue.class, parameter.getAnnotations()[2].annotationType());
        }

        assertFalse(params.hasNext());
    }

    public static class Color {

        public Color(@Red final String s, @Green final int g, @Blue final boolean b, @Red @Green @Blue final Object o) {

        }

        public void foo(@Green final String s, @Blue final int g, @Red final boolean b, @Red @Green @Blue final Object o) {

        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = ElementType.PARAMETER)
    public @interface Red {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = ElementType.PARAMETER)
    public @interface Green {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = ElementType.PARAMETER)
    public @interface Blue {
    }
}
