/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.util.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Suppliers {
    private Suppliers() {
    }

    public static <T> Stream<T> asStream(final Supplier<T> supplier) {
        return asStream(asIterator(supplier));
    }

    public static <T> Iterator<T> asIterator(final Supplier<T> supplier) {
        return new SupplierIterator<>(supplier);
    }

    public static <T> Stream<T> asStream(final Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }


    private static class SupplierIterator<T> implements Iterator<T> {
        private final Supplier<T> supplier;
        private T next;

        public SupplierIterator(final Supplier<T> supplier) {
            this.supplier = wrap(supplier);
            this.next = this.supplier.get();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            final T next = this.next;
            if (next == null) throw new NoSuchElementException();
            try {
                return next;
            } finally {
                this.next = supplier.get();
            }
        }

        private Supplier<T> wrap(final Supplier<T> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw e;
                }
            };
        }
    }
}
