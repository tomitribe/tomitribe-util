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

import org.junit.Assert;
import org.junit.Test;

public class HexTest extends Assert {

    @Test
    public void test() throws Exception {
        assertHex("ff", 0xFF);
        assertHex("fe", 0xFE);
        assertHex("fe231200", 0xFE, 0x23, 0x12, 0x00);
        assertHex("dcfeab0132547689", 0xDC, 0xFE, 0xAB, 0x01, 0x32, 0x54, 0x76, 0x89);

    }

    @Test
    public void testMixedCase() throws Exception {
        assertHex("fF", 0xFF);
        assertHex("Fe", 0xFE);
        assertHex("fE231200", 0xFE, 0x23, 0x12, 0x00);
        assertHex("dcfeAB0132547689", 0xDC, 0xFE, 0xAB, 0x01, 0x32, 0x54, 0x76, 0x89);

    }

    @Test
    public void testEmptyString() throws Exception {
        assertArrayEquals(new byte[0], Hex.fromString(""));
    }

    @Test
    public void testInvalidInput() throws Exception {
        try {
            Hex.fromString(null);
            fail();
        } catch (IllegalArgumentException e) {
            // pass
        }
        try {
            Hex.fromString("q");
            fail();
        } catch (Hex.InvalidHexFormatException e) {
            assertEquals("q", e.getString());
        }

        try {
            Hex.fromString("FFGF");
            fail();
        } catch (Hex.InvalidHexFormatException e) {
            assertEquals("FFGF", e.getString());
        }
    }

    private void assertHex(String hexString, int... bytes) {
        assertHex(hexString, bytes(bytes));
    }

    private void assertHex(String hexString, byte... bytes) {
        // creating HEX from bytes
        assertEquals(hexString.toLowerCase(), Hex.toString(bytes));

        // creating bytes from HEX
        assertArrayEquals(bytes, Hex.fromString(hexString));
    }

    public static byte[] bytes(final int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return bytes;
    }
}
