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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("UnusedDeclaration")
public class IO {

    private IO() {
        // no-op
    }

    public static final OutputStream IGNORE_OUTPUT = new OutputStream() {
        @Override
        public void write(final int b) {
        }
    };

    public static Properties readProperties(final URL resource) throws IOException {
        return readProperties(resource, new Properties());
    }

    public static Properties readProperties(final URL resource, final Properties properties) throws IOException {
        try (InputStream read = read(resource)) {
            return readProperties(read, properties);
        }
    }

    public static Properties readProperties(final File resource) throws IOException {
        return readProperties(resource, new Properties());
    }

    public static Properties readProperties(final File resource, final Properties properties) throws IOException {
        try (final InputStream read = read(resource)) {
            return readProperties(read, properties);
        }
    }

    public static Properties readProperties(final Path resource) throws IOException {
        return readProperties(resource, new Properties());
    }

    public static Properties readProperties(final Path resource, final Properties properties) throws IOException {
        try (final InputStream read = read(resource)) {
            return readProperties(read, properties);
        }
    }

    /**
     * Reads and closes the input stream
     */
    public static Properties readProperties(final InputStream in, final Properties properties) throws IOException {
        if (in == null) throw new NullPointerException("InputStream is null");
        if (properties == null) throw new NullPointerException("Properties is null");

        try {
            properties.load(in);
        } finally {
            close(in);
        }
        return properties;
    }

