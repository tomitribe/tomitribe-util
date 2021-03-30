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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zips {
    private Zips() {
    }

    public static void unzip(final File zipFile, final File destination) throws IOException {
        unzip(zipFile, destination, false);
    }

    public static void unzip(final File zipFile, final File destination,
                             final boolean noparent) throws IOException {
        unzip(zipFile, destination, noparent, false, AcceptAll.INSTANCE);
    }

    public static void unzip(final File zipFile, final File destination,
                             final boolean noparent, final boolean flatten,
                             final Filter filter) throws IOException {
        Files.dir(destination);
        Files.writable(destination);
        Files.file(zipFile);
        Files.readable(zipFile);
        try (InputStream read = IO.read(zipFile)) {
            unzip(read, destination, noparent, flatten, filter);
        }

    }

    public static void unzip(final InputStream read, final File destination,
                             final boolean noparent) throws IOException {
        unzip(read, destination, noparent, false, AcceptAll.INSTANCE);
    }

    public static void unzip(final InputStream read, final File destination,
                             final boolean noparent, final boolean flatten,
                             final Filter filter) throws IOException {
        try {
            ZipInputStream e = new ZipInputStream(read);

            ZipEntry entry;
            while ((entry = e.getNextEntry()) != null) {
                String path = entry.getName();

                if (!filter.accept(path)) {
                    continue;
                }

                if (noparent) {
                    path = path.replaceFirst("^[^/]+/", "");
                }

                if (flatten) {
                    path = path.substring(path.lastIndexOf("/"));
                }

                File file = new File(destination, path);
                if (entry.isDirectory()) {
                    Files.mkdirs(file);
                } else {
                    Files.mkdirs(file.getParentFile());
                    IO.copy(e, file);
                    long lastModified = entry.getTime();
                    if (lastModified > 0L) {
                        file.setLastModified(lastModified);
                    }
                }
            }

        } catch (IOException var9) {
            throw new IOException("Unable to unzip " + read, var9);
        }
    }

    public interface Filter {
        boolean accept(String filename);
    }

    public static class AcceptAll implements Filter {

        public static final AcceptAll INSTANCE = new AcceptAll();

        @Override
        public boolean accept(final String filename) {
            return true;
        }
    }

    public static class KeepArchives implements Filter {

        public static final KeepArchives INSTANCE = new KeepArchives();

        @Override
        public boolean accept(final String filename) {
            return filename != null
                   && (
                       filename.endsWith(".jar") ||
                       filename.endsWith(".war") ||
                       filename.endsWith(".ear") ||
                       filename.endsWith(".rar") ||
                       filename.endsWith(".zip")
                   );
        }
    }
}
