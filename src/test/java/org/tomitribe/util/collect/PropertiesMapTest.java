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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesMapTest extends TestCase {

    public void testGet() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("color", "red");

        final PropertiesMap map = new PropertiesMap(properties);
        assertEquals("red", map.get("color"));
        assertNull(map.get("missing"));
    }

    public void testPutWritesThrough() throws Exception {
        final Properties properties = new Properties();
        final PropertiesMap map = new PropertiesMap(properties);

        assertNull(map.put("color", "red"));
        assertEquals("red", properties.getProperty("color"));

        assertEquals("red", map.put("color", "green"));
        assertEquals("green", properties.getProperty("color"));
    }

    public void testExternalChangesAreVisible() throws Exception {
        final Properties properties = new Properties();
        final PropertiesMap map = new PropertiesMap(properties);

        properties.setProperty("color", "blue");
        assertEquals("blue", map.get("color"));
        assertEquals(1, map.size());
    }

    public void testRemove() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("color", "red");

        final PropertiesMap map = new PropertiesMap(properties);
        assertEquals("red", map.remove("color"));
        assertFalse(properties.containsKey("color"));
        assertTrue(map.isEmpty());
    }

    public void testIteration() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("one", "1");
        properties.setProperty("two", "2");
        properties.setProperty("three", "3");

        final PropertiesMap map = new PropertiesMap(properties);

        final List<String> keys = new ArrayList<String>();
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            keys.add(entry.getKey() + "=" + entry.getValue());
        }
        keys.sort(String::compareTo);
        assertEquals("[one=1, three=3, two=2]", keys.toString());
    }

    public void testEntrySetValueWritesThrough() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("color", "red");

        final PropertiesMap map = new PropertiesMap(properties);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            assertEquals("red", entry.setValue("green"));
        }
        assertEquals("green", properties.getProperty("color"));
    }

    public void testIteratorRemoveWritesThrough() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("keep", "yes");
        properties.setProperty("drop", "no");

        final PropertiesMap map = new PropertiesMap(properties);
        map.entrySet().removeIf(entry -> entry.getKey().equals("drop"));

        assertFalse(properties.containsKey("drop"));
        assertTrue(properties.containsKey("keep"));
    }

    public void testNonStringValueSurfacedAsString() throws Exception {
        final Properties properties = new Properties();
        properties.put("count", 42);

        final PropertiesMap map = new PropertiesMap(properties);
        assertEquals("42", map.get("count"));
    }

    public void testNoArgConstructor() throws Exception {
        final PropertiesMap map = new PropertiesMap();
        map.put("color", "red");
        assertEquals("red", map.getProperties().getProperty("color"));
    }
}
