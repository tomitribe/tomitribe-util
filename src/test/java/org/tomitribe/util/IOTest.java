/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */

package org.tomitribe.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class IOTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    // ---------- readString ----------

    @Test
    public void readStringFile() throws Exception {
        final File f = tmp.newFile("a.txt");
        IO.writeString(f, "hello file");
        assertEquals("hello file", IO.readString(f));
    }

    @Test
    public void readStringPath() throws Exception {
        final Path p = tmp.newFile("b.txt").toPath();
        IO.writeString(p, "hello path");
        assertEquals("hello path", IO.readString(p));
    }

    @Test
    public void readStringUrl() throws Exception {
        final File f = tmp.newFile("c.txt");
        IO.writeString(f, "hello url");
        final URL url = f.toURI().toURL();
        assertEquals("hello url", IO.readString(url));
    }

    // ---------- readBytes ----------

    @Test
    public void readBytesFile() throws Exception {
        final File f = tmp.newFile("bin.dat");
        final byte[] bytes = "abc123".getBytes("UTF-8");
        IO.copy(bytes, f);
        assertArrayEquals(bytes, IO.readBytes(f));
    }

    @Test
    public void readBytesPath() throws Exception {
        final Path p = tmp.newFile("bin2.dat").toPath();
        final byte[] bytes = "xyz789".getBytes("UTF-8");
        IO.copy(bytes, p);
        assertArrayEquals(bytes, IO.readBytes(p));
    }

    @Test
    public void readBytesUrl() throws Exception {
        final File f = tmp.newFile("bin3.dat");
        final byte[] bytes = "fromUrl".getBytes("UTF-8");
        IO.copy(bytes, f);
        assertArrayEquals(bytes, IO.readBytes(f.toURI().toURL()));
    }

    @Test
    public void readBytesStream() throws Exception {
        final byte[] bytes = "stream!".getBytes("UTF-8");
        try (final InputStream in = new ByteArrayInputStream(bytes)) {
            assertArrayEquals(bytes, IO.readBytes(in));
        }
    }

    // ---------- slurp ----------

    @Test
    public void slurpFile() throws Exception {
        final File f = tmp.newFile("s1.txt");
        IO.copy("line1\nline2", f);
        assertEquals("line1\nline2", IO.slurp(f));
    }

    @Test
    public void slurpPath() throws Exception {
        final Path p = tmp.newFile("s2.txt").toPath();
        IO.copy("lineA\nlineB", p);
        assertEquals("lineA\nlineB", IO.slurp(p));
    }

    @Test
    public void slurpUrl() throws Exception {
        final File f = tmp.newFile("s3.txt");
        IO.copy("u1\nu2", f);
        assertEquals("u1\nu2", IO.slurp(f.toURI().toURL()));
    }

    @Test
    public void slurpStream() throws Exception {
        try (final InputStream in = new ByteArrayInputStream("in-memory\nok".getBytes("UTF-8"))) {
            assertEquals("in-memory\nok", IO.slurp(in));
        }
    }

    // ---------- append & write/copy helpers ----------

    @Test
    public void appendFile() throws Exception {
        final File f = tmp.newFile("append-file.txt");
        IO.copy("first", f);
        IO.copy(" second", f, true);
        assertEquals("first second", IO.slurp(f));
    }

    @Test
    public void appendPath() throws Exception {
        final Path p = tmp.newFile("append-path.txt").toPath();
        IO.copy("first", p);
        IO.copy(" second", p, true);
        assertEquals("first second", IO.slurp(p));
    }

    @Test
    public void copyStreamToStreamFlushes() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (final InputStream in = new ByteArrayInputStream("payload".getBytes("UTF-8"))) {
            IO.copy(in, out);
        }
        assertEquals("payload", new String(out.toByteArray(), "UTF-8"));
    }

    // ---------- Properties ----------

    @Test
    public void readPropertiesFromFileAndPathAndStream() throws Exception {
        final File f = tmp.newFile("config.properties");
        final Path p = tmp.newFile("config2.properties").toPath();

        IO.copy("a=1\nb=two\n", f);
        IO.copy("x=42\ny=yes\n", p);

        final Properties props1 = IO.readProperties(f);
        final Properties props2 = IO.readProperties(p);

        assertEquals("1", props1.getProperty("a"));
        assertEquals("two", props1.getProperty("b"));
        assertEquals("42", props2.getProperty("x"));
        assertEquals("yes", props2.getProperty("y"));

        final Properties base = new Properties();
        base.setProperty("base", "ok");
        try (final InputStream in = IO.read("c=see\n", "UTF-8")) {
            final Properties merged = IO.readProperties(in, base);
            assertEquals("ok", merged.getProperty("base"));
            assertEquals("see", merged.getProperty("c"));
        }
    }

    // ---------- readLines ----------

    @Test
    public void readLinesIterableAutoCloses() throws Exception {
        final File f = tmp.newFile("lines.txt");
        IO.copy("L1\nL2\nL3\n", f);

        final List<String> seen = new ArrayList<>();
        for (final String line : IO.readLines(f)) {
            seen.add(line);
        }
        assertEquals(3, seen.size());
        assertEquals("L1", seen.get(0));
        assertEquals("L2", seen.get(1));
        assertEquals("L3", seen.get(2));

        assertTrue("File should be deletable after iteration", f.delete() || !f.exists());
    }

    @Test
    public void readLinesBufferedReaderVariant() throws Exception {
        final File f = tmp.newFile("rr.txt");
        IO.copy("A\nB\n", f);

        try (final BufferedReader br = Files.newBufferedReader(f.toPath())) {
            final Iterable<String> it = IO.readLines(br);
            final Iterator<String> iter = it.iterator();
            assertTrue(iter.hasNext());
            assertEquals("A", iter.next());
            assertTrue(iter.hasNext());
            assertEquals("B", iter.next());
            assertFalse(iter.hasNext());
        }
    }

    // ---------- print helpers ----------

    @Test
    public void printWritesToFile() throws Exception {
        final File f = tmp.newFile("print.txt");
        try (final PrintStream ps = IO.print(f)) {
            ps.print("hello");
        }
        assertEquals("hello", IO.slurp(f));
    }

    // ---------- zip/unzip ----------

    @Test
    public void zipAndUnzipFile() throws Exception {
        final File zf = tmp.newFile("file.zip");
        try (final ZipOutputStream zos = IO.zip(zf)) {
            writeZipEntry(zos, "greet.txt", "hi");
        }
        try (final ZipInputStream zis = IO.unzip(zf)) {
            final ZipEntry e = zis.getNextEntry();
            assertNotNull(e);
            assertEquals("greet.txt", e.getName());
            final byte[] data = IO.readBytes(zis);
            assertEquals("hi", new String(data, "UTF-8"));
        }
    }

    @Test
    public void zipAndUnzipPath() throws Exception {
        final Path zp = tmp.newFile("path.zip").toPath();
        try (final ZipOutputStream zos = IO.zip(zp)) {
            writeZipEntry(zos, "x.txt", "X");
        }
        try (final ZipInputStream zis = IO.unzip(zp)) {
            final ZipEntry e = zis.getNextEntry();
            assertNotNull(e);
            assertEquals("x.txt", e.getName());
            assertEquals("X", new String(IO.readBytes(zis), "UTF-8"));
        }
    }

    // ---------- copyDirectory (File) ----------

    @Test
    public void copyDirectoryFileBasicAndExclusion() throws Exception {
        final File src = tmp.newFolder("src");
        final File dest = new File(src, "dest-inside-src");
        assertTrue(dest.mkdirs());

        final File sub = new File(src, "sub");
        final File leaf = new File(sub, "file.txt");
        assertTrue(sub.mkdirs());
        IO.copy("content", leaf);

        IO.copyDirectory(src, dest);

        final File copied = new File(new File(dest, "sub"), "file.txt");
        assertTrue(copied.exists());
        assertEquals("content", IO.slurp(copied));
    }

    @Test(expected = IOException.class)
    public void copyDirectoryFileSamePathDisallowed() throws Exception {
        final File src = tmp.newFolder("same");
        IO.copyDirectory(src, src);
    }

    // ---------- copyDirectory (Path) ----------

    @Test
    public void copyDirectoryPathBasic() throws Exception {
        final Path src = tmp.newFolder("psrc").toPath();
        final Path dest = tmp.newFolder("pdest").toPath();

        final Path sub = Files.createDirectory(src.resolve("a"));
        final Path leaf = sub.resolve("b.txt");
        IO.copy("B", leaf.toFile());

        IO.copyDirectory(src, dest);

        final Path copied = dest.resolve("a").resolve("b.txt");
        assertTrue(Files.exists(copied));
        assertEquals("B", IO.slurp(copied));
    }

    // ---------- delete ----------

    @Test
    public void deleteFile() throws Exception {
        final File f = tmp.newFile("delete-me.txt");
        IO.copy("x", f);
        assertTrue(IO.delete(f));
        assertFalse(f.exists());
    }

    @Test
    public void deletePath() throws Exception {
        final Path p = tmp.newFile("delete-me-too.txt").toPath();
        IO.copy("y", p);
        assertTrue(IO.delete(p));
        assertFalse(Files.exists(p));
    }

    // ---------- IGNORE_OUTPUT and read helpers ----------

    @Test
    public void ignoreOutputDoesNotThrow() throws Exception {
        final OutputStream out = IO.IGNORE_OUTPUT;
        for (final byte b : "anything".getBytes("UTF-8")) {
            out.write(b);
        }
        out.flush();
        out.close();
    }

    @Test
    public void readStringAndBytesFromStringAndEncoding() throws Exception {
        assertEquals("abc", IO.slurp(IO.read("abc")));
        assertEquals("å", IO.slurp(IO.read("å", "UTF-8")));
    }

    // ---------- close(Flushable/Closeable) ----------

    @Test
    public void closeFlushableThenCloseSwallowsThrowables() {
        final RecordingFlushableCloseable ok = new RecordingFlushableCloseable(false, false);
        IO.close(ok);
        assertTrue(ok.flushed);
        assertTrue(ok.closed);

        final RecordingFlushableCloseable noisy = new RecordingFlushableCloseable(true, true);
        IO.close(noisy);
        assertTrue(noisy.flushed);
        assertTrue(noisy.closed);
    }

    // ---------- static helpers & inner classes at bottom ----------

    private static void writeZipEntry(final ZipOutputStream zos, final String name, final String data) throws IOException {
        final ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(data.getBytes("UTF-8"));
        zos.closeEntry();
    }

    private static final class RecordingFlushableCloseable implements Closeable, Flushable {
        private final boolean flushThrows;
        private final boolean closeThrows;
        private boolean flushed;
        private boolean closed;

        private RecordingFlushableCloseable(final boolean flushThrows, final boolean closeThrows) {
            this.flushThrows = flushThrows;
            this.closeThrows = closeThrows;
        }

        @Override
        public void flush() throws IOException {
            flushed = true;
            if (flushThrows) {
                throw new IOException("boom flush");
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (closeThrows) {
                throw new IOException("boom close");
            }
        }
    }
}