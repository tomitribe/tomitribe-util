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

import java.util.HashSet;
import java.util.Set;

public class HashedObjectTest extends Assert {


    @Test
    public void test() throws Exception {

        assertEquals(59, data(new Hasher() {
            @Override
            public long hash(String value) {
                return XxHash32.hash(value);
            }
        }).conflicts);


        assertEquals(0, data(new Hasher() {
            @Override
            public long hash(String value) {
                return XxHash64.hash(value);
            }
        }).conflicts);

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

    private Data data(Hasher hasher) {
        final Data data = new Data();

        data.conflicts = 0;
        data.strings = 1000000;

        final long[] longs = new long[data.strings];

        final long start = System.nanoTime();
        for (int i = 0; i < data.strings; i++) {
            final String value = "http://stackoverflow.com/questions/" + i + "/convert-from-byte-array-to-hex-string-in-java";
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
