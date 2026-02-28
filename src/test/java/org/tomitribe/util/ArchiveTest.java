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

package org.tomitribe.util;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArchiveTest {

    @Test
    public void archiveFactory() throws IOException {
        final Archive archive = Archive.archive()
                .add("hello.txt", "world");

        final File dir = archive.toDir();

        assertFile(dir, "hello.txt", "world");
    }

    @Test
    public void addBytes() throws IOException {
        final Archive archive = new Archive()
                .add("data.bin", new byte[]{1, 2, 3});

        final File dir = archive.toDir();

        final File file = new File(dir, "data.bin");
        assertTrue(file.exists());
        final byte[] bytes = IO.readBytes(file);
        assertEquals(3, bytes.length);
        assertEquals(1, bytes[0]);
        assertEquals(2, bytes[1]);
        assertEquals(3, bytes[2]);
    }

    @Test
    public void addSupplier() throws IOException {
        final Archive archive = new Archive()
                .add("lazy.txt", () -> "deferred".getBytes());

        final File dir = archive.toDir();

        assertFile(dir, "lazy.txt", "deferred");
    }

    @Test
    public void addFile() throws IOException {
        final File tmp = Files.tmpdir();
        final File source = new File(tmp, "source.txt");
        IO.copy("file-content", source);

        final Archive archive = new Archive()
                .add("copied.txt", source);

        final File dir = archive.toDir();

        assertFile(dir, "copied.txt", "file-content");
    }

    @Test
    public void addFileDirectory() throws IOException {
        final File tmp = Files.tmpdir();
        final File subdir = new File(tmp, "sub");
        subdir.mkdirs();
        IO.copy("aaa", new File(subdir, "a.txt"));
        IO.copy("bbb", new File(subdir, "b.txt"));

        final Archive archive = new Archive()
                .add("prefix", subdir);

        final File dir = archive.toDir();

        assertFile(dir, "prefix/a.txt", "aaa");
        assertFile(dir, "prefix/b.txt", "bbb");
    }

    @Test
    public void addUrl() throws IOException {
        final File tmp = Files.tmpdir();
        final File source = new File(tmp, "url-source.txt");
        IO.copy("from-url", source);

        final Archive archive = new Archive()
                .add("url.txt", source.toURI().toURL());

        final File dir = archive.toDir();

        assertFile(dir, "url.txt", "from-url");
    }

    @Test
    public void addDir() throws IOException {
        final File tmp = Files.tmpdir();
        IO.copy("root-file", new File(tmp, "root.txt"));
        final File nested = new File(tmp, "nested");
        nested.mkdirs();
        IO.copy("nested-file", new File(nested, "deep.txt"));

        final Archive archive = new Archive().addDir(tmp);

        final File dir = archive.toDir();

        assertFile(dir, "root.txt", "root-file");
        assertFile(dir, "nested/deep.txt", "nested-file");
    }

    @Test
    public void addString() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green/emerald.txt", "#50c878");

        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void addArchive() throws IOException {

        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");
        final Archive archive = new Archive();
        archive.add("colors", nested);
        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    /**
     * When adding an inner class we must also add the parent as the child
     * cannot be loaded without the parent class
     */
    @Test
    public void addInnerClass() throws IOException {

        final Archive archive = new Archive().add(Child.class);
        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("org", dir.listFiles()[0].getName());

        final File parent = new File(dir, "org/tomitribe/util/ArchiveTest.class");
        final File child = new File(dir, "org/tomitribe/util/ArchiveTest$Child.class");
        assertTrue(parent.exists());
        assertTrue(child.exists());
    }

    @Test
    public void toPath() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green/emerald.txt", "#50c878");

        final Path dir = archive.toPath();

        assertTrue(java.nio.file.Files.isDirectory(dir));
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void toPathAddString() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green/emerald.txt", "#50c878");

        final Path dir = Files.tmpdir().toPath();
        archive.toPath(dir);

        assertTrue(java.nio.file.Files.isDirectory(dir));
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void toPathAddArchive() throws IOException {

        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");
        final Archive archive = new Archive();
        archive.add("colors", nested);

        final Path dir = Files.tmpdir().toPath();
        archive.toPath(dir);

        assertTrue(java.nio.file.Files.isDirectory(dir));
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void toPathAddInnerClass() throws IOException {

        final Archive archive = new Archive().add(Child.class);

        final Path dir = Files.tmpdir().toPath();
        archive.toPath(dir);

        assertTrue(java.nio.file.Files.isDirectory(dir));

        final Path parent = dir.resolve("org/tomitribe/util/ArchiveTest.class");
        final Path child = dir.resolve("org/tomitribe/util/ArchiveTest$Child.class");
        assertTrue(java.nio.file.Files.exists(parent));
        assertTrue(java.nio.file.Files.exists(child));
    }

    @Test
    public void manifestObject() throws IOException {
        final Archive archive = new Archive()
                .manifest("Created-By", "test")
                .manifest("Bundle-Version", 42)
                .add("hello.txt", "world");

        final File dir = archive.toDir();

        final File manifestFile = new File(dir, "META-INF/MANIFEST.MF");
        assertTrue(manifestFile.exists());
        final String content = IO.slurp(manifestFile);
        assertTrue(content.contains("Created-By: test"));
        assertTrue(content.contains("Bundle-Version: 42"));
    }

    @Test
    public void manifestClass() throws IOException {
        final Archive archive = new Archive()
                .manifest("Main-Class", Child.class)
                .add("hello.txt", "world");

        final File dir = archive.toDir();

        final File manifestFile = new File(dir, "META-INF/MANIFEST.MF");
        assertTrue(manifestFile.exists());
        final String content = IO.slurp(manifestFile);
        assertTrue(content.contains("Main-Class: org.tomitribe.util.ArchiveTest$Child"));
    }

    @Test
    public void manifestNotGeneratedWhenEmpty() throws IOException {
        final Archive archive = new Archive()
                .add("hello.txt", "world");

        final File dir = archive.toDir();

        final File manifestFile = new File(dir, "META-INF/MANIFEST.MF");
        assertTrue(!manifestFile.exists());
    }

    @Test
    public void toJar() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green.txt", "emerald");

        final File jar = archive.toJar();

        assertTrue(jar.exists());
        assertTrue(jar.getName().endsWith(".jar"));

        final JarFile jarFile = new JarFile(jar);
        assertJarEntry(jarFile, "colors/red.txt", "crimson");
        assertJarEntry(jarFile, "colors/green.txt", "emerald");
        jarFile.close();
    }

    @Test
    public void toJarFile() throws IOException {
        final File target = new File(Files.tmpdir(), "custom.jar");

        final Archive archive = new Archive()
                .add("data.txt", "content");

        final File jar = archive.toJar(target);

        assertEquals(target, jar);
        assertTrue(jar.exists());

        final JarFile jarFile = new JarFile(jar);
        assertJarEntry(jarFile, "data.txt", "content");
        jarFile.close();
    }

    @Test
    public void toJarWithManifest() throws IOException {
        final Archive archive = new Archive()
                .manifest("Created-By", "test")
                .add("hello.txt", "world");

        final File jar = archive.toJar();

        final JarFile jarFile = new JarFile(jar);
        assertNotNull(jarFile.getEntry("META-INF/MANIFEST.MF"));
        assertJarEntry(jarFile, "hello.txt", "world");

        final String manifest = new String(IO.readBytes(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF"))));
        assertTrue(manifest.contains("Created-By: test"));
        jarFile.close();
    }

    @Test
    public void asJar() {
        final Archive archive = new Archive()
                .add("test.txt", "content");

        final File jar = archive.asJar();

        assertTrue(jar.exists());
        assertTrue(jar.getName().endsWith(".jar"));
    }

    @Test
    public void addJar() throws IOException {
        final Archive original = new Archive()
                .add("one.txt", "first")
                .add("two.txt", "second");

        final File jar = original.toJar();

        final Archive fromJar = new Archive().addJar(jar);
        final File dir = fromJar.toDir();

        assertFile(dir, "one.txt", "first");
        assertFile(dir, "two.txt", "second");
    }

    @Test
    public void asDir() {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green.txt", "emerald");

        final File dir = archive.asDir();

        assertTrue(dir.isDirectory());
        assertTrue(new File(dir, "colors/red.txt").exists());
        assertTrue(new File(dir, "colors/green.txt").exists());
    }

    @Test
    public void toDirFile() throws IOException {
        final File target = Files.tmpdir();

        final Archive archive = new Archive()
                .add("a.txt", "alpha")
                .add("sub/b.txt", "beta");

        archive.toDir(target);

        assertFile(target, "a.txt", "alpha");
        assertFile(target, "sub/b.txt", "beta");
    }

    @Test
    public void addArchiveMergesManifest() throws IOException {
        final Archive nested = new Archive()
                .manifest("Nested-Key", "nested-value")
                .add("file.txt", "data");

        final Archive archive = new Archive()
                .manifest("Root-Key", "root-value");
        archive.add("prefix", nested);

        final File dir = archive.toDir();

        final File manifestFile = new File(dir, "META-INF/MANIFEST.MF");
        assertTrue(manifestFile.exists());
        final String content = IO.slurp(manifestFile);
        assertTrue(content.contains("Root-Key: root-value"));
        assertTrue(content.contains("Nested-Key: nested-value"));
    }

    @Test
    public void roundTripThroughJar() throws IOException {
        final Archive original = new Archive()
                .add("a.txt", "alpha")
                .add("deep/nested/b.txt", "beta");

        final File jar = original.toJar();

        final Archive restored = new Archive().addJar(jar);
        final File dir = restored.toDir();

        assertFile(dir, "a.txt", "alpha");
        assertFile(dir, "deep/nested/b.txt", "beta");
    }

    @Test(expected = FileNotFoundException.class)
    public void toPathDirDoesNotExist() throws IOException {
        final Path dir = Files.tmpdir().toPath().resolve("does-not-exist");

        new Archive().add("test.txt", "hello").toPath(dir);
    }

    @Test(expected = FileNotFoundException.class)
    public void toPathNotADirectory() throws IOException {
        final Path file = Files.tmpdir().toPath().resolve("afile.txt");
        java.nio.file.Files.write(file, "hello".getBytes());

        new Archive().add("test.txt", "hello").toPath(file);
    }

    private static void assertFile(final File dir, final String name, final String expected) throws IOException {
        final File file = new File(dir, name);
        assertTrue(name, file.exists());
        assertEquals(expected, IO.slurp(file));
    }

    private static void assertFile(final Path dir, final String name, final String expected) throws IOException {
        final Path file = dir.resolve(name);
        assertTrue(name, java.nio.file.Files.exists(file));
        assertEquals(expected, IO.slurp(file));
    }

    private static void assertJarEntry(final JarFile jarFile, final String name, final String expected) throws IOException {
        final JarEntry entry = jarFile.getJarEntry(name);
        assertNotNull("Missing jar entry: " + name, entry);
        final String actual = new String(IO.readBytes(jarFile.getInputStream(entry)));
        assertEquals(expected, actual);
    }

    /**
     * Inner classes cannot be loaded without their parent,
     * so we must always include the parent by default
     */
    public static class Child {

    }
}
