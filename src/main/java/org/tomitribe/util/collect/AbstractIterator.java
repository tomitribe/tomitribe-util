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

package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractIterator<T> implements Iterator<T> {
    private T next;

    @Override
    public boolean hasNext() {
        if (next != null) return true;

        try {
            next = advance();

            return next != null;
        } catch (final NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public T next() {
        if (!hasNext()) throw new NoSuchElementException();

        final T v = next;
        next = null;
        return v;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract T advance() throws NoSuchElementException;
}
