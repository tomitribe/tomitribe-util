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

import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.util.Hex;

import java.util.BitSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Ignore
public class BinaryTest {

    @Test
    public void testToString() {
        final byte[] bytes = {0x5, (byte) 0xFF, 0x00, 0x09};
        assertEquals("00001010000011111111000000001001", Binary.toString(bytes));
    }

    @Test
    public void test() {
        final String expectedString = "1110100001101001";
        final byte[] expectedBytes = {(byte) 0xE8, 0x69};
        final byte[] actualBytes = Binary.toBytes(expectedString);
        final String actualString = Binary.toString(actualBytes);
        assertEquals(expectedString, actualString);
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void test2() {
        final String expected = "11";
        final BitSet set = new BitSet(expected.length());
        final StringBuilder sb = new StringBuilder(expected);
        for (int i = 0; i < sb.length(); i++) set.set(i, '1' == sb.charAt(i));
        final BitSet bitSet = set;
        final StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) sb1.append(bitSet.get(i) ? "1" : "0");
        final String actual = sb1.toString();
        assertEquals(expected, actual);

        final byte[] bytes = {0x3};
        assertEquals("11", Binary.toString(bytes));
    }

    @Test
    public void testFromBinaryString() {
        final byte[] actual = Binary.toBytes("1110100001101001");
        final byte[] expected = {(byte) 0xE8, 0x69};
        final BitSet bitSet = BitSet.valueOf(expected);
        final BitSet set = new BitSet("1110100001101001".length());
        final StringBuilder sb = new StringBuilder("1110100001101001");
        for (int i = 0; i < sb.length(); i++) set.set(i, '1' == sb.charAt(i));
        assertEquals(bitSet, set);
        assertEquals(Hex.toString(expected), Hex.toString(actual));
    }
}
