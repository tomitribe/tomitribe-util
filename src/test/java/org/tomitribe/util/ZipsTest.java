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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.dir.Dir;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class ZipsTest extends Assert {

    final File testClasses = JarLocation.jarLocation(ZipsTest.class);
    final File zipFile = new File(testClasses, "apache-activemq.zip");
    final File zipAssemblyFile = new File(testClasses, "apache-activemq-SNAPSHOT.zip");
    final File garbageFile = new File(testClasses, "garbage.zip");
    final File colorFile = new File(testClasses, "colors");

    @Test
    public void testUnzip() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "testUnzip");
        Zips.unzip(zipFile, testUnzip, false);
    }

    /**
     * This test complements the testUnzip.
     * We have realized that sometimes the order the entries are returned by the ZipInputStream is such as a child file
     * can be extracted before the parent file resulting in a failure while creating a directory.
     * In zips, we have fixed it by lazily creating the parents if they don't exist.
     */
    @Test
    public void testUnzipAssembly() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "testUnzipAssembly");
        Zips.unzip(zipAssemblyFile, testUnzip, false);
    }

    @Test(expected = IllegalStateException.class)
    public void notFile() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "notFile");
        Zips.unzip(colorFile, testUnzip, false);
    }

    @Test(expected = IllegalStateException.class)
    public void notExists() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "notExists");
        Zips.unzip(garbageFile, testUnzip, false);
    }

    @Test
    public void keepArchivesOnly() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "keepArchivesOnly");
        Zips.unzip(zipFile, testUnzip, false, false, Zips.KeepArchives.INSTANCE);

        final Dir result = Dir.of(Dir.class, testUnzip);
        final AtomicInteger directories = new AtomicInteger(0);
        result.walk().forEach((f) -> {
            Files.exists(f);
            if (f.isDirectory() && !f.equals(testUnzip)) {
                directories.incrementAndGet();
            }
        });

        assertTrue("Directories = " + directories.get(), directories.get() > 5); // there is at least a few nested directories
    }

    @Test
    public void keepArchivesOnlyFlattened() throws Exception {
        final File testUnzip = Files.mkdirs(testClasses.getParentFile(), "keepArchivesOnlyFlattened");
        Zips.unzip(zipFile, testUnzip, false, true, Zips.KeepArchives.INSTANCE);

        final Dir result = Dir.of(Dir.class, testUnzip);
        result.walk().forEach((f) -> {
            Files.exists(f);
            if (!f.equals(testUnzip)) { // root does not count
                Files.file(f); // will fail if at least one directory is there
            }
            Files.readable(f);
        });
    }
}
