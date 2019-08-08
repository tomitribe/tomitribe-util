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
package org.tomitribe.util.dir;

import org.tomitribe.util.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public interface Dir {
    File dir();
    File mkdir();

    File get();

    File parent();

    File file(String name);

    static <T> T of(final Class<T> clazz, final File file) {
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz},
                new DirHandler(file)
        );
    }

    class DirHandler implements InvocationHandler {
        private final File dir;

        public DirHandler(final File dir) {
            this.dir = dir;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(Dir.class)) {
                if (method.getName().equals("dir")) return dir;
                if (method.getName().equals("get")) return dir;
                if (method.getName().equals("parent")) return dir.getParentFile();
                if (method.getName().equals("file")) return new File(dir, args[0].toString());
                if (method.getName().equals("mkdir")) return mkdkr();
                throw new IllegalStateException("Unknown method " + method);
            }

            final File file = new File(dir, name(method));

            if (File.class.equals(method.getReturnType())) {

                // They want an exception if the file isn't found
                if (exceptions(method).contains(FileNotFoundException.class) && !file.exists()) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }

                return file;
            }

            if (method.getReturnType().isInterface()) {
                return Dir.of(method.getReturnType(), file);
            }

            throw new UnsupportedOperationException(method.toGenericString());
        }

        private File mkdkr() {
            Files.mkdir(dir);
            return dir;
        }

        private String name(final Method method) {
            if (method.isAnnotationPresent(Name.class)) {
                return method.getAnnotation(Name.class).value();
            }
            return method.getName();
        }

        public List<Class<?>> exceptions(final Method method) {
            final Class<?>[] exceptionTypes = method.getExceptionTypes();
            return Arrays.asList(exceptionTypes);
        }
    }
}
