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

public class ReturnFilteredStreamOfFilesTest {
    @Test
    public void test() throws Exception {
        final File dir = Files.tmpdir();

        final List<String> strings = Arrays.asList("blue", "green", "red");
        for (final String submodule : strings) {
            Files.mkdirs(dir, submodule, "src", "main", "java");
            Files.mkdirs(dir, submodule, "src", "main", "resources");
            Files.mkdirs(dir, submodule, "src", "test", "java");
            Files.mkdirs(dir, submodule, "src", "test", "resources");
            Files.mkdirs(dir, submodule, "target");
            Files.file(dir, submodule, "pom.xml").createNewFile();
        }

        Files.file(dir, "pom.xml").createNewFile();

        final Module parent = Dir.of(Module.class, dir);

        final Stream<File> modules = parent.modules();

        final List<String> actual = modules.map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        // The pom.xml is NOT listed
        assertEquals(Join.join("\n", strings), Join.join("\n", actual));
    }

    public interface Module extends Dir {

        @Filter(HasPomXml.class)
        Stream<File> modules();
    }

    public static class HasPomXml implements FileFilter {
        @Override
        public boolean accept(final File pathname) {
            final File pom = new File(pathname, "pom.xml");
            return pom.exists();
        }
    }

}