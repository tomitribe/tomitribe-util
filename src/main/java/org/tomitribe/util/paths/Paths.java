/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.util.paths;

import org.tomitribe.util.collect.AbstractIterator;
import org.tomitribe.util.collect.FilteredIterator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Paths {

    private static final DeleteOnExit DELETE_ON_EXIT = new DeleteOnExit();


    private Paths() {
        // no-op
    }

    public static Path path(final String... parts) {
        Path path = null;
        for (final String part : parts) {
            if (path == null) {
                path = java.nio.file.Paths.get(part);
            } else {
                path = path.resolve(part);
            }
        }
        return path;
    }

    public static Path path(Path dir, final String... parts) {
        for (final String part : parts) {
            dir = dir.resolve(part);
        }
        return dir;
    }

    public static List<Path> collect(final Path dir) {
        try (final Stream<Path> stream = Files.walk(dir).filter(path -> !path.equals(dir))) {
            return stream.collect(Collectors.toList());
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to collect paths from: " + dir, e);
        }
    }

    public static List<Path> collect(final Path dir, final String regex) {
        return collect(dir, Pattern.compile(regex));
    }

    public static List<Path> collect(final Path dir, final Pattern pattern) {
        try (final Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(p -> pattern.matcher(p.toString()).matches())
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to collect paths from: " + dir, e);
        }
    }

    public static boolean visit(final Path dir, final Visitor visitor) {
        return visit(dir, new Predicate<Path>() {
            @Override
            public boolean test(final Path path) {
                return true;
            }
        }, visitor);
    }


    public static boolean visit(final Path dir, final String regex, final Visitor visitor) {
        return visit(dir, Pattern.compile(regex), visitor);
    }

    public static boolean visit(final Path dir, final Pattern pattern, final Visitor visitor) {
        final Predicate<Path> filter = path -> true;

        final Predicate<Path> matcher = path -> {
            try {
                return java.nio.file.Files.isRegularFile(path) &&
                        pattern.matcher(path.toAbsolutePath().toString()).matches();
            } catch (Exception e) {
                return false;
            }
        };

        return visit(dir, filter, path -> {
            if (matcher.test(path)) {
                visitor.visit(path);
            }
            return true;
        });
    }

    public static boolean visit(final Path dir, final Predicate<Path> filter, final Visitor visitor) {
        try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir)) {
            for (final Path path : stream) {
                if (!filter.test(path)) return false;
                if (!visitor.visit(path)) return false;
                if (java.nio.file.Files.isDirectory(path)) {
                    if (!visit(path, filter, visitor)) return false;
                }
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to visit paths under: " + dir, e);
        }

        return true;
    }

    public static Iterable<Path> iterate(final Path dir) {
        return iterate(dir, path -> true);
    }

    public static Iterable<Path> iterate(final Path dir, final String regex) {
        return iterate(dir, Pattern.compile(regex));
    }

    public static Iterable<Path> iterate(final Path dir, final Pattern pattern) {
        return iterate(dir, path -> pattern.matcher(path.toString()).matches());
    }

    public static Iterable<Path> iterate(final Path dir, final Predicate<Path> filter) {
        return new Iterable<Path>() {
            @Override
            public Iterator<Path> iterator() {
                return new FilteredIterator<Path>(new RecursivePathIterator(dir), new FilteredIterator.Filter<Path>() {
                    @Override
                    public boolean accept(final Path file) {
                        return filter.test(file);
                    }
                });
            }
        };
    }

    public static Path[] listFiles(final Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Path> list = new ArrayList<>();
            for (Path path : stream) {
                list.add(path);
            }
            return list.toArray(new Path[0]);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class PathIterator extends AbstractIterator<Path> {
        private final Path[] files;
        private int index;

        private PathIterator(final Path dir) {
            dir(dir);
            this.files = listFiles(dir);
            this.index = 0;
        }

        @Override
        protected Path advance() throws NoSuchElementException {
            if (index >= files.length) {
                return null;
            }
            return files[index++];
        }
    }

    private static class RecursivePathIterator extends AbstractIterator<Path> {
        private final LinkedList<PathIterator> stack = new LinkedList<>();

        public RecursivePathIterator(final Path base) {
            stack.add(new PathIterator(base));
        }

        @Override
        protected Path advance() throws NoSuchElementException {

            final PathIterator current = stack.element();

            try {
                final Path path = current.advance();

                if (path == null) {
                    stack.pop();
                    return advance();
                }

                if (Files.isDirectory(path)) {
                    stack.push(new PathIterator(path));
                }

                return path;
            } catch (final NoSuchElementException e) {
                stack.pop();
                return advance();
            }
        }
    }

    public static void exists(final Path path, final String message) {
        if (!Files.exists(path)) {
            throw new IllegalStateException(message + " does not exist: " + path.toAbsolutePath());
        }
    }

    public static void exists(final Path path) {
        exists(path, "Path");
    }

    public static void dir(final Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("Not a directory: " + path.toAbsolutePath());
        }
    }

    public static void file(final Path path) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Not a file: " + path.toAbsolutePath());
        }
    }

    public static void writable(final Path path) {
        if (!Files.isWritable(path)) {
            throw new IllegalStateException("Not writable: " + path.toAbsolutePath());
        }
    }

    public static void readable(final Path path) {
        if (!Files.isReadable(path)) {
            throw new IllegalStateException("Not readable: " + path.toAbsolutePath());
        }
    }

    public static Path rename(final Path from, final Path to) {
        try {
            return Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not rename " + from + " to " + to, e);
        }
    }

    public static void remove(final Path path) {
        if (path == null || !Files.exists(path)) return;

        if (Files.isDirectory(path)) {
            delete(path);
        } else {
            try {
                Files.delete(path);
            } catch (final Exception e) {
                throw new IllegalStateException("Could not delete file: " + path.toAbsolutePath(), e);
            }
        }
    }

    public static void delete(final Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        Files.delete(file);
                    } catch (final Exception e) {
                        throw new RuntimeException("Failed to delete file: " + file, e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        Files.delete(dir);
                    } catch (final Exception e) {
                        throw new RuntimeException("Failed to delete dir: " + dir, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final Exception e) {
            throw new IllegalStateException("Could not delete directory: " + dir.toAbsolutePath(), e);
        }
    }

    public static void mkdir(final Path path) {
        if (Files.exists(path)) {
            dir(path);
            return;
        }
        try {
            Files.createDirectory(path);
        } catch (final Exception e) {
            throw new RuntimeException("Cannot mkdir: " + path.toAbsolutePath(), e);
        }
    }

    public static void mkdirs(final Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (final Exception e) {
                throw new RuntimeException("Cannot mkdirs: " + path.toAbsolutePath(), e);
            }
        } else {
            dir(path);
        }
    }

    public static Path mkdirs(final Path dir, final String... parts) {
        final Path path = path(dir, parts);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new RuntimeException("Cannot mkdirs: " + path.toAbsolutePath(), e);
            }
        } else {
            dir(path);
        }

        return path;
    }

    public static Path mkparent(final Path path) {
        mkdirs(path.getParent());
        return path;
    }


    public static Path tmpdir() {
        return tmpdir(true);
    }

    public static Path tmpdir(final boolean deleteOnExit) {
        try {
            final Path file = Files.createTempFile("temp", "dir");

            Files.delete(file); // convert temp file into a directory
            mkdir(file);

            if (deleteOnExit) {
                DELETE_ON_EXIT.clean(file);
            }

            return file;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
    }

    public static Path resolve(final Path base, final Path child) {
        if (child == null) {
            throw new IllegalArgumentException("path is null");
        }
        if (child.isAbsolute()) {
            return child;
        }
        if (base == null) {
            throw new IllegalArgumentException("absolutePath is null");
        }
        if (!base.isAbsolute()) {
            throw new IllegalArgumentException("absolutePath is not absolute: " + base);
        }
        return base.resolve(child);
    }

    public static void absolute(final Path path) {
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("absolutePath is not absolute: " + path);
        }
    }

    public static String format(double size) {
        if (size < 1024) return String.format("%.0f B", size);
        if ((size /= 1024) < 1024) return String.format("%.0f KB", size);
        if ((size /= 1024) < 1024) return String.format("%.0f MB", size);
        if ((size /= 1024) < 1024) return String.format("%.1f GB", size);
        if ((size /= 1024) < 1024) return String.format("%.1f TB", size);
        return "unknown";
    }

    public interface Visitor {
        boolean visit(Path path);
    }

    public static class DeleteOnExit {
        private final List<Path> files = new ArrayList<>();

        public DeleteOnExit() {
            Runtime.getRuntime().addShutdownHook(new Thread(this::clean));
        }

        public Path clean(final Path path) {
            this.files.add(path);
            return path;
        }

        public void clean() {
            files.forEach(this::delete);
        }

        private void delete(final Path path) {
            try {
                Files.walkFileTree(path, new RecursiveDelete());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        private static class RecursiveDelete implements FileVisitor<Path> {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                java.nio.file.Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                java.nio.file.Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        }
    }
}
