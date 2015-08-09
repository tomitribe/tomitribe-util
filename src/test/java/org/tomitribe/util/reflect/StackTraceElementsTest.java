/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.util.reflect;

import org.junit.Assert;
import org.junit.Test;

public class StackTraceElementsTest extends Assert {

    @Test
    public void testGetCurrentMethod() throws Exception {

        assertElement(StackTraceElements.getCurrentMethod(), StackTraceElementsTest.class, "testGetCurrentMethod");
        assertElement(thisMethod(), StackTraceElementsTest.class, "thisMethod");
    }

    @Test
    public void testGetCallingMethod() throws Exception {

        assertElement(theCaller(), StackTraceElementsTest.class, "testGetCallingMethod");
        assertElement(joe(), StackTraceElementsTest.class, "joe");
    }

    @Test
    public void testAsClass() throws Exception {
        final StackTraceElement stackTraceElement = StackTraceElements.getCurrentMethod();

        assertEquals(StackTraceElementsTest.class, StackTraceElements.asClass(stackTraceElement));
    }


    private void assertElement(StackTraceElement currentMethod, final Class<StackTraceElementsTest> clazz, final String method) {
        assertEquals(clazz.getName(), currentMethod.getClassName());
        assertEquals(method, currentMethod.getMethodName());
    }

    public StackTraceElement thisMethod() {
        return StackTraceElements.getCurrentMethod();
    }

    public StackTraceElement theCaller() {
        return StackTraceElements.getCallingMethod();
    }

    public StackTraceElement joe() {
        return theCaller();
    }
}