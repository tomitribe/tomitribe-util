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

public class Ints {

    private Ints() {
    }

    public static byte[] toBytes(final int v) {
        final byte[] bytes = new byte[4];
        bytes[0] = (byte) (v >>> 24);
        bytes[1] = (byte) (v >>> 16);
        bytes[2] = (byte) (v >>> 8);
        bytes[3] = (byte) (v);
        return bytes;
    }

    public static int fromBytes(final byte[] bytes) {
        if (bytes == null) throw new IllegalArgumentException("bytes are null");
        if (bytes.length != 4) throw new IllegalArgumentException("bytes length not 4: " + bytes.length);

        return ((bytes[0] << 24) +
                ((bytes[1] & 255) << 16) +
                ((bytes[2] & 255) << 8) +
                ((bytes[3] & 255)));
    }

    public static String toHex(final int value) {
        final byte[] bytes = toBytes(value);
        return Hex.toString(bytes);
    }

    public static int fromHex(final String hex) {
        final byte[] bytes = Hex.fromString(hex);
        return fromBytes(bytes);
    }

}
