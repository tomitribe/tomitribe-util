/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.util.hash;

import static java.lang.Long.rotateLeft;
import static org.tomitribe.util.hash.JvmUtils.unsafe;
import static org.tomitribe.util.hash.Preconditions.checkPositionIndexes;

/**
 * Lifted from Airlift Slice
 * @author Martin Traverso
 */
public class XxHash32 {
    private final static long PRIME32_1 = 506952113; // 2654435761L & 0x7FFFFFFF
    private final static long PRIME32_2 = 99338871; // 2246822519L & 0x7FFFFFFF
    private final static long PRIME32_3 = 1119006269; // 3266489917L & 0x7FFFFFFF
    private final static long PRIME32_4 = 668265263;
    private final static long PRIME32_5 = 374761393;

    private final static long DEFAULT_SEED = 0;

    public static long hash(String data) {
        return hash(Slices.utf8Slice(data));
    }

    public static long hash(Slice data) {
        return hash(data, 0, data.length());
    }

    public static long hash(long seed, Slice data) {
        return hash(seed, data, 0, data.length());
    }

    public static long hash(Slice data, int offset, int length) {
        return hash(DEFAULT_SEED, data, offset, length);
    }

    public static long hash(long seed, Slice data, int offset, int length) {
        checkPositionIndexes(0, offset + length, data.length());

        Object base = data.getBase();
        long p = data.getAddress() + offset;
        long end = p + length;

        long hash;

        if (length >= 16) {
            long v1 = seed + PRIME32_1 + PRIME32_2;
            long v2 = seed + PRIME32_2;
            long v3 = seed + 0;
            long v4 = seed - PRIME32_1;

            long limit = end - 16;
            do {
                v1 = mix(v1, unsafe.getInt(base, p));
                p += 4;

                v2 = mix(v2, unsafe.getInt(base, p));
                p += 4;

                v3 = mix(v3, unsafe.getInt(base, p));
                p += 4;

                v4 = mix(v4, unsafe.getInt(base, p));
                p += 4;
            }
            while (p <= limit);

            hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);

        } else {
            hash = seed + PRIME32_5;
        }

        hash += length;

        if (p <= end - 4) {

            hash += unsafe.getInt(base, p) * PRIME32_3;
            hash = rotateLeft(hash, 17) * PRIME32_4;
            p += 4;
        }

        while (p < end) {
            int unsigned = unsafe.getByte(base, p) & 0xFF;
            hash += (unsigned * PRIME32_5);
            hash = rotateLeft(hash, 11) * PRIME32_1;
            p++;
        }

        hash ^= hash >>> 15;
        hash *= PRIME32_2;
        hash ^= hash >>> 13;
        hash *= PRIME32_3;
        hash ^= hash >>> 16;

        return hash;
    }

    private static long mix(long current, long value) {
        return rotateLeft(current + value * PRIME32_2, 13) * PRIME32_1;
    }
}
