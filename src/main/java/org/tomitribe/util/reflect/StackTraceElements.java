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

import java.util.Arrays;
import java.util.Iterator;

public class StackTraceElements {

    private StackTraceElements() {
    }

    public static StackTraceElement getCurrentMethod() {
        final Iterator<StackTraceElement> stackTrace = seek(StackTraceElements.class, "getCurrentMethod");

        return stackTrace.next();
    }

    public static StackTraceElement getCallingMethod() {
        final Iterator<StackTraceElement> stackTrace = seek(StackTraceElements.class, "getCallingMethod");

        stackTrace.next();
        return stackTrace.next();
    }

    private static Iterator<StackTraceElement> seek(final Class<StackTraceElements> clazz, final String method) {
        final Iterator<StackTraceElement> stackTrace = Arrays.asList(new Exception().fillInStackTrace().getStackTrace()).iterator();

        while (stackTrace.hasNext()) {
            final StackTraceElement next = stackTrace.next();
            if (next.getClassName().equals(clazz.getName()) && next.getMethodName().equals(method)) {
                break;
            }
        }
        return stackTrace;
    }

    public static Class<?> asClass(final StackTraceElement stackTraceElement) throws ClassNotFoundException {
        return asClass(stackTraceElement, Thread.currentThread().getContextClassLoader());
    }

    public static Class<?> asClass(final StackTraceElement stackTraceElement, final ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass(stackTraceElement.getClassName());
    }
}
