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

import java.nio.charset.StandardCharsets;

public class Longs {

    private Longs() {
    }

    public static byte[] toBytes(final long v) {
        final byte[] bytes = new byte[8];
        bytes[0] = (byte) (v >>> 56);
        bytes[1] = (byte) (v >>> 48);
        bytes[2] = (byte) (v >>> 40);
        bytes[3] = (byte) (v >>> 32);
        bytes[4] = (byte) (v >>> 24);
        bytes[5] = (byte) (v >>> 16);
        bytes[6] = (byte) (v >>> 8);
        bytes[7] = (byte) (v);
        return bytes;
    }

    public static long fromBytes(final byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes are null");
        if (bytes.length != 8) throw new IllegalArgumentException("bytes length not 8: " + bytes.length);

        return (((long) bytes[0] << 56) +
                ((long) (bytes[1] & 255) << 48) +
                ((long) (bytes[2] & 255) << 40) +
                ((long) (bytes[3] & 255) << 32) +
                ((long) (bytes[4] & 255) << 24) +
                ((bytes[5] & 255) << 16) +
                ((bytes[6] & 255) << 8) +
                ((bytes[7] & 255)));
    }

    public static String toHex(final long value) {
        final byte[] bytes = toBytes(value);
        return Hex.toString(bytes);
    }

    public static long fromHex(final String hex) {
        final byte[] bytes = Hex.fromString(hex);
        return fromBytes(bytes);
    }

    public static String toBase32(final long value) {
        final byte[] bytes = toBytes(value);
        return Base32.encode(bytes);
    }

    public static long fromBase32(final String base32) {
        try {
            final byte[] bytes = Base32.decode(base32);
            return fromBytes(bytes);
        } catch (Base32.DecodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String toBase64(final long value) {
        final byte[] bytes = toBytes(value);
        return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
    }

    public static long fromBase64(final String base64) {
        final byte[] bytes = Base64.decodeBase64(base64.getBytes(StandardCharsets.UTF_8));
        return fromBytes(bytes);
    }

    public static String toBase58(final long value) {
        final byte[] bytes = toBytes(value);
        return Base58.encode(bytes);
    }

    public static long fromBase58(final String base58) {
        final byte[] bytes = Base58.decode(base58);
        return fromBytes(bytes);
    }
}
