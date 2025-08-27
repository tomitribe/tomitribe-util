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

public class LongsTest extends Assert {

    @Test
    public void hex() {
        assertHex("0000000000000000", 0x00L);
        assertHex("00000000000000ff", 0xFFL);
        assertHex("00000000000000fe", 0xFEL);
        assertHex("0000000000009067", 0x9067L);
        assertHex("0000000000fe0023", 0xFE0023L);
        assertHex("0000000000fe0023", 0xFE0023L);
        assertHex("00000000fe231200", 0xFE231200L);
        assertHex("0000000010324576", 0x10324576L);
        assertHex("00000000dcfeab01", 0xDCFEAB01L);

        assertHex("000000000000007f", Byte.MAX_VALUE);
        assertHex("ffffffffffffff80", Byte.MIN_VALUE);

        assertHex("0000000000007fff", Short.MAX_VALUE);
        assertHex("ffffffffffff8000", Short.MIN_VALUE);

        assertHex("000000000000ffff", Character.MAX_VALUE);
        assertHex("0000000000000000", Character.MIN_VALUE);

        assertHex("000000007fffffff", Integer.MAX_VALUE);
        assertHex("ffffffff80000000", Integer.MIN_VALUE);

        assertHex("7fffffffffffffff", Long.MAX_VALUE);
        assertHex("8000000000000000", Long.MIN_VALUE);

        assertHex("0000000000000000", 0x00L);
        assertHex("0000000000ff0000", 0xFF0000L);
        assertHex("0000000000fe0000", 0xFE0000L);
        assertHex("0000000090670000", 0x90670000L);
        assertHex("000000fe00230000", 0xFE00230000L);
        assertHex("000000fe00230000", 0xFE00230000L);
        assertHex("0000fe2312000000", 0xFE2312000000L);
        assertHex("0000103245760000", 0x103245760000L);
        assertHex("0000dcfeab010000", 0xDCFEAB010000L);

        assertHex("0000000000000000", 0x00L);
        assertHex("000000ff00000000", 0xFF00000000L);
        assertHex("000000fe00000000", 0xFE00000000L);
        assertHex("0000906700000000", 0x906700000000L);
        assertHex("00fe002300000000", 0xFE002300000000L);
        assertHex("00fe002300000000", 0xFE002300000000L);
        assertHex("fe23120000000000", 0xFE23120000000000L);
        assertHex("1032457600000000", 0x1032457600000000L);
        assertHex("dcfeab0100000000", 0xDCFEAB0100000000L);
    }

    private void assertHex(String hexString, long value) {

        assertEquals(hexString, Longs.toHex(value));

        assertEquals(value, Longs.fromHex(hexString));
    }

