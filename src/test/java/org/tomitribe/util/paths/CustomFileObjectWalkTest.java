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

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.util.paths.Dir.of;

public class CustomFileObjectWalkTest {

    private Path dir;
    private List<String> paths;
    private List<String> expected;
    private Module module;

    @Before
    public void setup() throws Exception {
        dir = Paths.tmpdir();

        paths = Arrays.asList(
                "src/main/java/io/superbiz/colors/Red.java",
                "src/main/java/io/superbiz/colors/Green.java",
                "src/main/java/io/superbiz/colors/Blue.java",
                "src/test/java/io/superbiz/colors/RedTest.java",
                "src/test/java/io/superbiz/colors/GreenTest.java",
                "src/test/java/io/superbiz/colors/BlueTest.java"
        );

        for (final String path : paths) {
            final Path file = dir.resolve(path);
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }

        module = of(Module.class, dir);

        expected = paths.stream()
                .map(dir::resolve)
                .map(p -> p.toAbsolutePath().toString())
                .sorted()
                .collect(toList());
    }

    @Test
    public void testStreamOfFiles() {
        final List<String> actual = module.streamOfFiles()
                .map(Archivo.class::cast)
                .map(Archivo::getAbsolutePath)
                .sorted()
                .collect(toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testStream() {
        final List<String> actual = module.streamOfJava()
                .map(Java.class::cast)
                .map(Dir::dir)
                .map(p -> p.toAbsolutePath().toString())
                .sorted()
                .collect(toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArrayOfFiles() {
        final Archivo[] values = module.arrayOfFiles();
        final List<String> actual = Stream.of(values)
                .map(Archivo.class::cast)
                .map(Archivo::getAbsolutePath)
                .sorted()
                .collect(toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArray() {
        final List<String> actual = Stream.of(module.arrayOfJava())
                .map(Java.class::cast)
                .map(Dir::dir)
                .map(p -> p.toAbsolutePath().toString())
                .sorted()
                .collect(toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    public interface Module extends Dir {

        @Walk
        @Filter(IsJava.class)
        Stream<Archivo> streamOfFiles();

        @Walk
        @Filter(IsJava.class)
        Stream<Java> streamOfJava();

        @Walk
        @Filter(IsJava.class)
        Archivo[] arrayOfFiles();

        @Walk
        @Filter(IsJava.class)
        Java[] arrayOfJava();
    }

    public interface Java extends Dir {
    }

    public static class IsJava implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            final Path name = path.getFileName();
            return name != null && name.toString().endsWith(".java");
        }
    }

    public static class Archivo {
        private final Path path;

        private Archivo(final Path path, final boolean ignored) {
            this.path = path;
        }

        public String getName() {
            return path.getFileName().toString();
        }

        public boolean isDirectory() {
            return Files.isDirectory(path);
        }

        public String getAbsolutePath() {
            return path.toAbsolutePath().toString();
        }

        public static Archivo from(final Path path) {
            return new Archivo(path, false);
        }
    }
} 
