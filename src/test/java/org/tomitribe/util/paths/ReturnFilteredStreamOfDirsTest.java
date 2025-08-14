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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.util.paths.Dir.of;

public class ReturnFilteredStreamOfDirsTest {

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

        final Stream<Module> modules = parent.modules();

        final AtomicInteger count = new AtomicInteger(0);
        modules.forEach(module -> {
            final Path moduleDir = module.get();
            System.out.println("PATH " + moduleDir.toAbsolutePath());

            assertPaths(dir, moduleDir.getFileName().toString(), moduleDir);
            assertPaths(moduleDir, "src/main/java", module.src().main().java());
            assertPaths(moduleDir, "src/main/resources", module.src().main().resources());
            assertPaths(moduleDir, "src/test/java", module.src().test().java());
            assertPaths(moduleDir, "src/test/resources", module.src().test().resources());
            assertPaths(moduleDir, "pom.xml", module.pomXml());

            assertTrue(java.nio.file.Files.exists(module.pomXml()));

            count.incrementAndGet();
        });

        // The root-level pom.xml is NOT listed
        assertEquals(strings.size(), count.get());
    }

    private void assertPaths(final Path dir, final String expectedPath, final Path actual) {
        final Path expected = dir.resolve(expectedPath).toAbsolutePath();
        assertEquals(expected, actual.toAbsolutePath());
    }

    public interface Module extends Dir {

        @Name("pom.xml")
        Path pomXml();

        @Filter(HasPomXml.class)
        Stream<Module> modules();

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

    public static class HasPomXml implements Predicate<Path> {
        @Override
        public boolean test(final Path path) {
            return java.nio.file.Files.exists(path.resolve("pom.xml"));
        }
    }
}