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
import org.tomitribe.util.Hex;
import org.tomitribe.util.IO;
import org.tomitribe.util.PrintString;

import java.util.Iterator;
import java.util.List;

public class ClassesTest extends Assert {

    @Test
    public void testForName() throws Exception {
        final Class clazz = Classes.forName(ClassesTest.class.getName(), ClassesTest.class.getClassLoader());
        assertEquals(ClassesTest.class, clazz);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testForNameNotFound() throws Exception {
        final Class clazz = Classes.forName(ClassesTest.class.getName()+"s", ClassesTest.class.getClassLoader());
        assertEquals(ClassesTest.class, clazz);
    }

    @Test
    public void testPackageName() throws Exception {
        final String s = Classes.packageName(ClassesTest.class);
        assertEquals("org.tomitribe.util.reflect", s);
    }

    @Test
    public void testPackageName1() throws Exception {
        final String s = Classes.packageName(ClassesTest.class.getName());
        assertEquals("org.tomitribe.util.reflect", s);
    }

    @Test
    public void testSimpleName() throws Exception {
        final String s = Classes.simpleName(ClassesTest.class.getName());
        assertEquals("ClassesTest", s);
    }

    @Test
    public void testSimpleName1() throws Exception {
        final String s = Classes.simpleName(ClassesTest.class);
        assertEquals("ClassesTest", s);
    }

    @Test
    public void testGetSimpleNames() throws Exception {
        final List<String> simpleNames = Classes.getSimpleNames(IO.class, Classes.class, Hex.class);

        final Iterator<String> iterator = simpleNames.iterator();
        assertEquals("IO", iterator.next());
        assertEquals("Classes", iterator.next());
        assertEquals("Hex", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testDeprimitivize() throws Exception {
        assertEquals(Byte.class, Classes.deprimitivize(byte.class));
        assertEquals(Short.class, Classes.deprimitivize(short.class));
        assertEquals(Integer.class, Classes.deprimitivize(int.class));
        assertEquals(Long.class, Classes.deprimitivize(long.class));
        assertEquals(Float.class, Classes.deprimitivize(float.class));
        assertEquals(Double.class, Classes.deprimitivize(double.class));
        assertEquals(Character.class, Classes.deprimitivize(char.class));
        assertEquals(Boolean.class, Classes.deprimitivize(boolean.class));
    }

    @Test
    public void testAncestors() throws Exception {
        final List<Class<?>> ancestors = Classes.ancestors(PrintString.class);

        final Iterator<Class<?>> iterator = ancestors.iterator();
        assertEquals("org.tomitribe.util.PrintString", iterator.next().getName());
        assertEquals("java.io.PrintStream", iterator.next().getName());
        assertEquals("java.io.FilterOutputStream", iterator.next().getName());
        assertEquals("java.io.OutputStream", iterator.next().getName());
        assertFalse(iterator.hasNext());
    }
}