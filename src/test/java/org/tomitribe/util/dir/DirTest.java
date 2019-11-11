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

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.Files;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class DirTest {
    @Test
    public void test() throws Exception {
        final File dir = Files.tmpdir();
        Files.mkdirs(dir, "src", "main", "java");
        Files.mkdirs(dir, "src", "main", "resources");
        Files.mkdirs(dir, "src", "test", "java");
        Files.mkdirs(dir, "src", "test", "resources");
        Files.mkdirs(dir, "target");
        Files.file(dir, "pom.xml").createNewFile();

        final Project project = Dir.of(Project.class, dir);

        assertFiles(dir, "src/main/java", project.src().main().java());
        assertFiles(dir, "src/main/resources", project.src().main().resources());
        assertFiles(dir, "src/test/java", project.src().test().java());
        assertFiles(dir, "src/test/resources", project.src().test().resources());
        assertFiles(dir, "pom.xml", project.pomXml());
        assertFiles(dir, "target", project.target());
    }

    @Test
    public void unknownSubPathes() throws Exception {
        final File dir = Files.tmpdir();
        Files.mkdirs(dir, "src", "main", "java");
        Files.mkdirs(dir, "src", "main", "resources");
        Files.mkdirs(dir, "src", "test", "java");
        Files.mkdirs(dir, "src", "test", "resources");
        Files.mkdirs(dir, "target");
        Files.file(dir, "pom.xml").createNewFile();

        final Project project = Dir.of(Project.class, dir);

        assertFiles(dir, "src/main/java", project.src().section("main").java());
        assertFiles(dir, "src/main/resources", project.src().section("main").resources());
        assertFiles(dir, "src/test/java", project.src().section("test").java());
        assertFiles(dir, "src/test/resources", project.src().section("test").resources());
        assertFiles(dir, "pom.xml", project.pomXml());
        assertFiles(dir, "target", project.target());
    }

    @Test
    public void hasDefault() throws Exception {
        final File dir = Files.tmpdir();

        final HasDefault hasDefault = Dir.of(HasDefault.class, dir);

        final long modified = hasDefault.modified();
        Assert.assertTrue(modified > 1573459973000L);
        System.out.println(modified);
    }

    private void assertFiles(final File dir, final String expectedPath, final File actual) {
        final File expected = new File(dir, expectedPath);
        assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
    }

    public interface Project {
        Src src();

        File target();

        @Name("pom.xml")
        File pomXml();
    }

    public interface Src {
        Section main();

        Section test();

        Section section(final String name);
    }

    public interface Section {
        File java();

        File resources();
    }

    public interface HasDefault extends Dir {
        File java();

        default long modified() {
            return get().lastModified();
        }
    }
}