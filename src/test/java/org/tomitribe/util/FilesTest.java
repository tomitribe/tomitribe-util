/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.tomitribe.util.Join.join;

public class FilesTest extends Assert {

    final File testClasses = JarLocation.jarLocation(FilesTest.class);
    final File colors = new File(testClasses, "colors");

    @Test
    public void testCollect() throws Exception {

        final List<File> files = Files.collect(colors);
        assertAll(testClasses, files);
    }

    @Test
    public void testVisit() throws Exception {

        final List<File> files = new ArrayList<File>();
        Files.visit(colors, new Files.Visitor() {
            @Override
            public boolean visit(final File file) {
                files.add(file);
                return true;
            }
        });
        assertAll(testClasses, files);
    }

    @Test
    public void testIterate() throws Exception {

        final List<File> files = new ArrayList<File>();
        for (final File file : Files.iterate(colors)) {
            files.add(file);
        }

        assertAll(testClasses, files);
    }

    @Test
    public void testCollectFiltered() throws Exception {

        final List<File> files = Files.collect(colors, ".*.txt");
        assertTxtFiles(testClasses, files);
    }


    @Test
    public void testVisitFiltered() throws Exception {

        final List<File> files = new ArrayList<File>();
        Files.visit(colors, ".*\\.txt", new Files.Visitor() {
            @Override
            public boolean visit(final File file) {
                files.add(file);
                return true;
            }
        });
        assertTxtFiles(testClasses, files);
    }

    @Test
    public void testIterateFiltered() throws Exception {

        final List<File> files = new ArrayList<File>();
        for (final File file : Files.iterate(colors, ".*\\.txt")) {
            files.add(file);
        }

        assertTxtFiles(testClasses, files);
    }

    private String[] relativize(final File base, final List<File> files) {

        final List<String> list = new ArrayList<String>();
        final int parent = base.getAbsolutePath().length();
        for (final File file : files) {
            final String absolutePath = file.getAbsolutePath();
            final String relativePath = absolutePath.substring(parent).replace('\\', '/');
            list.add(relativePath);
        }
        return list.toArray(new String[list.size()]);
    }


    private void assertTxtFiles(final File testClasses, final List<File> files) {
        final String[] actual = relativize(testClasses, files);

        final String[] expected = {
                "/colors/blue/midnight.txt",
                "/colors/blue/pastel/powder.txt",
                "/colors/orange.txt",
                "/colors/other/cmyk/magenta.txt",
                "/colors/other/cmyk/yellow.txt",
                "/colors/other/hsb/hue.txt",
                "/colors/red/crimson.txt"
        };

        Arrays.sort(actual);
        Arrays.sort(expected);
        assertEquals(join("\n", expected), join("\n", actual));
    }


    private void assertAll(final File testClasses, final List<File> files) {
        final String[] actual = relativize(testClasses, files);

        final String[] expected = {
                "/colors/blue",
                "/colors/blue/midnight.txt",
                "/colors/blue/navy.csv",
                "/colors/blue/pastel",
                "/colors/blue/pastel/powder.txt",
                "/colors/green",
                "/colors/green/emerald.csv",
                "/colors/orange.txt",
                "/colors/other",
                "/colors/other/cmyk",
                "/colors/other/cmyk/cyan.csv",
                "/colors/other/cmyk/key.sh",
                "/colors/other/cmyk/magenta.txt",
                "/colors/other/cmyk/yellow.txt",
                "/colors/other/hsb",
                "/colors/other/hsb/brightness.csv",
                "/colors/other/hsb/hue.txt",
                "/colors/other/hsb/saturation",
                "/colors/red",
                "/colors/red/crimson.txt"
        };

        Arrays.sort(actual);
        Arrays.sort(expected);
        assertEquals(join("\n", expected), join("\n", actual));
    }
}
