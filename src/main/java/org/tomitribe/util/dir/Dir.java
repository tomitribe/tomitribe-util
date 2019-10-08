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
import org.tomitribe.util.reflect.Generics;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>Efforts to create strongly-typed code are often poisoned by string file path references
 * spread all over the same codebase.</p>
 *
 * <p>This utility was born out of a desire that all path references could be compile-time checked and code completed.</p>
 *
 * <h3>Step 1 Create an interface matching a directory</h3>
 *
 * <p>For example, a directory structure like this:</p>
 *
 * <pre>
 * project/
 *   - src/
 *   - target/
 *   - .git/
 *   - pom.xml</pre>
 *
 * <p>Could be handled with the following interface:</p>
 *
 * <pre>
 * public interface Project {
 *     File src();
 *     File target();
 *     &#64;Name(".git")
 *     File git();
 *     &#64;Name("pom.xml")
 *     File pomXml();
 * }</pre>
 *
 * <h3>Step 2 Get a proxied reference to that directory</h3>
 *
 * <pre>
 *     File mydir = new File("/some/path/to/a/project");
 *     Project project = Dir.of(Project.class,  mydir);</pre>
 *
 * <p>Under the covers the interface is implemented as a dynamic proxy whose InvocationHandler is
 * holding the actual File object.</p>
 *
 * <h3>Returning Another Interface</h3>
 *
 * <p>Instead of <code>src()</code> returning a <code>File</code>, it could return another similar interface. For example</p>
 *
 * <pre>
 * public interface Src {
 *     File main();
 *     File test();
 * } </pre>
 *
 * <p>And now we update <code>Project</code> so the <code>src()</code> method will return <code>Src</code>
 * <pre>
 * public interface Project {
 *     Src src();
 *     File target();
 *     &#64;Name(".git")
 *     File git();
 *     &#64;Name("pom.xml")
 *     File pomXml();
 * } </pre>
 *
 *
 * <p>Now we have a strongly-typed directory structure that also supports code completion in the IDE.</p>
 *
 * <h3>Passing a Subdirectory Name</h3>
 *
 * <p>There may be times when you don't know the exact subdirectory name, but you know that it will use a specific
 * directory structure.  Here's how you might reference a nested Maven module structure:</p>

 * <pre>
 * public interface Module {
 *     &#64;Name("pom.xml")
 *     File pomXml();
 *     File src();
 *     File target();
 *     Module submodule(String name);
 * } </pre>
 *
 */
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
                if (method.getName().equals("mkdir")) return mkdkr();
                throw new IllegalStateException("Unknown method " + method);
            }

            final File file = new File(dir, name(method));

            final Class<?> returnType = method.getReturnType();

            if (returnType.isArray()) {
                return returnArray(method);
            }

            if (Stream.class.equals(returnType) && args == null) {
                return returnStream(method);
            }

            if (File.class.equals(returnType) && args == null) {
                return returnFile(method, file);
            }

            if (returnType.isInterface() && args != null && args.length == 1 && args[0] instanceof String) {
                return Dir.of(returnType, new File(dir, (String) args[0]));
            }

            if (returnType.isInterface() && args == null) {
                return Dir.of(returnType, file);
            }

            throw new UnsupportedOperationException(method.toGenericString());
        }

        private Object returnStream(final Method method) {
            final Class returnType = (Class) Generics.getReturnType(method);
            final Predicate<File> filter = getFilter(method);

            if (returnType.isInterface()) {
                return stream(dir, method)
                        .filter(filter)
                        .map(child -> Dir.of(returnType, child));
            }
            if (File.class.equals(returnType)) {
                return stream(dir, method)
                        .filter(filter);
            }
            throw new UnsupportedOperationException(method.toGenericString());
        }

        private Object returnFile(final Method method, final File file) throws FileNotFoundException {
            // They want an exception if the file isn't found
            if (exceptions(method).contains(FileNotFoundException.class) && !file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }

            return file;
        }

        private Object returnArray(final Method method) {
            final Predicate<File> filter = getFilter(method);

            final Class<?> arrayType = method.getReturnType().getComponentType();

            if (File.class.equals(arrayType)) {

                return stream(dir, method)
                        .filter(filter)
                        .toArray(File[]::new);

            } else if (arrayType.isInterface()) {

                // will be an array of type Object[]
                final Object[] src = stream(dir, method)
                        .filter(filter)
                        .map(child -> Dir.of(arrayType, child))
                        .toArray();

                // will be an array of the user's interface type
                final Object[] dest = (Object[]) Array.newInstance(arrayType, src.length);

                System.arraycopy(src, 0, dest, 0, src.length);

                return dest;
            }

            throw new UnsupportedOperationException(method.toGenericString());
        }

        private static Stream<File> stream(final File dir, final Method method) {
            final Walk walk = method.getAnnotation(Walk.class);

            if (walk != null) return walk(walk, dir);

            return Stream.of(dir.listFiles());

        }

        private static Stream<File> walk(final Walk walk, final File dir) {
            try {
                if (walk.maxDepth() != -1) {
                    return java.nio.file.Files.walk(dir.toPath(), walk.maxDepth())
                            .map(Path::toFile);
                }
                return java.nio.file.Files.walk(dir.toPath())
                        .map(Path::toFile);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private Predicate<File> getFilter(final Method method) {
            final Filter filter = method.getAnnotation(Filter.class);

            if (filter == null) return pathname -> true;

            final Class<? extends FileFilter> clazz = filter.value();
            try {
                final FileFilter fileFilter = clazz.newInstance();
                return fileFilter::accept;
            } catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate filter " + clazz, e);
            }
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