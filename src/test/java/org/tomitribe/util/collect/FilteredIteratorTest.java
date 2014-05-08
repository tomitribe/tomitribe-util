/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.util.collect;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class FilteredIteratorTest extends Assert {
    @Test
    public void testNextOnly() throws Exception {

        final List<String> strings = Arrays.asList("red", "green", "blue");
        final Iterator<String> iterator = new FilteredIterator<String>(strings.iterator(), new FilteredIterator.Filter<String>() {
            @Override
            public boolean accept(final String s) {
                return !"green".equals(s);
            }
        });

        assertEquals("red", iterator.next());
        assertEquals("blue", iterator.next());

        try {
            iterator.next();

            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            // pass
        }

    }

    @Test
    public void testRepeatHasNext() throws Exception {

        final List<String> strings = Arrays.asList("red", "green", "blue");
        final Iterator<String> iterator = new FilteredIterator<String>(strings.iterator(), new FilteredIterator.Filter<String>() {
            @Override
            public boolean accept(final String s) {
                return !"green".equals(s);
            }
        });

        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("red", iterator.next());
        assertEquals("blue", iterator.next());

        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());

        try {
            iterator.next();

            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException e) {
            // pass
        }

    }
}
