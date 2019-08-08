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

import java.io.IOException;
import java.io.InputStream;

import static java.lang.Integer.rotateLeft;
import static java.lang.Math.min;
import static org.tomitribe.util.hash.JvmUtils.unsafe;
import static org.tomitribe.util.hash.Preconditions.checkPositionIndexes;
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

/**
 * Lifted from Airlift Slice
 * @author Martin Traverso
 */
public class XxHash32 {
    private final static int PRIME32_1 = 506952113; // 2654435761L & 0x7FFFFFFF
    private final static int PRIME32_2 = 99338871; // 2246822519L & 0x7FFFFFFF
    private final static int PRIME32_3 = 1119006269; // 3266489917L & 0x7FFFFFFF
    private final static int PRIME32_4 = 668265263;
    private final static int PRIME32_5 = 374761393;

    private final static int DEFAULT_SEED = 0;

    private final int seed;

    private static final long BUFFER_ADDRESS = ARRAY_BYTE_BASE_OFFSET;
    private final byte[] buffer = new byte[16];
    private int bufferSize;

    private long bodyLength;

    private int v1;
    private int v2;
    private int v3;
    private int v4;

    public XxHash32() {
        this(DEFAULT_SEED);
    }

    public XxHash32(int seed) {
        this.seed = seed;
        this.v1 = seed + PRIME32_1 + PRIME32_2;
        this.v2 = seed + PRIME32_2;
        this.v3 = seed;
        this.v4 = seed - PRIME32_1;
    }

    public XxHash32 update(byte[] data) {
        return update(data, 0, data.length);
    }

    public XxHash32 update(byte[] data, int offset, int length) {
        checkPositionIndexes(offset, offset + length, data.length);
        updateHash(data, ARRAY_BYTE_BASE_OFFSET + offset, length);
        return this;
    }

    public XxHash32 update(Slice data) {
        return update(data, 0, data.length());
    }

    public XxHash32 update(Slice data, int offset, int length) {
        checkPositionIndexes(0, offset + length, data.length());
        updateHash(data.getBase(), data.getAddress() + offset, length);
        return this;
    }

    public int hash() {
        int hash;
        if (bodyLength > 0) {
            hash = computeBody();
        } else {
            hash = seed + PRIME32_5;
        }

        hash += bodyLength + bufferSize;

        return updateTail(hash, buffer, BUFFER_ADDRESS, 0, bufferSize);
    }

    private int computeBody() {
        int hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);

//        hash = update(hash, v1);
//        hash = update(hash, v2);
//        hash = update(hash, v3);
//        hash = update(hash, v4);

        return hash;
    }

    private void updateHash(Object base, long address, int length) {
        if (bufferSize > 0) {
            int available = min(16 - bufferSize, length);

            unsafe.copyMemory(base, address, buffer, BUFFER_ADDRESS + bufferSize, available);

            bufferSize += available;
            address += available;
            length -= available;

            if (bufferSize == 16) {
                updateBody(buffer, BUFFER_ADDRESS, bufferSize);
                bufferSize = 0;
            }
        }

        if (length >= 16) {
            int index = updateBody(base, address, length);
            address += index;
            length -= index;
        }

        if (length > 0) {
            unsafe.copyMemory(base, address, buffer, BUFFER_ADDRESS, length);
            bufferSize = length;
        }
    }

    private int updateBody(Object base, long address, int length) {
        int remaining = length;
        while (remaining >= 16) {
            v1 = mix(v1, unsafe.getInt(base, address));
            v2 = mix(v2, unsafe.getInt(base, address + 4));
            v3 = mix(v3, unsafe.getInt(base, address + 8));
            v4 = mix(v4, unsafe.getInt(base, address + 12));

            address += 16;
            remaining -= 16;
        }

        int index = length - remaining;
        bodyLength += index;
        return index;
    }

    public static long hash(int value) {
        int hash = DEFAULT_SEED + PRIME32_5 + SizeOf.SIZE_OF_INT;
        hash = updateTail(hash, value);
        hash = finalShuffle(hash);

        return hash;
    }

    public static int hash(String data) {
        return hash(Slices.utf8Slice(data));
    }

    public static int hash(InputStream in) throws IOException {
        return hash(DEFAULT_SEED, in);
    }

    public static int hash(int seed, InputStream in)
            throws IOException {
        XxHash32 hash = new XxHash32(seed);
        byte[] buffer = new byte[8192];
        while (true) {
            int length = in.read(buffer);
            if (length == -1) {
                break;
            }
            hash.update(buffer, 0, length);
        }
        return hash.hash();
    }

    public static int hash(Slice data) {
        return hash(data, 0, data.length());
    }

    public static int hash(int seed, Slice data) {
        return hash(seed, data, 0, data.length());
    }

    public static int hash(Slice data, int offset, int length) {
        return hash(DEFAULT_SEED, data, offset, length);
    }

    public static int hash(int seed, Slice data, int offset, int length) {
        checkPositionIndexes(0, offset + length, data.length());

        Object base = data.getBase();
        final long address = data.getAddress() + offset;

        int hash;

        if (length >= 16) {
            hash = updateBody(seed, base, address, length);
        } else {
            hash = seed + PRIME32_5;
        }

        hash += length;

        // round to the closest 32 byte boundary
        // this is the point up to which updateBody() processed
        int index = length & 0xFFFFFFF0;

        return updateTail(hash, base, address, index, length);
    }

    private static int updateTail(int hash, Object base, long address, int index, int length) {
        if (index <= length - 4) {
            hash = updateTail(hash, unsafe.getInt(base, address + index));
            index += 4;
        }

        while (index < length) {
            hash = updateTail(hash, unsafe.getByte(base, address + index));
            index++;
        }

        hash = finalShuffle(hash);

        return hash;
    }

    private static int updateBody(int seed, Object base, long address, int length) {
        int v1 = seed + PRIME32_1 + PRIME32_2;
        int v2 = seed + PRIME32_2;
        int v3 = seed;
        int v4 = seed - PRIME32_1;

        int remaining = length;
        while (remaining >= 16) {
            v1 = mix(v1, unsafe.getInt(base, address));
            v2 = mix(v2, unsafe.getInt(base, address + 4));
            v3 = mix(v3, unsafe.getInt(base, address + 8));
            v4 = mix(v4, unsafe.getInt(base, address + 12));

            address += 16;
            remaining -= 16;
        }

        int hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);
//
//        hash = update(hash, v1);
//        hash = update(hash, v2);
//        hash = update(hash, v3);
//        hash = update(hash, v4);

        return hash;
    }

    private static int mix(int current, int value) {
        return rotateLeft(current + value * PRIME32_2, 13) * PRIME32_1;
    }

    private static int update(int hash, int value) {
        int temp = hash + mix(0, value);
        return temp * PRIME32_1 + PRIME32_4;
    }

    private static int updateTail(int hash, int value) {
        int temp = hash + value * PRIME32_3;
        return rotateLeft(temp, 17) * PRIME32_4;
    }

    private static int updateTail(int hash, byte value) {
        int unsigned = value & 0xFF;
        int temp = hash;
        temp += (unsigned * PRIME32_5);
        return rotateLeft(temp, 11) * PRIME32_1;
    }

    private static int finalShuffle(int hash) {
        hash ^= hash >>> 15;
        hash *= PRIME32_2;
        hash ^= hash >>> 13;
        hash *= PRIME32_3;
        hash ^= hash >>> 16;
        return hash;
    }
}
