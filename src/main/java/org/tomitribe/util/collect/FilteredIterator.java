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

public class FilteredIterator<T> extends AbstractIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private final Filter<T> filter;

    public FilteredIterator(final Iterator<T> iterator, final Filter<T> filter) {
        if (iterator == null) throw new IllegalArgumentException("iterator cannot be null");
        if (filter == null) throw new IllegalArgumentException("filter cannot be null");

        this.iterator = iterator;
        this.filter = filter;
    }

    @Override
    protected T advance() throws NoSuchElementException {
        T next = null;

        while ((next = iterator.next()) != null) {
            if (filter.accept(next)) {
                return next;
            }
        }

        throw new NoSuchElementException();
    }

    public interface Filter<T> {
        boolean accept(T t);
    }
}
