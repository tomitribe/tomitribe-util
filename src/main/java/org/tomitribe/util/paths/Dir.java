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
package org.tomitribe.util.paths;


import org.tomitribe.util.reflect.Generics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
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
    Path dir();

    Dir dir(String name);

    Path mkdir();

    Path mkdirs();

    Path get();

    Path parent();

    // replacement for dir()
    Path file();

    Path file(String name);

    /**
     * Recursively walks downward from this path.
     * @return Stream of files and directories down from this path
     */
    Stream<Path> walk();

    /**
     * Recursively walks downward from this path.
     * @param depth Limit the recursion to specified depth
     * @return Stream of files and directories down from this path
     */
    Stream<Path> walk(int depth);

    /**
     * Recursively walks downward from this path.
     * @return Stream of files down from this path, excluding directories
     */
    Stream<Path> files();

    /**
     * Recursively walks downward from this path.
     * @param depth Limit the recursion to specified depth
     * @return Stream of files down from this path, excluding directories
     */
    Stream<Path> files(int depth);

    void delete();

    static <T> T of(final Class<T> clazz, final Path path) {
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz},
                new DirHandler(path)
        );
    }

    class DirHandler implements InvocationHandler {
        private final Path dir;

        public DirHandler(final Path dir) {
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
                if (method.getName().equals("parent")) return dir.getParent();
                if (method.getName().equals("mkdir")) return mkdir();
                if (method.getName().equals("mkdirs")) return mkdirs();
                if (method.getName().equals("delete")) return delete();
                if (method.getName().equals("file")) return file(args);
                if (method.getName().equals("walk")) return walk(args);
                if (method.getName().equals("files")) return walk(args).filter(Files::isRegularFile);
                throw new IllegalStateException("Unknown method " + method);
            }

            final Path path = getFile(method);
            final Function<Path, Path> action = action(method);

            final Class<?> returnType = method.getReturnType();

            if (returnType.isArray()) {
                return returnArray(method);
            }

            if (Stream.class.equals(returnType) && args == null) {
                return returnStream(method);
            }

            if (Path.class.equals(returnType) && args == null) {
                return returnFile(method, action.apply(path));
            }

            final FileWrapper wrapper = new FileWrapper(returnType);
            if (wrapper.isWrapper()) {
                return returnFileWrapper(method, wrapper, action.apply(path));
            }

            if (returnType.isInterface() && args != null && args.length == 1 && args[0] instanceof String) {
                return Dir.of(returnType, action.apply(dir.resolve((String) args[0])));
            }

            if (returnType.isInterface() && args == null) {
                return Dir.of(returnType, action.apply(path));
            }

            throw new UnsupportedOperationException(method.toGenericString());
        }

        private Path getFile(final Method method) {
            final Parent parent = method.getAnnotation(Parent.class);
            if (parent != null) {
                Path parentPath = dir;
                for (int depth = parent.value(); depth > 0; depth--) {
                    parentPath = parentPath.getParent();
                }

                return parentPath;
            }

            return dir.resolve(name(method));
        }

        private Object dir(final Object[] args) {
            if (args == null || args.length == 0) return dir;
            return Dir.of(Dir.class, file(args));
        }

        private Stream<Path> walk(final Object[] args) {
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

        private Path file(final Object[] args) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Expected String argument.  Found args length: " + args.length);
            }
            if (args[0] == null) {
                throw new IllegalArgumentException("Expected String argument.  Found null");
            }
            if (!String.class.equals(args[0].getClass())) {
                throw new IllegalArgumentException("Expected String argument.  Found " + args[0].getClass());
            }
            return this.dir.resolve(args[0].toString());
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
                                MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                                method.getDeclaringClass()
                        ).bindTo(proxy)
                        .invokeWithArguments(args);
            }
        }

        private Object returnStream(final Method method) {
            final Class returnType = (Class) Generics.getReturnType(method);
            final Predicate<Path> filter = getFilter(method);

            if (Path.class.equals(returnType)) {
                return stream(dir, method)
                        .filter(filter);
            }

            final FileWrapper wrapper = new FileWrapper(returnType);
            if (wrapper.isWrapper()) {
                return stream(dir, method)
                        .filter(filter)
                        .map(wrapper::wrap)
                        ;
            }

            if (returnType.isInterface()) {
                return stream(dir, method)
                        .filter(filter)
                        .map(child -> Dir.of(returnType, child));
            }
            throw new UnsupportedOperationException(method.toGenericString());
        }

        private Object returnFile(final Method method, final Path path) throws FileNotFoundException {
            // They want an exception if the file isn't found
            if (exceptions(method).contains(FileNotFoundException.class) && !Files.exists(path)) {
                throw new FileNotFoundException(path.toAbsolutePath().toString());
            }

            return path;
        }

        private Object returnFileWrapper(final Method method, final FileWrapper wrapper, final Path path) throws FileNotFoundException {
            // They want an exception if the file isn't found
            if (exceptions(method).contains(FileNotFoundException.class) && !Files.exists(path)) {
                throw new FileNotFoundException(path.toAbsolutePath().toString());
            }

            return wrapper.wrap(path);
        }

        private Object returnArray(final Method method) {
            final Predicate<Path> filter = getFilter(method);

            final Class<?> arrayType = method.getReturnType().getComponentType();

            if (Path.class.equals(arrayType)) {

                return stream(dir, method)
                        .filter(filter)
                        .toArray(Path[]::new);

            }

            final FileWrapper wrapper = new FileWrapper(arrayType);
            if (wrapper.isWrapper()) {

                /*
                 * This array is of type Object, not what we need
                 */
                final Object[] src = stream(dir, method)
                        .filter(filter)
                        .map(wrapper::wrap)
                        .toArray();

                /*
                 * Create a new array of the proper type and populate it
                 */
                final Object[] dest = (Object[]) Array.newInstance(arrayType, src.length);
                System.arraycopy(src, 0, dest, 0, src.length);

                return dest;
            }

            if (arrayType.isInterface()) {

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

        private static Stream<Path> stream(final Path dir, final Method method) {
            final Walk walk = method.getAnnotation(Walk.class);

            if (walk != null) return walk(walk, dir);

            try {
                return Files.list(dir);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static Stream<Path> walk(final Walk walk, final Path dir) {
            return walk(dir, walk.maxDepth(), walk.minDepth());
        }

        private static Stream<Path> walk(final Path dir, final int maxDepth, final int minDepth) {
            final Predicate<Path> min = minDepth <= 0 ? path -> true : minDepth(dir, minDepth);
            try {
                if (maxDepth != -1) {
                    return Files.walk(dir, maxDepth)
                            .filter(min);
                }
                return Files.walk(dir)
                        .filter(min);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private static Predicate<Path> minDepth(final Path dir, final int minDepth) {
            int parentDepth = getDepth(dir);
            return path -> {
                final int fileDepth = getDepth(path);
                final int depth = fileDepth - parentDepth;
                return depth >= minDepth;
            };
        }

        private static int getDepth(final Path dir) {
            int depth = 0;
            Path f = dir;
            while (f != null) {
                f = f.getParent();
                depth++;
            }
            return depth;
        }

        private Predicate<Path> getFilter(final Method method) {
            if (method.isAnnotationPresent(Filter.class)) {
                final Filter filter = method.getAnnotation(Filter.class);
                return asPredicate(filter);
            }

            if (method.isAnnotationPresent(Filters.class)) {
                final Filters filters = method.getAnnotation(Filters.class);
                Predicate<Path> predicate = path -> true;
                for (final Filter filter : filters.value()) {
                    predicate = predicate.and(asPredicate(filter));
                }
                return predicate;
            }

            return pathname -> true;
        }

        private Predicate<Path> asPredicate(final Filter filter) {
            if (filter == null) return pathname -> true;

            final Class<? extends Predicate<Path>> clazz = filter.value();
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to instantiate filter " + clazz, e);
            }
        }

        private Path mkdir() {
            Paths.mkdir(dir);
            return dir;
        }

        private Void mkdirs() {
            Paths.mkdirs(dir);
            return null;
        }

        private Void delete() {
            Paths.remove(dir);
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

        public Function<Path, Path> action(final Method method) {
            if (method.isAnnotationPresent(Mkdir.class)) return mkdir(method);
            if (method.isAnnotationPresent(Mkdirs.class)) return mkdirs(method);
            return noop(method);
        }

        public Function<Path, Path> mkdir(final Method method) {
            return path -> {
                try {
                    Paths.mkdir(path);
                    return path;
                } catch (Exception e) {
                    throw new MkdirFailedException(method, path, e);
                }
            };
        }

        public Function<Path, Path> mkdirs(final Method method) {
            return path -> {
                try {
                    Paths.mkdirs(path);
                    return path;
                } catch (Exception e) {
                    throw new MkdirsFailedException(method, path, e);
                }
            };
        }

        public Function<Path, Path> noop(final Method method) {
            return path -> path;
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
            return dir.toAbsolutePath().toString();
        }
    }

    class FileWrapper {

        private final Constructor<?> constructor;
        private final Method factory;
        private Class<?> type;

        public FileWrapper(final Class<?> type) {
            this.type = type;
            this.constructor = Arrays.stream(type.getConstructors())
                    .filter(constructor -> constructor.getParameterTypes().length == 1)
                    .filter(constructor -> constructor.getParameterTypes()[0].equals(Path.class))
                    .findFirst().orElse(null);

            factory = Arrays.stream(type.getMethods())
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .filter(constructor -> constructor.getParameterTypes().length == 1)
                    .filter(constructor -> constructor.getParameterTypes()[0].equals(Path.class))
                    .filter(constructor -> constructor.getReturnType().equals(type))
                    .min(Comparator.comparing(Method::getName))
                    .orElse(null);
        }

        public boolean isWrapper() {
            return constructor != null || factory != null;
        }

        public Object wrap(final Path path) {
            if (constructor != null) {
                try {
                    return constructor.newInstance(path);
                } catch (final InvocationTargetException e) {
                    throw new ConstructorFailedException(type, constructor, path, e.getCause());
                } catch (final Exception e) {
                    throw new ConstructorFailedException(type, constructor, path, e);
                }
            }

            if (factory != null) {
                try {
                    return factory.invoke(null, path);
                } catch (final InvocationTargetException e) {
                    throw new FactoryMethodFailedException(type, factory, path, e.getCause());
                } catch (final Exception e) {
                    throw new FactoryMethodFailedException(type, factory, path, e);
                }
            }

            return new InvalidFileWrapperException(type);
        }
    }

    class MkdirFailedException extends RuntimeException {
        public MkdirFailedException(final Method method, final Path dir, final Throwable t) {
            super(String.format("@Mkdir failed%n method: %s%n path: %s", method, dir.toAbsolutePath().toString()), t);
        }
    }

    class MkdirsFailedException extends RuntimeException {
        public MkdirsFailedException(final Method method, final Path dir, final Throwable t) {
            super(String.format("@Mkdirs failed%n method: %s%n path: %s", method, dir.toAbsolutePath().toString()), t);
        }
    }

    class ConstructorFailedException extends RuntimeException {
        public ConstructorFailedException(final Class<?> clazz, final Constructor<?> constructor, final Path dir, final Throwable t) {
            super(String.format("%s construction failed%n constructor: %s%n path: %s", clazz.getSimpleName(), constructor, dir.toAbsolutePath().toString()), t);
        }
    }

    class FactoryMethodFailedException extends RuntimeException {
        public FactoryMethodFailedException(final Class<?> clazz, final Method factory, final Path dir, final Throwable t) {
            super(String.format("%s factory method failed%n method: %s%n path: %s", clazz.getSimpleName(), factory, dir.toAbsolutePath().toString()), t);
        }
    }

    class InvalidFileWrapperException extends RuntimeException {
        public InvalidFileWrapperException(final Class<?> clazz) {
            super(String.format("Type '%s' cannot be constructed.  Add a constructor or factory method similar to the following:%n" +
                            "public %s(final File file){...}%n" +
                            "public static %s from(final File file){...}",
                    clazz.getSimpleName(), clazz.getSimpleName(), clazz.getSimpleName()));
        }
    }


}
