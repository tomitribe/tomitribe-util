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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Join;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.tomitribe.util.paths.Dir.of;
import static org.tomitribe.util.paths.Paths.path;
import static org.tomitribe.util.paths.Paths.tmpdir;

public class DirTest {
    @Test
    public void testDir() throws Exception {
        final Path original = tmpdir();
        final Dir dir = of(Dir.class, original);

        assertEquals(original.toAbsolutePath(), dir.dir().toAbsolutePath());
    }

    @Test
    public void testDirByName() throws Exception {
        final Path original = tmpdir();
        final Dir dir = Dir.of(Dir.class, original);

        assertEquals(original.toAbsolutePath(), dir.dir().toAbsolutePath());
        assertEquals(path(original, "color").toAbsolutePath(), dir.dir("color").get().toAbsolutePath());
    }

    @Test
    public void testMkdir() throws Exception {
        final Path original = tmpdir();

        { // We can make green
            final Path green = path(original, "green");
            final Dir dir = Dir.of(Dir.class, green);
            assertEquals(green.toAbsolutePath(), dir.dir().toAbsolutePath());

            // Green does not yet exist
            assertFalse(Files.exists(dir.dir()));

            // Make it
            dir.mkdir();

            // Green should now exist
            assertTrue(Files.exists(dir.dir()));
        }

        { // Red is nested in a parent that does not exist
            // we should not be able to make red
            final Path red = path(original, "triangle", "red");
            final Dir dir = Dir.of(Dir.class, red);
            assertEquals(red.toAbsolutePath(), dir.dir().toAbsolutePath());

            // Red does not yet exist
            assertFalse(Files.exists(dir.dir()));

            try {
                // Make it -- it should fail
                dir.mkdir();
                fail("RuntimeException should have been thrown");
            } catch (RuntimeException e) {
                // pass
            }

            // Red should still not exist
            assertFalse(Files.exists(dir.dir()));
        }
    }

    @Test
    public void testMkdirs() throws Exception {
        final Path original = tmpdir();

        { // We can make green and its parent
            final Path green = path(original, "circle/green");
            final Dir dir = Dir.of(Dir.class, green);
            assertEquals(green.toAbsolutePath(), dir.dir().toAbsolutePath());

            // Green does not yet exist
            assertFalse(Files.exists(dir.dir()));

            // Make it
            dir.mkdirs();

            // Green should now exist
            assertTrue(Files.exists(dir.dir()));
        }
    }

    /**
     * get() is a synonym for dir()
     */
    @Test
    public void testGet() throws Exception {
        final Path original = tmpdir();

        final Path green = path(original, "circle", "green");
        final Dir dir = of(Dir.class, green);
        assertEquals(green.toAbsolutePath(), dir.get().toAbsolutePath());
    }

    @Test
    public void testParent() throws Exception {
        final Path original = tmpdir();

        final Path green = path(original, "circle", "green");
        final Dir dir = of(Dir.class, green);
        assertEquals(green.getParent().toAbsolutePath(), dir.parent().toAbsolutePath());
    }

    @Test
    public void testFile() throws Exception {
        final Path original = tmpdir();
        assertTrue(createDirectories(path(original, "color")).toFile().exists());
        assertTrue(createFile(path(original, "pom.xml")).toFile().exists());
        assertTrue(createFile(path(original, "color/green.txt")).toFile().exists());

        final Dir dir = of(Dir.class, original);

        {
            final Path file = dir.file("pom.xml");
            assertTrue(Files.exists(file));
        }
        {
            final Path file = dir.file("color/green.txt");
            assertTrue(Files.exists(file));
        }
    }

    @Test
    public void testDelete() throws Exception {
        final Path original = Paths.tmpdir();
        Files.createDirectories(Paths.path(original, "color"));
        Files.createFile(Paths.path(original, "pom.xml"));
        Files.createFile(Paths.path(original, "color/green.txt"));

        final Dir dir = Dir.of(Dir.class, Paths.path(original, "color"));
        dir.delete();

        { // pom.xml should not have been deleted
            final Path file = Paths.path(original, "pom.xml");
            assertTrue(Files.exists(file));
        }
        { // green.txt should have been deleted
            final Path file = Paths.path(original, "color/green.txt");
            assertFalse(Files.exists(file));
        }
        { // color should have been deleted
            final Path file = Paths.path(original, "color");
            assertFalse(Files.exists(file));
        }
    }

    @Test
    public void walk() throws IOException {
        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");

        final Archive archive = new Archive();
        archive.add("colors", nested);
        archive.add("more/depth/shades", nested);

        final Path original = archive.toDir().toPath();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.walk()
                .map(Path::toAbsolutePath)
                .sorted()
                .map(path -> path.toString().substring(original.toAbsolutePath().toString().length()))
                .collect(Collectors.toList());

        assertEquals("\n" +
                "/colors\n" +
                "/colors/green\n" +
                "/colors/green/emerald.txt\n" +
                "/colors/red.txt\n" +
                "/more\n" +
                "/more/depth\n" +
                "/more/depth/shades\n" +
                "/more/depth/shades/green\n" +
                "/more/depth/shades/green/emerald.txt\n" +
                "/more/depth/shades/red.txt", Join.join("\n", paths));
    }