    public static String readString(final URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        }
    }

    public static String readString(final File file) throws IOException {
        try (FileReader in = new FileReader(file)) {
            final BufferedReader reader = new BufferedReader(in);
            return reader.readLine();
        }
    }

    public static byte[] readBytes(final File file) throws IOException {
        try (InputStream in = read(file)) {
            return readBytes(in);
        }
    }

    public static String readString(final Path file) throws IOException {
        try (BufferedReader reader = java.nio.file.Files.newBufferedReader((file))) {
            return reader.readLine();
        }
    }

    public static byte[] readBytes(final Path file) throws IOException {
        try (InputStream in = read(file)) {
            return readBytes(in);
        }
    }

    public static byte[] readBytes(final URL url) throws IOException {
        try (InputStream in = read(url)) {
            return readBytes(in);
        }
    }

    public static byte[] readBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static String slurp(final File file) throws IOException {
        try (InputStream read = read(file)) {
            return slurp(read);
        }
    }

    public static String slurp(final Path file) throws IOException {
        try (InputStream read = read(file)) {
            return slurp(read);
        }
    }

    public static String slurp(final URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return slurp(in);
        }
    }

    public static String slurp(final InputStream in) throws IOException {
        return new String(readBytes(in));
    }

    public static void writeString(final File file, final String string) throws IOException {
        try (FileWriter out = new FileWriter(file)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(out)) {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            }
        }
    }

    public static void writeString(final Path path, final String string) throws IOException {
        try (final BufferedWriter writer = java.nio.file.Files.newBufferedWriter(path)) {
            writer.write(string);
            writer.newLine();
        }
    }

    public static void copy(final File from, final File to) throws IOException {
        if (!from.isDirectory()) {
            try (FileOutputStream fos = new FileOutputStream(to)) {
                copy(from, fos);
            }
        } else {
            copyDirectory(from, to);
        }
    }

    public static void copy(final Path from, final Path to) throws IOException {
        if (!java.nio.file.Files.isDirectory(from)) {
            try (OutputStream out = java.nio.file.Files.newOutputStream(to)) {
                copy(from, out);
            }
        } else {
            copyDirectory(from, to);
        }
    }

    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        if (srcDir == null) throw new NullPointerException("Source must not be null");
        if (destDir == null) throw new NullPointerException("Destination must not be null");
        if (!srcDir.exists()) throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        if (!srcDir.isDirectory()) throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = srcDir.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, exclusionList);
    }

    private static void doCopyDirectory(final File srcDir, final File destDir, final List<String> exclusionList)
            throws IOException {

        final File[] files = srcDir.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File file : files) {
            final File copiedFile = new File(destDir, file.getName());
            if (exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
                if (file.isDirectory()) {
                    doCopyDirectory(file, copiedFile, exclusionList);
                } else {
                    copy(file, copiedFile);
                }
            }
        }
    }

    public static void copyDirectory(final Path srcDir, final Path destDir) throws IOException {
        if (srcDir == null) throw new NullPointerException("Source must not be null");
        if (destDir == null) throw new NullPointerException("Destination must not be null");
        if (!java.nio.file.Files.exists(srcDir))
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        if (!java.nio.file.Files.isDirectory(srcDir))
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        if (java.nio.file.Files.isSameFile(srcDir, destDir)) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        List<Path> exclusionList = null;
        final Path canonicalSrc = srcDir.toRealPath();
        final Path canonicalDest = destDir.toRealPath(LinkOption.NOFOLLOW_LINKS);

        if (canonicalDest.startsWith(canonicalSrc)) {
            try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(srcDir)) {
                exclusionList = new ArrayList<>();
                for (final Path entry : stream) {
                    exclusionList.add(canonicalDest.resolve(entry.getFileName()).toRealPath());
                }
            }
        }

        doCopyDirectory(srcDir, destDir, exclusionList);
    }

    private static void doCopyDirectory(final Path srcDir, final Path destDir, final List<Path> exclusionList)
            throws IOException {

        if (!java.nio.file.Files.exists(destDir)) {
            java.nio.file.Files.createDirectories(destDir);
        } else if (!java.nio.file.Files.isDirectory(destDir)) {
            throw new IOException("Destination '" + destDir + "' exists but is not a directory");
        }

        if (!java.nio.file.Files.isWritable(destDir)) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }

        try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(srcDir)) {
            for (final Path entry : stream) {
                final Path copied = destDir.resolve(entry.getFileName());
                final Path canonicalEntry = entry.toRealPath(LinkOption.NOFOLLOW_LINKS);

                if (exclusionList == null || exclusionList.stream().noneMatch(e -> {
                    try {
                        return java.nio.file.Files.isSameFile(e, canonicalEntry);
                    } catch (final IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })) {
                    if (java.nio.file.Files.isDirectory(entry)) {
                        doCopyDirectory(entry, copied, exclusionList);
                    } else {
                        copy(entry, copied);
                    }
                }
            }
        }
    }

    public static void copy(final File from, final OutputStream to) throws IOException {
        try (InputStream read = read(from)) {
            copy(read, to);
        }
    }

    public static void copy(final Path from, final OutputStream to) throws IOException {
        try (InputStream read = read(from)) {
            copy(read, to);
        }
    }

    public static void copy(final URL from, final OutputStream to) throws IOException {
        try (InputStream read = read(from)) {
            copy(read, to);
        }
    }

    public static void copy(final String contents, final OutputStream to) throws IOException {
        try (InputStream read = read(contents)) {
            copy(read, to);
        }
    }

    public static void copy(final InputStream from, final File to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(from, write);
        }
    }

    public static void copy(final URL from, final File to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(from, write);
        }
    }

    public static void copy(final InputStream from, final File to, final boolean append) throws IOException {
        try (OutputStream write = write(to, append)) {
            copy(from, write);
        }
    }

    public static void copy(final String contents, final File to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(contents, write);
        }
    }

    public static void copy(final String contents, final File to, final boolean append) throws IOException {
        try (OutputStream write = write(to, append)) {
            copy(contents, write);
        }
    }

    public static void copy(final InputStream from, final Path to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(from, write);
        }
    }

    public static void copy(final URL from, final Path to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(from, write);
        }
    }

    public static void copy(final InputStream from, final Path to, final boolean append) throws IOException {
        try (OutputStream write = write(to, append)) {
            copy(from, write);
        }
    }

    public static void copy(final String contents, final Path to) throws IOException {
        try (OutputStream write = write(to)) {
            copy(contents, write);
        }
    }

    public static void copy(final String contents, final Path to, final boolean append) throws IOException {
        try (OutputStream write = write(to, append)) {
            copy(contents, write);
        }
    }

    public static void copy(final InputStream from, final OutputStream to) throws IOException {
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = from.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
        to.flush();
    }

    public static void copy(final byte[] from, final File to) throws IOException {
        copy(new ByteArrayInputStream(from), to);
    }

    public static void copy(final byte[] from, final OutputStream to) throws IOException {
        copy(new ByteArrayInputStream(from), to);
    }

    public static ZipOutputStream zip(final File file) throws IOException {
        final OutputStream write = write(file);
        return new ZipOutputStream(write);
    }

    public static ZipInputStream unzip(final File file) throws IOException {
        final InputStream read = read(file);
        return new ZipInputStream(read);
    }

    public static void copy(final byte[] from, final Path to) throws IOException {
        copy(new ByteArrayInputStream(from), to);
    }

    public static ZipOutputStream zip(final Path file) throws IOException {
        final OutputStream write = write(file);
        return new ZipOutputStream(write);
    }

    public static ZipInputStream unzip(final Path file) throws IOException {
        final InputStream read = read(file);
        return new ZipInputStream(read);
    }


    public static void close(final Closeable closeable) {
        if (closeable == null) return;

        try {
            if (Flushable.class.isInstance(closeable)) {
                ((Flushable) closeable).flush();
            }
        } catch (final Throwable e) {
            //Ignore
        }
        try {
            closeable.close();
        } catch (final Throwable e) {
            //Ignore
        }
    }

    public static boolean delete(final File file) {
        if (file == null) return false;

        if (!file.delete()) {
            Logger.getLogger(IO.class.getName()).log(Level.WARNING, "Delete failed on: " + file.getAbsolutePath());
            return false;
        }

        return true;
    }

    public static boolean delete(final Path path) {
        if (path == null) return false;

        try {
            return java.nio.file.Files.deleteIfExists(path);
        } catch (final IOException e) {
            Logger.getLogger(IO.class.getName()).log(Level.WARNING, "Delete failed on: " + path.toAbsolutePath(), e);
            return false;
        }
    }

    public static OutputStream write(final Path destination) throws IOException {
        final OutputStream out = java.nio.file.Files.newOutputStream(destination);
        return new BufferedOutputStream(out, 32_768);
    }

    public static OutputStream write(final File destination) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination);
        return new BufferedOutputStream(out, 32768);
    }

    public static OutputStream write(final File destination, final boolean append) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination, append);
        return new BufferedOutputStream(out, 32768);
    }

    public static OutputStream write(final Path destination, final boolean append) throws IOException {
        final OpenOption[] options = append
                ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND}
                : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

        final OutputStream out = java.nio.file.Files.newOutputStream(destination, options);
        return new BufferedOutputStream(out, 32_768);
    }

    public static PrintStream print(final File destination, final boolean append) throws FileNotFoundException {
        return print(write(destination, append));
    }

    public static PrintStream print(final File destination) throws FileNotFoundException {
        return print(write(destination));
    }

    public static PrintStream print(final Path destination, final boolean append) throws IOException {
        return print(write(destination, append));
    }

    public static PrintStream print(final Path destination) throws IOException {
        return print(write(destination));
    }

    public static PrintStream print(final OutputStream out) {
        return new PrintStream(out);
    }

    public static InputStream read(final File source) throws FileNotFoundException {
        final InputStream in = new FileInputStream(source);
        return new BufferedInputStream(in, 32768);
    }

    public static InputStream read(final Path source) throws IOException {
        final InputStream in = java.nio.file.Files.newInputStream(source);
        return new BufferedInputStream(in, 32768);
    }

    public static InputStream read(final String content) {
        return read(content.getBytes());
    }

    public static InputStream read(final String content, final String encoding) throws UnsupportedEncodingException {
        return read(content.getBytes(encoding));
    }

    public static InputStream read(final byte[] content) {
        return new ByteArrayInputStream(content);
    }

    public static InputStream read(final URL url) throws IOException {
        return url.openStream();
    }

    public static Iterable<String> readLines(final File file) throws FileNotFoundException {
        return readLines(read(file));
    }

    public static Iterable<String> readLines(final Path file) throws IOException {
        return readLines(read(file));
    }

    public static Iterable<String> readLines(final InputStream inputStream) {
        return readLines(new BufferedReader(new InputStreamReader(inputStream)));
    }

    public static Iterable<String> readLines(final BufferedReader reader) {
        return new BufferedReaderIterable(reader);
    }

    public static void copyNIO(final InputStream in, final OutputStream out) throws IOException {

        final ReadableByteChannel ic = Channels.newChannel(in);
        final WritableByteChannel oc = Channels.newChannel(out);

        try {
            copy(ic, oc);
        } finally {
            ic.close();
            oc.close();
        }
    }

    public static void copy(final ReadableByteChannel in, final WritableByteChannel out) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 1024);
        while (in.read(buffer) != -1) {
            buffer.flip();
            out.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            out.write(buffer);
        }
    }

    private static class BufferedReaderIterable implements Iterable<String> {
        private final BufferedReader reader;

        private BufferedReaderIterable(final BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public Iterator<String> iterator() {
            return new BufferedReaderIterator();
        }

        private class BufferedReaderIterator implements Iterator<String> {

            private String line;

            @Override
            // CHECKSTYLE:OFF
            public boolean hasNext() {
                try {
                    final boolean hasNext = (line = reader.readLine()) != null;
                    if (!hasNext) {
                        close(reader);
                    }
                    return hasNext;
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public String next() {
                return line;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove not supported");
            }
        }
    }
}
