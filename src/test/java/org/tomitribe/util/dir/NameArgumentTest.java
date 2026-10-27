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
import org.tomitribe.util.Archive;
import org.tomitribe.util.IO;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Each test documents one way a caller can pass a name argument
 * to a proxy method and what they should get back.
 */
public class NameArgumentTest {

    /**
     * Bug 1: a wrapper return type silently ignores the argument
     * and resolves to the method name instead.
     */
    @Test
    public void wrapperReturnUsesArgument() throws Exception {
        final File dir = new Archive()
                .add("orange/foo.txt", "naranja")
                .add("section/foo.txt", "wrong")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        final Wrapped wrapped = types.wrapped("orange");

        assertEquals("naranja", IO.slurp(new File(wrapped.getFile(), "foo.txt")));
    }

    /**
     * Bug 2a: a varargs argument is a String[] whose toString()
     * is an identity hash, not a name.
     */
    @Test
    public void arrayArgumentIsRejected() throws Exception {
        final File dir = new Archive().toDir();
        final Types types = Dir.of(Types.class, dir);

        try {
            types.section("a", "b");
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // pass
        }
    }

    /**
     * Bug 2b: an object that does not override toString() yields
     * a name like "NoToString@7a187f14".
     */
    @Test
    public void objectWithoutToStringIsRejected() throws Exception {
        final File dir = new Archive().toDir();
        final Types types = Dir.of(Types.class, dir);

        try {
            types.section(new NoToString());
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // pass
        }
    }

    /**
     * Bug 3: a Stream return type with an argument is treated as a
     * Dir interface and the caller receives a proxy that fails on
     * first use.
     */
    @Test
    public void streamReturnUsesArgument() throws Exception {
        final File dir = new Archive()
                .add("orange/one.txt", "")
                .add("orange/two.txt", "")
                .add("red/three.txt", "")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        final List<String> names = types.children("orange")
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        assertEquals("one.txt\ntwo.txt", String.join("\n", names));
    }

    /**
     * Bug 4: a File return type with an argument is unsupported,
     * though the same shape works on Dir.file(String).
     */
    @Test
    public void fileReturnUsesArgument() throws Exception {
        final File dir = new Archive()
                .add("orange/foo.txt", "naranja")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        final File orange = types.child("orange");

        assertEquals("naranja", IO.slurp(new File(orange, "foo.txt")));
    }

    /**
     * Bug 5: an array return type with an argument ignores the
     * argument and lists the method-name directory.
     */
    @Test
    public void arrayReturnUsesArgument() throws Exception {
        final File dir = new Archive()
                .add("orange/one.txt", "")
                .add("orange/two.txt", "")
                .add("red/three.txt", "")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        final List<String> names = Stream.of(types.list("orange"))
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        assertEquals("one.txt\ntwo.txt", String.join("\n", names));
    }

    /**
     * Bug 6: Dir.file(null) throws IllegalArgumentException but a
     * user method throws a raw NullPointerException from asName.
     */
    @Test
    public void nullArgumentIsRejected() throws Exception {
        final File dir = new Archive().toDir();
        final Types types = Dir.of(Types.class, dir);

        try {
            types.section((String) null);
            fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // pass
        }
    }

    public interface Types extends Dir {
        Section section(String name);

        Section section(String... names);

        Section section(NoToString name);

        Wrapped wrapped(String name);

        Stream<File> children(String name);

        File child(String name);

        File[] list(String name);
    }

    public interface Section extends Dir {
    }

    public static class Wrapped {
        private final File file;

        public Wrapped(final File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    public static class NoToString {
    }
}
