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

import static java.nio.NioPackageUtils.newDirectByteBuffer;
import static sun.misc.Unsafe.getUnsafe;

import java.nio.ByteBuffer;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
final class JvmUtils {
    static final Unsafe unsafe = getUnsafe();
    static final BackwardsCompatibleInnerClass newByteBuffer = new BackwardsCompatibleInnerClass();

    private JvmUtils() {
    }

    public static class BackwardsCompatibleInnerClass {

        public ByteBuffer invokeExact(Object[] args) {
            return newDirectByteBuffer(args);
        }

    }
}
