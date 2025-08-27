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

package org.tomitribe.util.dir;

import org.junit.Test;
import org.tomitribe.util.Files;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MkdirsTest {
    @Test
    public void test() throws Exception {
        final File dir = Files.tmpdir();

        final Project project = Dir.of(Project.class, dir);

        final Section main = project.src().main();

        assertMissing(dir, "src");
        assertMissing(dir, "src/main");
        assertMissing(dir, "src/main/java");
        assertFiles(dir, "src/main/java", main.java());
        assertExists(dir, "src/main/java");
    }

    private void assertExists(final File dir, final String expectedPath) {
        final File expected = new File(dir, expectedPath);
        assertTrue(expected.exists());
    }

    private void assertMissing(final File dir, final String expectedPath) {
        final File expected = new File(dir, expectedPath);
        assertFalse(expected.exists());
    }

    private void assertFiles(final File dir, final String expectedPath, final File actual) {
        final File expected = new File(dir, expectedPath);
        assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
    }

    public interface Project {
        Src src();

        @Mkdir
        File target();

        @Name("pom.xml")
        File pomXml();
    }

    public interface Src {
        Section main();

        Section test();

        Section section(final String name);
    }

    public interface Section extends Dir {
        @Mkdirs
        File java();

        @Mkdir
        File resources();
    }

}
