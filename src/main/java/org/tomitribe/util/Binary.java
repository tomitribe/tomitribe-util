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

package org.tomitribe.util;

import java.util.BitSet;

public class Binary {

    private Binary() {
    }

    public static byte[] toBytes(String binaryString) {
        return toBitSet(binaryString).toByteArray();
    }

    public static BitSet toBitSet(String binaryString) {
        final BitSet set = new BitSet(binaryString.length());
        final StringBuilder sb = new StringBuilder(binaryString);
        for (int i = 0; i < sb.length(); i++) {
            set.set(i, '1' == sb.charAt(i));
        }
        return set;
    }

    public static String toString(byte[] bytes) {
        final BitSet set = BitSet.valueOf(bytes);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < set.length(); i++) {
            sb.append(set.get(i) ? "1" : "0");
        }
        while (sb.length() % 8 != 0) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }
}
