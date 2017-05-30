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

import java.util.regex.Pattern;

import static java.lang.Character.digit;

public class Hex {

    private static final Pattern valid = Pattern.compile("^[A-Fa-f0-9]+$");

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    private Hex() {
    }

    public static String toString(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("hex string is null");
        }

        // Empty string, empty bytes
        if (s.length() == 0) {
            return new byte[0];
        }

        if (!valid.matcher(s).matches()) {
            throw new InvalidHexFormatException(s);
        }

        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((digit(s.charAt(i), 16) << 4) + digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static class InvalidHexFormatException extends IllegalArgumentException {
        private final String string;

        public InvalidHexFormatException(String string) {
            super(String.format("Invalid hex string '%s'", string));
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }
}
