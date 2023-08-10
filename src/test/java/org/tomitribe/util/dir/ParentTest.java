/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.util.dir;

import org.junit.Test;
import org.tomitribe.util.Archive;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

public class ParentTest {


    @Test
    public void test() throws Exception {
        final File dir = Archive.archive()
                .add("src/main/java/org/example/Foo.java", "")
                .add("pom.xml", "")
                .toDir();

        final Module module = Dir.of(Module.class, dir);

        final Src src = module.src();
        final Module parent = src.module();

        assertEquals(module.get().getAbsolutePath(), parent.get().getAbsolutePath());
    }

    @Test
    public void depth() throws Exception {
        final File dir = Archive.archive()
                .add("src/main/java/org/example/Foo.java", "")
                .add("pom.xml", "")
                .toDir();

        final Module module = Dir.of(Module.class, dir);

        final Src src = module.src();
        final Section main = src.main();
        final Java java = main.java();

        final String expected = module.get().getAbsolutePath();

        assertEquals(expected, src.module().get().getAbsolutePath());
        assertEquals(expected, main.module().get().getAbsolutePath());
        assertEquals(expected, java.module().get().getAbsolutePath());
    }


    public interface Module extends Dir {

        @Name("pom.xml")
        File pomXml();

        Src src();
    }

    public interface Src {

        @Parent
        Module module();

        Section main();

        Section test();
    }

    public interface Section {
        @Parent(2)
        Module module();

        Java java();

        File resources();
    }

    public interface Java extends Dir {

        @Parent(3)
        Module module();
    }

}
