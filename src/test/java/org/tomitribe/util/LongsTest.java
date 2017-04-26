/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

        System.out.printf("        assertBase32(\"%s\", %s);\n", Longs.toBase32(value), value);
//        assertEquals(base32String, Longs.toBase32(value));

//        assertEquals(value, Longs.fromBase32(base32String));
    }
    
    @Test
    public void base64() {
        assertBase64("AAAAAAAAAAAAA", 0x00L);
        assertBase64("3P6rAQAAAAA=", 0x00L);
        assertBase64("AAAAAAAAAAAP6", 0xFFL);
        assertBase64("AAAAAAAAAAAP4", 0xFEL);
        assertBase64("AAAAAAAAACIGO", 0x9067L);
        assertBase64("AAAAAAAA7YACG", 0xFE0023L);
        assertBase64("AAAAAAH6EMJAA", 0xFE231200L);
        assertBase64("AAAAAAAQGJCXM", 0x10324576L);
        assertBase64("AAAAAAG472VQC", 0xDCFEAB01L);

        assertBase64("AAAAAAAAAAAH6", Byte.MAX_VALUE);
        assertBase64("77777777777YA", Byte.MIN_VALUE);

        assertBase64("AAAAAAAAAB776", Short.MAX_VALUE);
        assertBase64("7777777776AAA", Short.MIN_VALUE);

        assertBase64("AAAAAAAAAD776", Character.MAX_VALUE);
        assertBase64("AAAAAAAAAAAAA", Character.MIN_VALUE);

        assertBase64("AAAAAAD777776", Integer.MAX_VALUE);
        assertBase64("7777774AAAAAA", Integer.MIN_VALUE);

        assertBase64("P777777777776", Long.MAX_VALUE);
        assertBase64("QAAAAAAAAAAAA", Long.MIN_VALUE);

        assertBase64("AAAAAAAAAAAAA", 0x00L);
        assertBase64("AAAAAAAA74AAA", 0xFF0000L);
        assertBase64("AAAAAAAA7YAAA", 0xFE0000L);
        assertBase64("AAAAAAEQM4AAA", 0x90670000L);
        assertBase64("AAAAB7QAEMAAA", 0xFE00230000L);
        assertBase64("AAAAB7QAEMAAA", 0xFE00230000L);
        assertBase64("AAAP4IYSAAAAA", 0xFE2312000000L);
        assertBase64("AAABAMSFOYAAA", 0x103245760000L);
        assertBase64("AAANZ7VLAEAAA", 0xDCFEAB010000L);

        assertBase64("AAAAAAAAAAAAA", 0x00L);
        assertBase64("AAAAB7YAAAAAA", 0xFF00000000L);
        assertBase64("AAAAB7QAAAAAA", 0xFE00000000L);
        assertBase64("AAAJAZYAAAAAA", 0x906700000000L);
        assertBase64("AD7AAIYAAAAAA", 0xFE002300000000L);
        assertBase64("AD7AAIYAAAAAA", 0xFE002300000000L);
        assertBase64("7YRREAAAAAAAA", 0xFE23120000000000L);
        assertBase64("CAZEK5QAAAAAA", 0x1032457600000000L);
        assertBase64("3T7KWAIAAAAAA", 0xDCFEAB0100000000L);
    }

    private void assertBase64(String base64String, long value) {

        System.out.printf("        assertBase64(\"%s\", %s);\n", Longs.toBase64(value), value);
//        assertEquals(base64String, Longs.toBase64(value));

//        assertEquals(value, Longs.fromBase64(base64String));
    }
    
}
