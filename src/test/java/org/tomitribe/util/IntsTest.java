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

public class IntsTest extends Assert {

    @Test
    public void hex() {
        assertHex("00000000", 0x00);
        assertHex("000000ff", 0xFF);
        assertHex("000000fe", 0xFE);
        assertHex("00009067", 0x9067);
        assertHex("00fe0023", 0xFE0023);
        assertHex("fe231200", 0xFE231200);
        assertHex("10324576", 0x10324576);
        assertHex("dcfeab01", 0xDCFEAB01);

        assertHex("0000007f", Byte.MAX_VALUE);
        assertHex("ffffff80", Byte.MIN_VALUE);

        assertHex("00007fff", Short.MAX_VALUE);
        assertHex("ffff8000", Short.MIN_VALUE);

        assertHex("0000ffff", Character.MAX_VALUE);
        assertHex("00000000", Character.MIN_VALUE);

        assertHex("7fffffff", Integer.MAX_VALUE);
        assertHex("80000000", Integer.MIN_VALUE);
    }

    private void assertHex(String hexString, int value) {

        assertEquals(hexString, Ints.toHex(value));

        assertEquals(value, Ints.fromHex(hexString));
    }
}
