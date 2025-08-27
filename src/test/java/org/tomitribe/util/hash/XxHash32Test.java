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

package org.tomitribe.util.hash;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.Longs;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

public class XxHash32Test extends Assert {

    final Hasher xxhash32 = new Hasher() {
        @Override
        public long hash(String value) {
            return XxHash32.hash(value);
        }
    };


    @Test
    public void shortString() {
        final Data data = data(xxhash32, 1000000, "http://host%s.foo.com");
        assertEquals(22, data.conflicts);
    }

    @Test
    public void mediumString() {
        final Data data = data(xxhash32, 1000000, "http://stackoverflow.com/questions/%s/convert-from-byte-array-to-hex-string-in-java");
        assertEquals(59, data.conflicts);
    }

    @Test
    public void longerString() {
        final Data data = data(xxhash32, 1000000, "\"Lorem ipsum\" (a.k.a. \"Lipsum\") is a popular placeholder often used in typography and web design. At its best, \"Lorem ipsum\" is a completely incomprehensible text which, to an untrained eye, may very well look like Latin. Be assured, Latin it is not. The text begins at half word as a quote from from Cicero's \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil):\n" +
                "\n" +
                "nemo enim ipsam voluptatem, quia voluptas sit,%s aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem.");
        assertEquals(118, data.conflicts);
    }

    @Test
    public void sanityCheck() {
        // expected values computed with old code

        assertEquals(0x5f627d81, XxHash32.hash("http://host%s.foo.com"));
        assertEquals(0xbd37dd97, XxHash32.hash("http://stackoverflow.com/questions/%s/convert-from-byte-array-to-hex-string-in-java"));
        assertEquals(0x9ad1f057, XxHash32.hash("\"Lorem ipsum\" (a.k.a. \"Lipsum\") is a popular placeholder often used in typography and web design. At its best, \"Lorem ipsum\" is a completely incomprehensible text which, to an untrained eye, may very well look like Latin. Be assured, Latin it is not. The text begins at half word as a quote from from Cicero's \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil):\n" +
                "\n" +
                "nemo enim ipsam voluptatem, quia voluptas sit,%s aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem."));
    }

    @Test
    public void sanityCheckStream() throws Exception {
        assertEquals(0x5f627d81, XxHash32.hash(new ByteArrayInputStream("http://host%s.foo.com".getBytes())));
        assertEquals(0xbd37dd97, XxHash32.hash(new ByteArrayInputStream("http://stackoverflow.com/questions/%s/convert-from-byte-array-to-hex-string-in-java".getBytes())));
        assertEquals(0x9ad1f057, XxHash32.hash(new ByteArrayInputStream(("\"Lorem ipsum\" (a.k.a. \"Lipsum\") is a popular placeholder often used in typography and web design. At its best, \"Lorem ipsum\" is a completely incomprehensible text which, to an untrained eye, may very well look like Latin. Be assured, Latin it is not. The text begins at half word as a quote from from Cicero's \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil):\n" +
                "\n" +
                "nemo enim ipsam voluptatem, quia voluptas sit,%s aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem.").getBytes())));
    }

    private static class Data {
        private int conflicts;
        private int strings;
        private long time;

        @Override
        public String toString() {
            return "Data{" +
                    "strings=" + strings +
                    ", time=" + (time / strings) +
                    ", conflicts=" + conflicts +
                    '}';
        }
    }

    private Data data(Hasher hasher, final int count, final String format) {
        final Data data = new Data();

        data.conflicts = 0;
        data.strings = count;

        final long[] longs = new long[data.strings];

        final long start = System.nanoTime();
        for (int i = 0; i < data.strings; i++) {
            final String value = String.format(format, i);
            longs[i] = hasher.hash(value);
        }
        data.time = System.nanoTime() - start;

        {
            final Set<String> strings = new HashSet<String>();
            for (final long l : longs) {
                final String s = Longs.toHex(l);
                if (!strings.add(s)) {
                    data.conflicts++;
                }
            }
        }
        return data;
    }

}