    @Test
    public void base32() {
        assertBase32("AAAAAAAAAAAAA", 0x00L);
        assertBase32("AAAAAAAAAAAP6", 0xFFL);
        assertBase32("AAAAAAAAAAAP4", 0xFEL);
        assertBase32("AAAAAAAAACIGO", 0x9067L);
        assertBase32("AAAAAAAA7YACG", 0xFE0023L);
        assertBase32("AAAAAAH6EMJAA", 0xFE231200L);
        assertBase32("AAAAAAAQGJCXM", 0x10324576L);
        assertBase32("AAAAAAG472VQC", 0xDCFEAB01L);

        assertBase32("AAAAAAAAAAAH6", Byte.MAX_VALUE);
        assertBase32("77777777777YA", Byte.MIN_VALUE);

        assertBase32("AAAAAAAAAB776", Short.MAX_VALUE);
        assertBase32("7777777776AAA", Short.MIN_VALUE);

        assertBase32("AAAAAAAAAD776", Character.MAX_VALUE);
        assertBase32("AAAAAAAAAAAAA", Character.MIN_VALUE);

        assertBase32("AAAAAAD777776", Integer.MAX_VALUE);
        assertBase32("7777774AAAAAA", Integer.MIN_VALUE);

        assertBase32("P777777777776", Long.MAX_VALUE);
        assertBase32("QAAAAAAAAAAAA", Long.MIN_VALUE);

        assertBase32("AAAAAAAAAAAAA", 0x00L);
        assertBase32("AAAAAAAA74AAA", 0xFF0000L);
        assertBase32("AAAAAAAA7YAAA", 0xFE0000L);
        assertBase32("AAAAAAEQM4AAA", 0x90670000L);
        assertBase32("AAAAB7QAEMAAA", 0xFE00230000L);
        assertBase32("AAAAB7QAEMAAA", 0xFE00230000L);
        assertBase32("AAAP4IYSAAAAA", 0xFE2312000000L);
        assertBase32("AAABAMSFOYAAA", 0x103245760000L);
        assertBase32("AAANZ7VLAEAAA", 0xDCFEAB010000L);

        assertBase32("AAAAAAAAAAAAA", 0x00L);
        assertBase32("AAAAB7YAAAAAA", 0xFF00000000L);
        assertBase32("AAAAB7QAAAAAA", 0xFE00000000L);
        assertBase32("AAAJAZYAAAAAA", 0x906700000000L);
        assertBase32("AD7AAIYAAAAAA", 0xFE002300000000L);
        assertBase32("AD7AAIYAAAAAA", 0xFE002300000000L);
        assertBase32("7YRREAAAAAAAA", 0xFE23120000000000L);
        assertBase32("CAZEK5QAAAAAA", 0x1032457600000000L);
        assertBase32("3T7KWAIAAAAAA", 0xDCFEAB0100000000L);
    }

    private void assertBase32(String base32String, long value) {

//        System.out.printf("        assertBase32(\"%s\", %s);\n", Longs.toBase32(value), value);
        assertEquals(base32String, Longs.toBase32(value));
        assertEquals(value, Longs.fromBase32(base32String));
    }
    
    @Test
    public void base64() {
        assertBase64("AAAAAAAAAAA=", 0x0000000000000000L);
        assertBase64("AAAAAAAAAAA=", 0x0000000000000000L);
        assertBase64("AAAAAAAAAP8=", 0x00000000000000ffL);
        assertBase64("AAAAAAAAAP4=", 0x00000000000000feL);
        assertBase64("AAAAAAAAkGc=", 0x0000000000009067L);
        assertBase64("AAAAAAD+ACM=", 0x0000000000fe0023L);
        assertBase64("AAAAAP4jEgA=", 0x00000000fe231200L);
        assertBase64("AAAAABAyRXY=", 0x0000000010324576L);
        assertBase64("AAAAANz+qwE=", 0x00000000dcfeab01L);
        assertBase64("AAAAAAAAAH8=", 0x000000000000007fL);
        assertBase64("/////////4A=", 0xffffffffffffff80L);
        assertBase64("AAAAAAAAf/8=", 0x0000000000007fffL);
        assertBase64("////////gAA=", 0xffffffffffff8000L);
        assertBase64("AAAAAAAA//8=", 0x000000000000ffffL);
        assertBase64("AAAAAAAAAAA=", 0x0000000000000000L);
        assertBase64("AAAAAH////8=", 0x000000007fffffffL);
        assertBase64("/////4AAAAA=", 0xffffffff80000000L);
        assertBase64("f/////////8=", 0x7fffffffffffffffL);
        assertBase64("gAAAAAAAAAA=", 0x8000000000000000L);
        assertBase64("AAAAAAAAAAA=", 0x0000000000000000L);
        assertBase64("AAAAAAD/AAA=", 0x0000000000ff0000L);
        assertBase64("AAAAAAD+AAA=", 0x0000000000fe0000L);
        assertBase64("AAAAAJBnAAA=", 0x0000000090670000L);
        assertBase64("AAAA/gAjAAA=", 0x000000fe00230000L);
        assertBase64("AAAA/gAjAAA=", 0x000000fe00230000L);
        assertBase64("AAD+IxIAAAA=", 0x0000fe2312000000L);
        assertBase64("AAAQMkV2AAA=", 0x0000103245760000L);
        assertBase64("AADc/qsBAAA=", 0x0000dcfeab010000L);
        assertBase64("AAAAAAAAAAA=", 0x0000000000000000L);
        assertBase64("AAAA/wAAAAA=", 0x000000ff00000000L);
        assertBase64("AAAA/gAAAAA=", 0x000000fe00000000L);
        assertBase64("AACQZwAAAAA=", 0x0000906700000000L);
        assertBase64("AP4AIwAAAAA=", 0x00fe002300000000L);
        assertBase64("AP4AIwAAAAA=", 0x00fe002300000000L);
        assertBase64("/iMSAAAAAAA=", 0xfe23120000000000L);
        assertBase64("EDJFdgAAAAA=", 0x1032457600000000L);
        assertBase64("3P6rAQAAAAA=", 0xdcfeab0100000000L);
    }

