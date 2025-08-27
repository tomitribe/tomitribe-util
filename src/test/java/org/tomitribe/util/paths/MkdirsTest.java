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

package org.tomitribe.util.paths;

import org.junit.Test;
import org.tomitribe.util.Files;

import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.notExists;
import static org.junit.Assert.assertEquals;
import static org.tomitribe.util.paths.Dir.of;

public class MkdirsTest {

    @Test
    public void test() throws Exception {
        final Path dir = Files.tmpdir().toPath();

        final Project project = of(Project.class, dir);

        final Section main = project.src().main();

        assertMissing(dir, "src");
        assertMissing(dir, "src/main");
        assertMissing(dir, "src/main/java");
        assertPaths(dir, "src/main/java", main.java());
        assertExists(dir, "src/main/java");
    }

    private void assertExists(final Path dir, final String expectedPath) {
        final Path expected = dir.resolve(expectedPath);
        assert exists(expected) : "Expected path to exist: " + expected;
    }

    private void assertMissing(final Path dir, final String expectedPath) {
        final Path expected = dir.resolve(expectedPath);
        assert notExists(expected) : "Expected path to not exist: " + expected;
    }

    private void assertPaths(final Path dir, final String expectedPath, final Path actual) {
        final Path expected = dir.resolve(expectedPath);
        assertEquals(expected.toAbsolutePath(), actual.toAbsolutePath());
    }

    public interface Project {
        Src src();

        @Mkdir
        Path target();

        @Name("pom.xml")
        Path pomXml();
    }

    public interface Src {
        Section main();

        Section test();

        Section section(final String name);
    }

    public interface Section extends Dir {
        @Mkdirs
        Path java();

        @Mkdir
        Path resources();
    }
}