    @Test
    public void walkWithDepth() throws IOException {
        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");

        final Archive archive = new Archive();
        archive.add("colors", nested);
        archive.add("more/depth/shades", nested);

        final Path original = archive.toDir().toPath();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.walk(3)
                .map(Path::toAbsolutePath)
                .sorted()
                .map(path -> path.toString().substring(original.toAbsolutePath().toString().length()))
                .collect(Collectors.toList());

        assertEquals("\n" +
                "/colors\n" +
                "/colors/green\n" +
                "/colors/green/emerald.txt\n" +
                "/colors/red.txt\n" +
                "/more\n" +
                "/more/depth\n" +
                "/more/depth/shades", Join.join("\n", paths));
    }

    @Test
    public void files() throws IOException {
        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");

        final Archive archive = new Archive();
        archive.add("colors", nested);
        archive.add("more/depth/shades", nested);

        final Path original = archive.toDir().toPath();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.files()
                .map(Path::toAbsolutePath)
                .sorted()
                .map(p -> p.toString().substring(original.toAbsolutePath().toString().length()))
                .collect(Collectors.toList());

        assertEquals("/colors/green/emerald.txt\n" +
                "/colors/red.txt\n" +
                "/more/depth/shades/green/emerald.txt\n" +
                "/more/depth/shades/red.txt", Join.join("\n", paths));
    }

    @Test
    public void filesWithDepth() throws IOException {
        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");
        final Archive archive = new Archive();
        archive.add("colors", nested);
        archive.add("more/depth/shades", nested);

        final Path original = archive.toDir().toPath();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.files(3)
                .map(Path::toAbsolutePath)
                .sorted()
                .map(p -> p.toString().substring(original.toAbsolutePath().toString().length()))
                .collect(Collectors.toList());

        assertEquals("/colors/green/emerald.txt\n" +
                "/colors/red.txt", Join.join("\n", paths));
    }

    @Test
    public void testToString() throws Exception {
        final Path original = Paths.tmpdir();

        final Path color = Paths.path(original, "color");
        final Dir dir = Dir.of(Dir.class, color);

        assertEquals(color.toAbsolutePath().toString(), dir.toString());
    }

    @Test
    public void testEquals() throws Exception {
        final Path original = Paths.tmpdir();

        final Dir dirA = Dir.of(Dir.class, Paths.path(original, "color"));
        final Dir dirB = Dir.of(Dir.class, Paths.path(original, "color"));
        final Dir dirC = Dir.of(Dir.class, Paths.path(original, "shape"));

        assertEquals(dirA, dirB);
        assertEquals(dirB, dirA);
        assertNotEquals(dirA, dirC);
        assertNotEquals(dirC, dirA);
        assertNotEquals(dirB, dirC);
        assertNotEquals(dirC, dirB);
    }

    @Test
    public void test() throws Exception {
        final Path dir = Paths.tmpdir();
        Paths.mkdirs(dir, "src", "main", "java");
        Paths.mkdirs(dir, "src", "main", "resources");
        Paths.mkdirs(dir, "src", "test", "java");
        Paths.mkdirs(dir, "src", "test", "resources");
        Paths.mkdirs(dir, "target");
        Files.createFile(Paths.path(dir, "pom.xml"));

        final Project project = Dir.of(Project.class, dir);

        assertPaths(dir, "src/main/java", project.src().main().java());
        assertPaths(dir, "src/main/resources", project.src().main().resources());
        assertPaths(dir, "src/test/java", project.src().test().java());
        assertPaths(dir, "src/test/resources", project.src().test().resources());
        assertPaths(dir, "pom.xml", project.pomXml());
        assertPaths(dir, "target", project.target());
    }

    @Test
    public void unknownSubPathes() throws Exception {
        final Path dir = Paths.tmpdir();
        Paths.mkdirs(dir, "src", "main", "java");
        Paths.mkdirs(dir, "src", "main", "resources");
        Paths.mkdirs(dir, "src", "test", "java");
        Paths.mkdirs(dir, "src", "test", "resources");
        Paths.mkdirs(dir, "target");
        Files.createFile(Paths.path(dir, "pom.xml"));

        final Project project = Dir.of(Project.class, dir);

        assertPaths(dir, "src/main/java", project.src().section("main").java());
        assertPaths(dir, "src/main/resources", project.src().section("main").resources());
        assertPaths(dir, "src/test/java", project.src().section("test").java());
        assertPaths(dir, "src/test/resources", project.src().section("test").resources());
        assertPaths(dir, "pom.xml", project.pomXml());
        assertPaths(dir, "target", project.target());
    }

    @Test
    public void hasDefault() throws Exception {
        final Path dir = Paths.tmpdir();

        final HasDefault hasDefault = Dir.of(HasDefault.class, dir);

        final long modified = hasDefault.modified();
        Assert.assertTrue(modified > 1573459973000L);
        System.out.println(modified);
    }

    private void assertPaths(final Path base, final String expectedPath, final Path actual) {
        final Path expected = base.resolve(expectedPath);
        assertEquals(expected.toAbsolutePath(), actual.toAbsolutePath());
    }

    public interface Project {
        Src src();

        Path target();

        @Name("pom.xml")
        Path pomXml();
    }

    public interface Src {
        Section main();

        Section test();

        Section section(final String name);
    }

    public interface Section {
        Path java();

        Path resources();
    }

    public interface HasDefault extends Dir {
        Path java();

        default long modified() {
            try {
                return Files.getLastModifiedTime(get()).toMillis();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
