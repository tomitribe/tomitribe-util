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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class PrintString extends PrintStream {

    private final ByteArrayOutputStream baos;

    public PrintString() {
        this(512);
    }

    public PrintString(final int size) {
        super(new ByteArrayOutputStream(size), true);
        baos = (ByteArrayOutputStream) out;
    }

    public byte[] toByteArray() {
        flush();
        return baos.toByteArray();
    }

    public String toString(final String charsetName) throws UnsupportedEncodingException {
        flush();
        return baos.toString(charsetName);
    }

    public int size() {
        flush();
        return baos.size();
    }

    @Override
    public String toString() {
        flush();
        return baos.toString();
    }
}
