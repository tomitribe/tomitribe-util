/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.dir;

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.util.Files;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WalkTest {

    private File dir;
    private List<String> paths;
    private List<String> expected;
    private Module module;

    @Before
    public void setup() throws Exception {
        dir = Files.tmpdir();

        paths = Arrays.asList(
                "src/main/java/io/superbiz/colors/Red.java",
                "src/main/java/io/superbiz/colors/Green.java",
                "src/main/java/io/superbiz/colors/Blue.java",
                "src/test/java/io/superbiz/colors/RedTest.java",
                "src/test/java/io/superbiz/colors/GreenTest.java",
                "src/test/java/io/superbiz/colors/BlueTest.java"
        );

        for (final String path : paths) {
            final File file = new File(dir, path);
            Files.mkparent(file);
            assertTrue(file.createNewFile());
        }

        module = Dir.of(Module.class, dir);

        expected = paths.stream()
                .map(s -> new File(dir, s))
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testStreamOfFiles() throws Exception {

        final List<String> actual = module.streamOfFiles()
                .map(File.class::cast) // explicitly verify type
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testStream() throws Exception {

        final List<String> actual = module.streamOfJava()
                .map(Java.class::cast) // explicitly verify type
                .map(Dir::dir)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArrayOfFiles() throws Exception {

        final List<String> actual = Stream.of(module.arrayOfFiles())
                .map(File.class::cast) // explicitly verify type
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    @Test
    public void testArray() throws Exception {

        final List<String> actual = Stream.of(module.arrayOfJava())
                .map(Java.class::cast) // explicitly verify type
                .map(Dir::dir)
                .map(File::getAbsolutePath)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Join.join("\n", expected), Join.join("\n", actual));
    }

    public interface Module extends Dir {

        @Walk
        @Filter(IsJava.class)
        Stream<File> streamOfFiles();

        @Walk
        @Filter(IsJava.class)
        Stream<Java> streamOfJava();

        @Walk
        @Filter(IsJava.class)
        File[] arrayOfFiles();

        @Walk
        @Filter(IsJava.class)
        Java[] arrayOfJava();

    }

    public interface Java extends Dir {

    }

    public static class IsJava implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getName().endsWith(".java");
        }
    }

}