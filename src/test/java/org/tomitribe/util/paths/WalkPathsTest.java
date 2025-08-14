/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.paths;

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Files;
import org.tomitribe.util.Join;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.util.paths.Dir.of;

public class WalkPathsTest {

    private Path dir;
    private List<String> paths;
    private List<String> expected;
    private Module module;

    @Before
    public void setup() throws Exception {
        dir = Files.tmpdir().toPath();

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
            Files.mkparent(file.toFile());
            assertTrue(file.toFile().createNewFile());
        }

        module = of(Module.class, dir);

        expected = paths.stream()
                .map(dir::resolve)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testStreamOfPaths() {
        final List<String> actual = module.streamOfFiles()
                .map(Path.class::cast)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testStream() {
        final List<String> actual = module.streamOfJava()
                .map(Java.class::cast)
                .map(Dir::get)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArrayOfPaths() {
        final List<String> actual = of(module.arrayOfFiles())
                .map(Path.class::cast)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArray() {
        final List<String> actual = of(module.arrayOfJava())
                .map(Java.class::cast)
                .map(Dir::get)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    private String path(final Path path) {
        if (path.toFile().isDirectory()) return path.toAbsolutePath().toString() + "/";
        return path.toAbsolutePath().toString();
    }

    private List<String> paths(final Path base, final List<Path> paths) {
        final int trim = path(base).length() - 1;
        return paths.stream()
                .map(this::path)
                .map(s -> s.substring(trim))
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void walk() throws IOException {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir().toPath();

        final Work work = of(Work.class, dir);
        final List<Path> list = work.nofilter().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("" +
                "/\n" +
                "/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    @Test
    public void maxDepthOne() throws IOException {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.maxOne().collect(Collectors.toList());
        assertEquals("/\n/repository/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void maxDepthTwo() throws IOException {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.maxTwo().collect(Collectors.toList());
        assertEquals("/\n" +
                "/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void minDepthOne() throws IOException {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.minOne().collect(Collectors.toList());
        assertEquals("/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void minDepthTwo() throws IOException {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.minTwo().collect(Collectors.toList());
        assertEquals("/repository/io.tomitribe/\n" +
                "/repository/io.tomitribe/crest/\n" +
                "/repository/io.tomitribe/crest/5/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/\n" +
                "/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/\n" +
                "/repository/junit/junit/\n" +
                "/repository/junit/junit/4/\n" +
                "/repository/junit/junit/4/4.12/\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color.bright/green/\n" +
                "/repository/org.color.bright/green/1/\n" +
                "/repository/org.color.bright/green/1/1.4/\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/\n" +
                "/repository/org.color/red/\n" +
                "/repository/org.color/red/1/\n" +
                "/repository/org.color/red/1/1.4/\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths(dir, list)));
    }

    @Test
    public void minOneMaxTwo() throws Exception {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.minOneMaxTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/\n" +
                "/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths));
    }

    @Test
    public void minTwoMaxTwo() throws Exception {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.minTwoMaxTwo().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/io.tomitribe/\n" +
                "/repository/junit/\n" +
                "/repository/org.color.bright/\n" +
                "/repository/org.color/", Join.join("\n", paths));
    }

    @Test
    public void files() throws Exception {
        final Path dir = new Archive()
                .add("repository/org.color/red/1/1.4/foo.txt", "")
                .add("repository/org.color.bright/green/1/1.4/foo.txt", "")
                .add("repository/junit/junit/4/4.12/bar.txt", "")
                .add("repository/io.tomitribe/crest/5/5.4.1.2/baz.txt", "")
                .toDir()
                .toPath();

        final Work work = Dir.of(Work.class, dir);
        final List<Path> list = work.files().collect(Collectors.toList());

        final List<String> paths = paths(dir, list);

        assertEquals("/repository/io.tomitribe/crest/5/5.4.1.2/baz.txt\n" +
                "/repository/junit/junit/4/4.12/bar.txt\n" +
                "/repository/org.color.bright/green/1/1.4/foo.txt\n" +
                "/repository/org.color/red/1/1.4/foo.txt", Join.join("\n", paths));
    }

    public interface Work extends Dir {
        @Walk
        Stream<Path> nofilter();

        @Walk(maxDepth = 1)
        Stream<Path> maxOne();

        @Walk(maxDepth = 2)
        Stream<Path> maxTwo();

        @Walk(minDepth = 1)
        Stream<Path> minOne();

        @Walk(minDepth = 2)
        Stream<Path> minTwo();

        @Walk(minDepth = 1, maxDepth = 2)
        Stream<Path> minOneMaxTwo();

        @Walk(minDepth = 2, maxDepth = 2)
        Stream<Path> minTwoMaxTwo();
    }

    public interface Module extends Dir {
        @Walk
        @Filter(IsJava.class)
        Stream<Path> streamOfFiles();

        @Walk
        @Filter(IsJava.class)
        Stream<Java> streamOfJava();

        @Walk
        @Filter(IsJava.class)
        Path[] arrayOfFiles();

        @Walk
        @Filter(IsJava.class)
        Java[] arrayOfJava();
    }

    public interface Java extends Dir {
    }

    public static class IsJava implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            return path.getFileName().toString().endsWith(".java");
        }
    }
}