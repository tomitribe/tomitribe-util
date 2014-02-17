/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util;

import junit.framework.TestCase;

public class StringsTest extends TestCase {

    public void testLc() throws Exception {
        assertEquals("orange", Strings.lc("OrAngE"));
    }

    public void testLowercase() throws Exception {
        assertEquals("orange", Strings.lowercase("oRANge"));
    }

    public void testUc() throws Exception {
        assertEquals("ORANGE", Strings.uc("oRANge"));
    }

    public void testUppercase() throws Exception {
        assertEquals("ORANGE", Strings.uppercase("OrAngE"));
    }

    public void testUcfirst() throws Exception {
        assertEquals("Orange", Strings.ucfirst("orange"));
    }

    public void testLcfirst() throws Exception {
        assertEquals("oRANGE", Strings.lcfirst("ORANGE"));
    }

    public void testCamelCase() throws Exception {
        assertEquals("Red", Strings.camelCase("red"));
        assertEquals("RedGreen", Strings.camelCase("red-green"));
        assertEquals("RedGreenBlue", Strings.camelCase("red-green-blue"));
    }

    public void testCamelCase1() throws Exception {
        assertEquals("Red", Strings.camelCase("red", "_"));
        assertEquals("RedGreen", Strings.camelCase("red_green", "_"));
        assertEquals("RedGreenBlue", Strings.camelCase("red_green_blue", "_"));

        assertEquals("Red", Strings.camelCase("red", "[_-]"));
        assertEquals("RedGreen", Strings.camelCase("red_green", "[_-]"));
        assertEquals("RedGreenBlue", Strings.camelCase("red_green-blue", "[_-]"));
    }
}
