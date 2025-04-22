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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class for converting {@link Supplier} instances into
 * {@link Iterator}s and {@link Stream}s. Especially useful for paging through
 * remote resources (e.g. AWS paged APIs) without loading everything into memory.
 */
public class Suppliers {
    private Suppliers() {
    }

    /**
     * Converts a {@link Supplier} into a lazily-evaluated {@link Stream}.
     * Iteration stops when the supplier returns {@code null}.
     *
     * @param supplier the supplier providing items
     * @param <T>      the item type
     * @return a stream of values returned by the supplier
     */
    public static <T> Stream<T> asStream(final Supplier<T> supplier) {
        return asStream(asIterator(supplier));
    }

    /**
     * Converts a {@link Supplier} and extractor function into a stream of flattened items.
     * Useful for paged API results that return lists of items in a wrapper.
     *
     * @param supplier  the supplier of paged responses
     * @param getItems  function to extract items from each response
     * @param <T>       the type of the full response
     * @param <I>       the type of the extracted item
     * @return a stream of items extracted from each page
     */
    public static <T, I> Stream<I> asStream(final Supplier<T> supplier, final Function<T, Collection<I>> getItems) {
        return asStream(asIterator(supplier)).map(getItems).flatMap(Collection::stream);
    }

    /**
     * Wraps a {@link Supplier} in an {@link Iterator}. Iteration stops when the supplier returns {@code null}.
     *
     * @param supplier the supplier of elements
     * @param <T>      the type of elements
     * @return an iterator over the supplier
     */
    public static <T> Iterator<T> asIterator(final Supplier<T> supplier) {
        return new SupplierIterator<>(supplier);
    }

    /**
     * Converts a given {@link Iterator} to a {@link Stream}.
     *
     * @param iterator the iterator to wrap
     * @param <T>      the type of elements
     * @return a stream backed by the given iterator
     */
    public static <T> Stream<T> asStream(final Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }

    /**
     * Combines a recursive {@link Function} with an item extractor to create a stream of flattened items.
     * The function is repeatedly applied to the previous result to produce the next item.
     *
     * @param function  function to derive the next result from the previous
     * @param getItems  function to extract items from each result
     * @param <T>       type of the result object
     * @param <I>       type of the item to stream
     * @return a stream of items from each page-like result
     */
    public static <T, I> Stream<I> asStream(final Function<T, T> function, final Function<T, Collection<I>> getItems) {
        return asStream(asIterator(asSupplier(function))).map(getItems).flatMap(Collection::stream);
    }

    /**
     * Converts a recursive function into a {@link Supplier}. The function is applied to the previous value to get the next.
     * The initial value is {@code null}.
     *
     * @param function the recursive function to generate values
     * @param <T>      the type of values supplied
     * @return a supplier that repeatedly applies the function
     */
    public static <T> Supplier<T> asSupplier(final Function<T, T> function) {
        return new RecursiveSupplier<>(function);
    }


    public static <T> Supplier<T> singleton(final Supplier<T> supplier) {
        return new SingletonSupplier<>(supplier);
    }

    /**
     * A {@link Supplier} that repeatedly applies a function to the previous result.
     * Starts with {@code null} and returns each result of {@code function.apply(previous)}.
     *
     * @param <T> the type of value supplied
     */
    public static class RecursiveSupplier<T> implements Supplier<T> {
        private final Function<T, T> function;
        private T previous;

        public RecursiveSupplier(final Function<T, T> function) {
            this.function = function;
        }

        @Override
        public T get() {
            previous = function.apply(previous);
            return previous;
        }
    }

    /**
     * An {@link Iterator} implementation that pulls values from a {@link Supplier}
     * until the supplier returns {@code null}.
     *
     * @param <T> the type of elements
     */
    private static class SupplierIterator<T> implements Iterator<T> {
        private final Supplier<T> supplier;
        private T next;

        public SupplierIterator(final Supplier<T> supplier) {
            this.supplier = supplier;
            this.next = this.supplier.get();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            final T result = this.next;
            if (result == null) throw new NoSuchElementException();
            this.next = supplier.get();
            return result;
        }
    }

    public static class SingletonSupplier<T> implements Supplier<T> {

        private final Supplier<T> supplier;
        private volatile T instance;

        public SingletonSupplier(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) {
                        instance = supplier.get();
                    }
                }
            }
            return instance;
        }
    }

}