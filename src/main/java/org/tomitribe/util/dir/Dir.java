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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
    // TODO this should return Dir
    File dir();

    Dir dir(String name);

    File mkdir();

    File mkdirs();

    File get();

    File parent();

    // replacement for dir()
    File file();

    File file(String name);

    /**
     * Recursively walks downward from this path.
     * @return Stream of files and directories down from this path
     */
    Stream<File> walk();

    /**
     * Recursively walks downward from this path.
     * @param depth Limit the recursion to specified depth
     * @return Stream of files and directories down from this path
     */
    Stream<File> walk(int depth);

    /**
     * Recursively walks downward from this path.
     * @return Stream of files down from this path, excluding directories
     */
    Stream<File> files();

    /**
     * Recursively walks downward from this path.
     * @param depth Limit the recursion to specified depth
     * @return Stream of files down from this path, excluding directories
     */
    Stream<File> files(int depth);

    void delete();

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

            if (method.isDefault()) {
                return invokeDefault(proxy, method, args);
            }

            if (method.getDeclaringClass().equals(Object.class)) {
                if (method.getName().equals("toString")) return toString();
                if (method.getName().equals("equals")) return equals(proxy, args);
                if (method.getName().equals("hashCode")) return hashCode();
            }
            if (method.getDeclaringClass().equals(Dir.class)) {
                if (method.getName().equals("dir")) return dir(args);
                if (method.getName().equals("get")) return dir;
                if (method.getName().equals("parent")) return dir.getParentFile();
                if (method.getName().equals("mkdir")) return mkdir();
                if (method.getName().equals("mkdirs")) return mkdirs();
                if (method.getName().equals("delete")) return delete();
                if (method.getName().equals("file")) return file(args);
                if (method.getName().equals("walk")) return walk(args);
                if (method.getName().equals("files")) return walk(args).filter(File::isFile);
                throw new IllegalStateException("Unknown method " + method);
            }

            final File file = new File(dir, name(method));
            final Function<File, File> action = action(method);

            final Class<?> returnType = method.getReturnType();

            if (returnType.isArray()) {
                return returnArray(method);
            }

            if (Stream.class.equals(returnType) && args == null) {
                return returnStream(method);
            }

            if (File.class.equals(returnType) && args == null) {
                return returnFile(method, action.apply(file));
            }

            if (returnType.isInterface() && args != null && args.length == 1 && args[0] instanceof String) {
                return Dir.of(returnType, action.apply(new File(dir, (String) args[0])));
            }

            if (returnType.isInterface() && args == null) {
                return Dir.of(returnType, action.apply(file));
            }

            throw new UnsupportedOperationException(method.toGenericString());
        }

        private Object dir(final Object[] args) {
            if (args == null || args.length == 0) return dir;
            return Dir.of(Dir.class, file(args));
        }

        private Stream<File> walk(final Object[] args) {
            if (args == null || args.length == 0) return walk(dir, -1, 0);
            return walk(dir, (Integer) args[0], 0);
        }

        private boolean equals(final Object proxy, final Object[] args) {
            if (args.length != 1) return false;
            if (args[0] == null) return false;
            if (!proxy.getClass().isAssignableFrom(args[0].getClass())) return false;

            final InvocationHandler handler = Proxy.getInvocationHandler(args[0]);
            return equals(handler);
        }

        private File file(final Object[] args) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Expected String argument.  Found args length: " + args.length);
            }
            if (args[0] == null) {
                throw new IllegalArgumentException("Expected String argument.  Found null");
            }
            if (!String.class.equals(args[0].getClass())) {
                throw new IllegalArgumentException("Expected String argument.  Found " + args[0].getClass());
            }
            return new File(this.dir, args[0].toString());
        }

        private static Object invokeDefault(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final float version = Float.parseFloat(System.getProperty("java.class.version"));
            if (version <= 52) { // Java 8
                final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);

                final Class<?> clazz = method.getDeclaringClass();
                return constructor.newInstance(clazz)
                        .in(clazz)
                        .unreflectSpecial(method, clazz)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            } else { // Java 9 and later
                return MethodHandles.lookup()
                        .findSpecial(
                                method.getDeclaringClass(),
                                method.getName(),
                                MethodType.methodType(method.getReturnType(), new Class[0]),
                                method.getDeclaringClass()
                        ).bindTo(proxy)
                        .invokeWithArguments(args);
            }
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
            return walk(dir, walk.maxDepth(), walk.minDepth());
        }

        private static Stream<File> walk(final File dir, final int maxDepth, final int minDepth) {
            final Predicate<File> min = minDepth <= 0 ? file -> true : minDepth(dir, minDepth);
            try {
                if (maxDepth != -1) {
                    return java.nio.file.Files.walk(dir.toPath(), maxDepth)
                            .map(Path::toFile)
                            .filter(min);
                }
                return java.nio.file.Files.walk(dir.toPath())
                        .map(Path::toFile)
                        .filter(min);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private static Predicate<File> minDepth(final File dir, final int minDepth) {
            int parentDepth = getDepth(dir);
            return file -> {
                final int fileDepth = getDepth(file);
                final int depth = fileDepth - parentDepth;
                return depth >= minDepth;
            };
        }

        private static int getDepth(final File dir) {
            int depth = 0;
            File f = dir;
            while (f != null) {
                f = f.getParentFile();
                depth++;
            }
            return depth;
        }

        private Predicate<File> getFilter(final Method method) {
            if (method.isAnnotationPresent(Filter.class)) {
                final Filter filter = method.getAnnotation(Filter.class);
                return asPredicate(filter);
            }

            if (method.isAnnotationPresent(Filters.class)) {
                final Filters filters = method.getAnnotation(Filters.class);
                Predicate<File> predicate = file -> true;
                for (final Filter filter : filters.value()) {
                    predicate = predicate.and(asPredicate(filter));
                }
                return predicate;
            }

            return pathname -> true;
        }

        private Predicate<File> asPredicate(final Filter filter) {
            if (filter == null) return pathname -> true;

            final Class<? extends FileFilter> clazz = filter.value();
            try {
                final FileFilter fileFilter = clazz.newInstance();
                return fileFilter::accept;
            } catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate filter " + clazz, e);
            }
        }

        private File mkdir() {
            Files.mkdir(dir);
            return dir;
        }

        private Void mkdirs() {
            Files.mkdirs(dir);
            return null;
        }

        private Void delete() {
            Files.remove(dir);
            return null;
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

        public Function<File, File> action(final Method method) {
            if (method.isAnnotationPresent(Mkdir.class)) return mkdir(method);
            if (method.isAnnotationPresent(Mkdirs.class)) return mkdirs(method);
            return noop(method);
        }

        public Function<File, File> mkdir(final Method method) {
            return file -> {
                try {
                    Files.mkdir(file);
                    return file;
                } catch (Exception e) {
                    throw new MkdirFailedException(method, file, e);
                }
            };
        }

        public Function<File, File> mkdirs(final Method method) {
            return file -> {
                try {
                    Files.mkdirs(file);
                    return file;
                } catch (Exception e) {
                    throw new MkdirsFailedException(method, file, e);
                }
            };
        }

        public Function<File, File> noop(final Method method) {
            return file -> file;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DirHandler that = (DirHandler) o;
            return dir.equals(that.dir);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dir);
        }

        @Override
        public String toString() {
            return dir.getAbsolutePath();
        }
    }


    class MkdirFailedException extends RuntimeException {
        public MkdirFailedException(final Method method, final File dir, final Throwable t) {
            super(String.format("@Mkdir failed%n method: %s%n path: %s", method, dir.getAbsolutePath()), t);
        }
    }

    class MkdirsFailedException extends RuntimeException {
        public MkdirsFailedException(final Method method, final File dir, final Throwable t) {
            super(String.format("@Mkdirs failed%n method: %s%n path: %s", method, dir.getAbsolutePath()), t);
        }
    }
}
