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

import org.junit.Test;
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class SuppliersTest {

    @Test
    public void asIterator() {
        final AtomicInteger nullsReturned = new AtomicInteger();
        final Supplier<String> supplier = new Supplier<String>() {
            private int count;

            @Override
            public String get() {
                if (++count < 10) return "count-" + count;
                nullsReturned.incrementAndGet();
                return null;
            }
        };

        final List<String> entries = new ArrayList<String>();

        final Iterator<String> iterator = Suppliers.asIterator(supplier);
        while (iterator.hasNext()) {
            entries.add(iterator.next());
        }

        assertEquals("" +
                "count-1\n" +
                "count-2\n" +
                "count-3\n" +
                "count-4\n" +
                "count-5\n" +
                "count-6\n" +
                "count-7\n" +
                "count-8\n" +
                "count-9", Join.join("\n", entries));

        assertEquals(1, nullsReturned.get());
        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException e) {
            // pass
        }
        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException e) {
            // pass
        }
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertEquals(1, nullsReturned.get());
    }

    @Test
    public void asStream() {
        final AtomicInteger nullsReturned = new AtomicInteger();
        final Supplier<String> supplier = new Supplier<String>() {
            private int count;

            @Override
            public String get() {
                if (++count < 10) return "count-" + count;
                nullsReturned.incrementAndGet();
                return null;
            }
        };

        final List<String> strings = Suppliers.asStream(supplier).collect(Collectors.toList());
        assertEquals("" +
                "count-1\n" +
                "count-2\n" +
                "count-3\n" +
                "count-4\n" +
                "count-5\n" +
                "count-6\n" +
                "count-7\n" +
                "count-8\n" +
                "count-9", Join.join("\n", strings));

    }
}
