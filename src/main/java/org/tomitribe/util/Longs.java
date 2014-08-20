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

public class Longs {

    private Longs() {
    }

    public static byte[] toBytes(long v) {
        final byte[] bytes = new byte[8];
        bytes[0] = (byte) (v >>> 56);
        bytes[1] = (byte) (v >>> 48);
        bytes[2] = (byte) (v >>> 40);
        bytes[3] = (byte) (v >>> 32);
        bytes[4] = (byte) (v >>> 24);
        bytes[5] = (byte) (v >>> 16);
        bytes[6] = (byte) (v >>> 8);
        bytes[7] = (byte) (v >>> 0);
        return bytes;
    }

    public static long fromBytes(byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes are null");
        if (bytes == null) throw new IllegalArgumentException("bytes length not 8: " + bytes.length);

        return (((long) bytes[0] << 56) +
                ((long) (bytes[1] & 255) << 48) +
                ((long) (bytes[2] & 255) << 40) +
                ((long) (bytes[3] & 255) << 32) +
                ((long) (bytes[4] & 255) << 24) +
                ((bytes[5] & 255) << 16) +
                ((bytes[6] & 255) << 8) +
                ((bytes[7] & 255) << 0));
    }

    public static String toHex(final long value) {
        final byte[] bytes = toBytes(value);
        return Hex.toString(bytes);
    }

    public static long fromHex(final String hex) {
        final byte[] bytes = Hex.fromString(hex);
        return fromBytes(bytes);
    }
}
