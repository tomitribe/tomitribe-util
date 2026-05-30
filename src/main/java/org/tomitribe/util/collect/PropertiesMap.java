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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * A live {@code Map<String, String>} view over a {@link Properties} object.
 *
 * <p>Wrapping rather than copying means mutations flow both ways: {@code put},
 * {@code remove} and {@link Iterator#remove()} write through to the underlying
 * {@code Properties}, and external changes to that {@code Properties} are
 * visible through this map. Any non-{@code String} keys or values that may have
 * been placed into the {@code Properties} directly are surfaced via
 * {@link String#valueOf(Object)}.</p>
 */
public class PropertiesMap extends AbstractMap<String, String> {

    private final Properties properties;

    public PropertiesMap() {
        this(new Properties());
    }

    public PropertiesMap(final Properties properties) {
        if (properties == null) throw new IllegalArgumentException("properties is null");
        this.properties = properties;
    }

    /**
     * @return the underlying {@code Properties} this map is a view of
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String get(final Object key) {
        return asString(properties.get(key));
    }

    @Override
    public String put(final String key, final String value) {
        return asString(properties.setProperty(key, value));
    }

    @Override
    public String remove(final Object key) {
        return asString(properties.remove(key));
    }

    @Override
    public boolean containsKey(final Object key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return properties.containsValue(value);
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                final Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
                return new Iterator<Entry<String, String>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<String, String> next() {
                        return new StringEntry(it.next());
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return properties.size();
            }
        };
    }

    private static String asString(final Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static class StringEntry implements Entry<String, String> {
        private final Entry<Object, Object> entry;

        private StringEntry(final Entry<Object, Object> entry) {
            this.entry = entry;
        }

        @Override
        public String getKey() {
            return asString(entry.getKey());
        }

        @Override
        public String getValue() {
            return asString(entry.getValue());
        }

        @Override
        public String setValue(final String value) {
            return asString(entry.setValue(value));
        }
    }
}
