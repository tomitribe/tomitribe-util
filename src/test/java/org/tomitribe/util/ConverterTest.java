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
package org.tomitribe.util;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.editor.Converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class ConverterTest extends Assert {
    private Collection<Duration> durations;
    private Map<Key, Duration> durationsMap;

    @Test
    public void testConvert() throws Exception {

        assertEquals(1, Converter.convert("1", Integer.class, null));
        assertEquals(1l, Converter.convert("1", Long.class, null));
        assertEquals(true, Converter.convert("true", Boolean.class, null));
        assertEquals(new URI("foo"), Converter.convert("foo", URI.class, null));
        assertEquals(new Green("foo"), Converter.convert("foo", Green.class, null));

        final Yellow expected = new Yellow();
        expected.value = "foo";

        assertEquals(expected, Converter.convert("foo", Yellow.class, null));
    }

    @Test
    public void testConvertString() throws NoSuchFieldException {
        assertEquals(
                asList("a", "b", "c"),
                Converter.convertString("a, b, c", new ParameterizedTypeImpl(List.class, String.class), "foo"));
        assertEquals(
                asList(new Duration("5 s"), new Duration("4 s"), new Duration("3 s")),
                Converter.convertString("5s, 4 s, 3 seconds", ConverterTest.class.getDeclaredField("durations").getGenericType(), "reflection"));
        assertEquals(
                new HashSet<String>(asList("a", "b", "c")),
                Converter.convertString("a, b, c", new ParameterizedTypeImpl(Set.class, String.class), "foo"));
        assertEquals(
                asList(1, 2, 3),
                Converter.convertString("1, 2, 3", new ParameterizedTypeImpl(Collection.class, Integer.class), "foo"));
        assertEquals(
                new HashMap<Integer, String>() {{
                    put(1, "a");
                    put(2, "b");
                    put(3, "c");
                }},
                Converter.convertString("1=a\n2=b\n3=c\n", new ParameterizedTypeImpl(Map.class, Integer.class, String.class), "foo"));
        assertEquals(
                new HashMap<Key, Duration>() {{
                    put(new Key("__"), new Duration("5 s"));
                    put(new Key("%&$"), new Duration("4 s"));
                    put(new Key("---"), new Duration("3 s"));
                }},
                Converter.convertString("__=5s\n%&$=4s\n---=3 seconds",
                        ConverterTest.class.getDeclaredField("durationsMap").getGenericType(), "reflection map"));

    }

    public static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type raw;
        private final Type[] arg;

        public ParameterizedTypeImpl(final Type raw, final Type... arg) {
            this.raw = raw;
            this.arg = arg;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return arg;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    public static class Green {

        private String value;

        public Green(final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Green)) {
                return false;
            }

            final Green green = (Green) o;

            if (value != null ? !value.equals(green.value) : green.value != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class Yellow {

        private String value;

        public static Yellow makeOne(final String value) {
            final Yellow yellow = new Yellow();
            yellow.value = value;
            return yellow;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Yellow yellow = (Yellow) o;

            if (value != null ? !value.equals(yellow.value) : yellow.value != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class Key {
        private final String k;

        public Key(String pwz) {
            k = pwz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return k.equals(key.k);

        }

        @Override
        public int hashCode() {
            return k.hashCode();
        }

        @Override
        public String toString() {
            return "Key{" +
                    "k='" + k + '\'' +
                    '}';
        }
    }
}
