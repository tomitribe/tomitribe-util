/*
 * Copyright 2022 Tomitribe and community
 *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Futures {

    public static <V> Future<List<V>> of(final Future<V>... futures) {
        return new FutureList<V>(Arrays.asList(futures));
    }

    static class FutureList<V> implements Future<List<V>> {
        final List<Future<V>> futures = new ArrayList<>();

        public FutureList(final List<Future<V>> futures) {
            this.futures.addAll(futures);
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            boolean b = true;
            for (final Future<V> future : futures) {
                if (!future.cancel(mayInterruptIfRunning)) {
                    b = false;
                }
            }
            return b;
        }

        @Override
        public boolean isCancelled() {
            for (final Future<V> future : futures) {
                if (!future.isCancelled()) return false;
            }
            return true;
        }

        @Override
        public boolean isDone() {
            for (final Future<V> future : futures) {
                if (!future.isDone()) return false;
            }
            return true;
        }

        @Override
        public List<V> get() throws InterruptedException, ExecutionException {
            final List<V> list = new ArrayList<V>();

            for (final Future<V> future : futures) {
                list.add(future.get());
            }
            return list;
        }

        @Override
        public List<V> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            final List<V> list = new ArrayList<V>();
            final long max = System.nanoTime() + unit.toNanos(timeout);

            for (final Future<V> future : futures) {
                final long remaining = max - System.nanoTime();

                if (remaining < 0) throw new TimeoutException();

                final V v = future.get(remaining, TimeUnit.NANOSECONDS);
                list.add(v);
            }
            return list;
        }

    }
}
