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

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.tomitribe.util.hash.UnsafeConstants.*;

@SuppressWarnings("restriction")
final class JvmUtils {
    static final Unsafe unsafe;
    static final MethodHandle newByteBuffer;

    static {
        try {
            // fetch theUnsafe object
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            if (unsafe == null) {
                throw new RuntimeException("Unsafe access not available");
            }

            // verify the stride of arrays matches the width of primitives
            assertArrayIndexScale("Boolean", ARRAY_BOOLEAN_INDEX_SCALE, 1);
            assertArrayIndexScale("Byte", ARRAY_BYTE_INDEX_SCALE, 1);
            assertArrayIndexScale("Short", ARRAY_SHORT_INDEX_SCALE, 2);
            assertArrayIndexScale("Int", ARRAY_INT_INDEX_SCALE, 4);
            assertArrayIndexScale("Long", ARRAY_LONG_INDEX_SCALE, 8);
            assertArrayIndexScale("Float", ARRAY_FLOAT_INDEX_SCALE, 4);
            assertArrayIndexScale("Double", ARRAY_DOUBLE_INDEX_SCALE, 8);

            // fetch a method handle for the hidden constructor for DirectByteBuffer
            Class<?> directByteBufferClass = ClassLoader.getSystemClassLoader().loadClass("java.nio.DirectByteBuffer");
            Constructor<?> constructor = directByteBufferClass.getDeclaredConstructor(long.class, int.class, Object.class);
            constructor.setAccessible(true);
            newByteBuffer = MethodHandles.lookup().unreflectConstructor(constructor)
                    .asType(MethodType.methodType(ByteBuffer.class, long.class, int.class, Object.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertArrayIndexScale(final String name, int actualIndexScale, int expectedIndexScale) {
        if (actualIndexScale != expectedIndexScale) {
            throw new IllegalStateException(name + " array index scale must be " + expectedIndexScale + ", but is " + actualIndexScale);
        }
    }

    private JvmUtils() {
    }
}
