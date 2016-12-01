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
package java.nio;

import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_BOOLEAN_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_BYTE_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_DOUBLE_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_FLOAT_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_INT_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_LONG_INDEX_SCALE;
import static org.tomitribe.util.hash.UnsafeConstants.ARRAY_SHORT_INDEX_SCALE;

import java.lang.reflect.Constructor;

public class NioPackageUtils {
    private NioPackageUtils() {
    }

    private static final Constructor<DirectByteBuffer> constructor;
    static {
        try {
            assertArrayIndexScale("Boolean", ARRAY_BOOLEAN_INDEX_SCALE, 1);
            assertArrayIndexScale("Byte", ARRAY_BYTE_INDEX_SCALE, 1);
            assertArrayIndexScale("Short", ARRAY_SHORT_INDEX_SCALE, 2);
            assertArrayIndexScale("Int", ARRAY_INT_INDEX_SCALE, 4);
            assertArrayIndexScale("Long", ARRAY_LONG_INDEX_SCALE, 8);
            assertArrayIndexScale("Float", ARRAY_FLOAT_INDEX_SCALE, 4);
            assertArrayIndexScale("Double", ARRAY_DOUBLE_INDEX_SCALE, 8);

            // fetch a method handle for the hidden constructor for DirectByteBuffer
            @SuppressWarnings("unchecked")
            Class<DirectByteBuffer> directByteBufferClass = (Class<DirectByteBuffer>) ClassLoader.getSystemClassLoader()
                    .loadClass("java.nio.DirectByteBuffer");
            constructor = directByteBufferClass.getDeclaredConstructor(long.class, int.class, Object.class);
            constructor.setAccessible(true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DirectByteBuffer newDirectByteBuffer(long addr, int cap, Object ob) {
        try {
            return constructor.newInstance(addr, cap, ob);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DirectByteBuffer newDirectByteBuffer(Object[] args) {
        try {
            return constructor.newInstance(args[0], args[1], args[2]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertArrayIndexScale(final String name, int actualIndexScale, int expectedIndexScale) {
        if (actualIndexScale != expectedIndexScale) {
            throw new IllegalStateException(
                    name + " array index scale must be " + expectedIndexScale + ", but is " + actualIndexScale);
        }
    }
}