    private void assertBase64(String base64String, long value) {
//        System.out.printf("        assertBase64(\"%s\", 0x%sL);\n", Longs.toBase64(value), Longs.toHex(value));
        assertEquals(base64String, Longs.toBase64(value));
        assertEquals(value, Longs.fromBase64(base64String));
    }


    @Test
    public void base58() {
        assertBase58("11111111", 0x0000000000000000L);
        assertBase58("11111111", 0x0000000000000000L);
        assertBase58("11111115Q", 0x00000000000000ffL);
        assertBase58("11111115P", 0x00000000000000feL);
        assertBase58("111111BzN", 0x0000000000009067L);
        assertBase58("111112UKL6", 0x0000000000fe0023L);
        assertBase58("11117Vmdod", 0x00000000fe231200L);
        assertBase58("1111R1gsf", 0x0000000010324576L);
        assertBase58("11116edpL8", 0x00000000dcfeab01L);
        assertBase58("11111113C", 0x000000000000007fL);
        assertBase58("jpXCZedGfTD", 0xffffffffffffff80L);
        assertBase58("111111Ajx", 0x0000000000007fffL);
        assertBase58("jpXCZedGVkT", 0xffffffffffff8000L);
        assertBase58("111111LUv", 0x000000000000ffffL);
        assertBase58("11111111", 0x0000000000000000L);
        assertBase58("11114GmR58", 0x000000007fffffffL);
        assertBase58("jpXCZbMWFRH", 0xffffffff80000000L);
        assertBase58("NQm6nKp8qFC", 0x7fffffffffffffffL);
        assertBase58("NQm6nKp8qFD", 0x8000000000000000L);
        assertBase58("11111111", 0x0000000000000000L);
        assertBase58("111112UeoR", 0x0000000000ff0000L);
        assertBase58("111112UKKV", 0x0000000000fe0000L);
        assertBase58("11114h5pBZ", 0x0000000090670000L);
        assertBase58("111Vf6Db6f", 0x000000fe00230000L);
        assertBase58("111Vf6Db6f", 0x000000fe00230000L);
        assertBase58("113BZ4cxmAX", 0x0000fe2312000000L);
        assertBase58("1194njo9rP", 0x0000103245760000L);
        assertBase58("112u3qgaG4X", 0x0000dcfeab010000L);
        assertBase58("11111111", 0x0000000000000000L);
        assertBase58("111VmdYfPM", 0x000000ff00000000L);
        assertBase58("111Vf61qF5", 0x000000fe00000000L);
        assertBase58("112Eug1uKio", 0x0000906700000000L);
        assertBase58("1AdH81tSpxP", 0x00fe002300000000L);
        assertBase58("1AdH81tSpxP", 0x00fe002300000000L);
        assertBase58("jWSwKNubNpw", 0xfe23120000000000L);
        assertBase58("3i8FqdEGSDV", 0x1032457600000000L);
        assertBase58("dxvjGeG8VEw", 0xdcfeab0100000000L);
    }

    private void assertBase58(String base58String, long value) {
//        System.out.printf("        assertBase58(\"%s\", 0x%sL);\n", Longs.toBase58(value), Longs.toHex(value));
        assertEquals(base58String, Longs.toBase58(value));
        assertEquals(value, Longs.fromBase58(base58String));
    }

}
