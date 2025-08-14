/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.tomitribe.util.Join;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.tomitribe.util.paths.Dir.of;

public class ReturnFilteredStreamOfPathsTest {

    @Test
    public void test() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final List<String> strings = Arrays.asList("blue", "green", "red");
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

        final Stream<Path> modules = parent.modules();

        final List<String> actual = modules
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());

        // The pom.xml is NOT listed
        assertEquals(Join.join("\n", strings), Join.join("\n", actual));
    }

    public interface Module extends Dir {

        @Filter(HasPomXml.class)
        Stream<Path> modules();
    }

    public static class HasPomXml implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            return java.nio.file.Files.exists(path.resolve("pom.xml"));
        }
    }
}