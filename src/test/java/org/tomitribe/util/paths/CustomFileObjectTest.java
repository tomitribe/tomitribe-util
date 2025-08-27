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
import org.tomitribe.util.Archive;

import java.nio.file.Path;

import static junit.framework.Assert.assertNotNull;
import static org.tomitribe.util.paths.Dir.of;

public class CustomFileObjectTest {

    @Test
    public void test() throws Exception {
        final Path dir = Archive.archive()
                .add("pom.xml", "ra{uXaiGoo4d")
                .toDir()
                .toPath();

        final Module module = of(Module.class, dir);

        assertNotNull(module.pomXml());
        assertNotNull("pom.xml", module.pomXml().getFile());
    }

    @Test
    public void array() throws Exception {
        final Path dir = Archive.archive()
                .add("pom.xml", "ra{uXaiGoo4d")
                .toDir()
                .toPath();

        final Module module = of(Module.class, dir);

        assertNotNull(module.pomXml());
        assertNotNull("pom.xml", module.pomXml().getFile());
    }

    public interface Module extends org.tomitribe.util.paths.Dir {
        @Name("pom.xml")
        Pom pomXml();
    }

    public static class Pom {
        private final Path file;

        public Pom(final Path file) {
            this.file = file;
        }

        public Path getFile() {
            return file;
        }
    }
}
