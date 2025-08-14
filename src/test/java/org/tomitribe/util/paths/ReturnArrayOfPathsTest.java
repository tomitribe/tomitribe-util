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
package org.tomitribe.util.paths;

import org.junit.Test;
import org.tomitribe.util.Files;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.tomitribe.util.paths.Dir.of;

public class ReturnArrayOfPathsTest {

    @Test
    public void testArray() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final List<String> strings = Arrays.asList("red", "green", "blue");
        for (final String submodule : strings) {
            Paths.mkdirs(dir.resolve(submodule).resolve("src/main/java"));
            Paths.mkdirs(dir.resolve(submodule).resolve("src/main/resources"));
            Paths.mkdirs(dir.resolve(submodule).resolve("src/test/java"));
            Paths.mkdirs(dir.resolve(submodule).resolve("src/test/resources"));
            Paths.mkdirs(dir.resolve(submodule).resolve("target"));
            Paths.file(java.nio.file.Files.createFile(dir.resolve(submodule).resolve("pom.xml")));
        }

        Paths.file(java.nio.file.Files.createFile(dir.resolve("pom.xml")));

        final Module parent = of(Module.class, dir);

        final Path[] modules = parent.modules();
        for (final Path module : modules) {
            final String name = module.getFileName().toString();
            assertPaths(dir, name, module);
        }

        // The pom.xml is listed as there is no filter
        assertEquals(strings.size() + 1, modules.length);
    }

    private void assertPaths(final Path dir, final String expectedPath, final Path actual) {
        final Path expected = dir.resolve(expectedPath).toAbsolutePath();
        assertEquals(expected, actual.toAbsolutePath());
    }

    public interface Module extends Dir {
        @Name("pom.xml")
        Path pomXml();

        Path[] modules();

        Src src();
    }

    public interface Src {
        Section main();

        Section test();
    }

    public interface Section {
        Path java();

        Path resources();
    }
}