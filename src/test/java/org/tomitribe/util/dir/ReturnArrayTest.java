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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReturnArrayTest {
    @Test
    public void testArray() throws Exception {
        final File dir = Files.tmpdir();

        final List<String> strings = Arrays.asList("red", "green", "blue");
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

        final Module[] modules = parent.modules();

        for (final Module module : modules) {
            final File moduleDir = module.dir();

            assertFiles(dir, moduleDir.getName(), moduleDir);

            assertFiles(moduleDir, "src/main/java", module.src().main().java());
            assertFiles(moduleDir, "src/main/resources", module.src().main().resources());
            assertFiles(moduleDir, "src/test/java", module.src().test().java());
            assertFiles(moduleDir, "src/test/resources", module.src().test().resources());
            assertFiles(moduleDir, "pom.xml", module.pomXml());
        }

        // The pom.xml is listed as there is no filter
        assertEquals(strings.size() + 1, modules.length);
    }

    private void assertFiles(final File dir, final String expectedPath, final File actual) {
        final File expected = new File(dir, expectedPath);
        assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
    }

    public interface Module extends Dir {

        @Name("pom.xml")
        File pomXml();

        Module[] modules();

        Src src();
    }

    public interface Src {
        Section main();

        Section test();
    }

    public interface Section {
        File java();

        File resources();
    }

}