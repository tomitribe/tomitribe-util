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

package org.tomitribe.util.dir;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.Files;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DirTest {

    @Test
    public void testDir() throws Exception {
        final File original = Files.tmpdir();
        final Dir dir = Dir.of(Dir.class, original);

        assertEquals(original.getAbsolutePath(), dir.dir().getAbsolutePath());
    }

    @Test
    public void testDirByName() throws Exception {
        final File original = Files.tmpdir();
        final Dir dir = Dir.of(Dir.class, original);

        assertEquals(original.getAbsolutePath(), dir.dir().getAbsolutePath());
        assertEquals(new File(original, "color").getAbsolutePath(), dir.dir("color").get().getAbsolutePath());
    }

    @Test
    public void testMkdir() throws Exception {
        final File original = Files.tmpdir();

        { // We can make green
            final File green = new File(original, "green");
            final Dir dir = Dir.of(Dir.class, green);
            assertEquals(green.getAbsolutePath(), dir.dir().getAbsolutePath());

            // Green does not yet exist
            assertFalse(dir.dir().exists());

            // Make it
            dir.mkdir();

            // Green should now exist
            assertTrue(dir.dir().exists());
        }

        { // Red is nested in a parent that does not exist
            // we should not be able to make red
            final File red = new File(original, "triangle/red");
            final Dir dir = Dir.of(Dir.class, red);
            assertEquals(red.getAbsolutePath(), dir.dir().getAbsolutePath());

            // Red does not yet exist
            assertFalse(dir.dir().exists());

            try {
                // Make it -- it should fail
                dir.mkdir();
                fail("RuntimeException should have been thrown");
            } catch (RuntimeException e) {
                // pass
            }

            // Red should still not exist
            assertFalse(dir.dir().exists());
        }
    }

    @Test
    public void testMkdirs() throws Exception {
        final File original = Files.tmpdir();

        { // We can make green and its parent
            final File green = new File(original, "circle/green");
            final Dir dir = Dir.of(Dir.class, green);
            assertEquals(green.getAbsolutePath(), dir.dir().getAbsolutePath());

            // Green does not yet exist
            assertFalse(dir.dir().exists());

            // Make it
            dir.mkdirs();

            // Green should now exist
            assertTrue(dir.dir().exists());
        }
    }

    /**
     * get() is a synonym for dir()
     */
    @Test
    public void testGet() throws Exception {
        final File original = Files.tmpdir();

        final File green = new File(original, "circle/green");
        final Dir dir = Dir.of(Dir.class, green);
        assertEquals(green.getAbsolutePath(), dir.get().getAbsolutePath());
    }

    @Test
    public void testParent() throws Exception {
        final File original = Files.tmpdir();

        final File green = new File(original, "circle/green");
        final Dir dir = Dir.of(Dir.class, green);
        assertEquals(green.getParentFile().getAbsolutePath(), dir.parent().getAbsolutePath());
    }

    @Test
    public void testFile() throws Exception {
        final File original = Files.tmpdir();
        assertTrue(new File(original, "color").mkdirs());
        assertTrue(Files.file(original, "pom.xml").createNewFile());
        assertTrue(Files.file(original, "color/green.txt").createNewFile());

        final Dir dir = Dir.of(Dir.class, original);

        {
            final File file = dir.file("pom.xml");
            assertTrue(file.exists());
        }
        {
            final File file = dir.file("color/green.txt");
            assertTrue(file.exists());
        }
    }

    @Test
    public void testDelete() throws Exception {
        final File original = Files.tmpdir();
        assertTrue(new File(original, "color").mkdirs());
        assertTrue(Files.file(original, "pom.xml").createNewFile());
        assertTrue(Files.file(original, "color/green.txt").createNewFile());

        final Dir dir = Dir.of(Dir.class, new File(original, "color"));
        dir.delete();

        { // pom.xml should not have been deleted
            final File file = new File(original, "pom.xml");
            assertTrue(file.exists());
        }
        { // green.txt should  have been deleted
            final File file = new File(original, "color/green.txt");
            assertFalse(file.exists());
        }
        { // color should have been deleted
            final File file = new File(original, "color");
            assertFalse(file.exists());
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

        final File original = archive.toDir();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.walk()
                .map(File::getAbsolutePath)
                .sorted()
                .map(s -> s.substring(original.getAbsolutePath().length()))
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

        final File original = archive.toDir();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.walk(3)
                .map(File::getAbsolutePath)
                .sorted()
                .map(s -> s.substring(original.getAbsolutePath().length()))
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

        final File original = archive.toDir();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.files()
                .map(File::getAbsolutePath)
                .sorted()
                .map(s -> s.substring(original.getAbsolutePath().length()))
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

        final File original = archive.toDir();
        final Dir dir = Dir.of(Dir.class, original);

        final List<String> paths = dir.files(3)
                .map(File::getAbsolutePath)
                .sorted()
                .map(s -> s.substring(original.getAbsolutePath().length()))
                .collect(Collectors.toList());

        assertEquals("/colors/green/emerald.txt\n" +
                "/colors/red.txt", Join.join("\n", paths));
    }

    @Test
    public void testToString() throws Exception {
        final File original = Files.tmpdir();

        final File color = new File(original, "color");
        final Dir dir = Dir.of(Dir.class, color);

        assertEquals(color.getAbsolutePath(), dir.toString());
    }

    @Test
    public void testEquals() throws Exception {
        final File original = Files.tmpdir();

        final Dir dirA = Dir.of(Dir.class, new File(original, "color"));
        final Dir dirB = Dir.of(Dir.class, new File(original, "color"));
        final Dir dirC = Dir.of(Dir.class, new File(original, "shape"));

        assertEquals(dirA, dirB);
        assertEquals(dirB, dirA);
        assertNotEquals(dirA, dirC);
        assertNotEquals(dirC, dirA);
        assertNotEquals(dirB, dirC);
        assertNotEquals(dirC, dirB);
    }


    @Test
    public void test() throws Exception {
        final File dir = Files.tmpdir();
        Files.mkdirs(dir, "src", "main", "java");
        Files.mkdirs(dir, "src", "main", "resources");
        Files.mkdirs(dir, "src", "test", "java");
        Files.mkdirs(dir, "src", "test", "resources");
        Files.mkdirs(dir, "target");
        Files.file(dir, "pom.xml").createNewFile();

        final Project project = Dir.of(Project.class, dir);

        assertFiles(dir, "src/main/java", project.src().main().java());
        assertFiles(dir, "src/main/resources", project.src().main().resources());
        assertFiles(dir, "src/test/java", project.src().test().java());
        assertFiles(dir, "src/test/resources", project.src().test().resources());
        assertFiles(dir, "pom.xml", project.pomXml());
        assertFiles(dir, "target", project.target());
    }

    @Test
    public void unknownSubPathes() throws Exception {
        final File dir = Files.tmpdir();
        Files.mkdirs(dir, "src", "main", "java");
        Files.mkdirs(dir, "src", "main", "resources");
        Files.mkdirs(dir, "src", "test", "java");
        Files.mkdirs(dir, "src", "test", "resources");
        Files.mkdirs(dir, "target");
        Files.file(dir, "pom.xml").createNewFile();

        final Project project = Dir.of(Project.class, dir);

        assertFiles(dir, "src/main/java", project.src().section("main").java());
        assertFiles(dir, "src/main/resources", project.src().section("main").resources());
        assertFiles(dir, "src/test/java", project.src().section("test").java());
        assertFiles(dir, "src/test/resources", project.src().section("test").resources());
        assertFiles(dir, "pom.xml", project.pomXml());
        assertFiles(dir, "target", project.target());
    }

    @Test
    public void hasDefault() throws Exception {
        final File dir = Files.tmpdir();

        final HasDefault hasDefault = Dir.of(HasDefault.class, dir);

        final long modified = hasDefault.modified();
        Assert.assertTrue(modified > 1573459973000L);
        System.out.println(modified);
    }

    private void assertFiles(final File dir, final String expectedPath, final File actual) {
        final File expected = new File(dir, expectedPath);
        assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
    }

    public interface Project {
        Src src();

        File target();

        @Name("pom.xml")
        File pomXml();
    }

    public interface Src {
        Section main();

        Section test();

        Section section(final String name);
    }

    public interface Section {
        File java();

        File resources();
    }

    public interface HasDefault extends Dir {
        File java();

        default long modified() {
            return get().lastModified();
        }
    }
}
