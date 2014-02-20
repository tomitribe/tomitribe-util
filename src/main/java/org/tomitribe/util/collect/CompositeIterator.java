/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompositeIterator<T> implements Iterator<T> {

    private final Iterator<Iterator<T>> source;
    private Iterator<T> current;

    private CompositeIterator(final Iterator<Iterator<T>> source) {
        this.source = source;
        if (this.source.hasNext()) {
            current = this.source.next();
        }
    }

    public boolean hasNext() {
        if (current == null) return false;
        if (current.hasNext()) return true;

        if (source.hasNext()) {
            current = source.next();
            return hasNext();
        }
        return false;
    }

    public T next() {
        if (!hasNext()) throw new NoSuchElementException();

        return current.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
