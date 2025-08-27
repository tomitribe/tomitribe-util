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

package org.tomitribe.util;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.util.editor.AbstractConverter;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.editor.Editors;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void testLocalDate() throws Exception {
        final Object o = Converter.convert("1976-03-30", LocalDate.class, "time");
        assertEquals(LocalDate.of(1976, 3, 30), o);
    }

    @Test
    public void testEnumEditor() throws Exception {
        PropertyEditorManager.registerEditor(TimeUnit.class, TimeUnitEditor.class);
        final PropertyEditor editor = Editors.get(TimeUnit.class);
        assertNotNull(editor);

        final Object o = Converter.convert("horas", TimeUnit.class, "time");
        assertEquals(TimeUnit.HOURS, o);
    }

    @Test
    public void testPathEditor() {
        final PropertyEditor editor = Editors.get(Path.class);
        assertNotNull(editor);

        final Object o = Converter.convert("my/path/and/file.txt", Path.class, "foo");
        assertEquals(Paths.get("my/path/and/file.txt"), o);
    }

    /**
     * Ensure string constructors are favored over charsequence
     */
    @Test
    public void charSequenceConstructor() {
        final Object o = Converter.convert("value", Purple.class, "foo");
        assertEquals("Purple{value='CharSequence: value'}", o.toString());
    }

    /**
     * Ensure string constructors are favored over charsequence
     */
    @Test
    public void charSequenceFactory() {
        final Object o = Converter.convert("value", Brown.class, "foo");
        assertEquals("Brown{value='CharSequence: value'}", o.toString());
    }

    /**
     * Ensure string constructors are favored over charsequence
     */
    @Test
    public void stringConstructorWins() {
        final Object o = Converter.convert("value", Orange.class, "foo");
        assertEquals("Orange{value='String: value'}", o.toString());
    }

    /**
     * Ensure string constructors are favored over charsequence
     */
    @Test
    public void stringFactoryWins() {
        final Object o = Converter.convert("value", Red.class, "foo");
        assertEquals("Red{value='String: value'}", o.toString());
    }

    /**
     * Ensure factories are sorted alphabetically
     */
    @Test
    public void sortedFactories() {
        final Object o = Converter.convert("abc", Blue.class, "foo");
        assertEquals("Blue{value='one: abc'}", o.toString());
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

    public static class TimeUnitEditor extends AbstractConverter {
        @Override
        protected Object toObjectImpl(String text) {
            if ("horas".equals(text)) return TimeUnit.HOURS;
            if ("dias".equals(text)) return TimeUnit.DAYS;
            return TimeUnit.valueOf(text);
        }
    }

    public static class Orange {
        private final String value;

        public Orange(final CharSequence charSequence) {
            this.value = "CharSequence: " + charSequence.toString();
        }

        public Orange(final String string) {
            this.value = "String: " + string;
        }

        @Override
        public String toString() {
            return "Orange{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }


    public static class Red {
        private final String value;

        private Red(final String value) {
            this.value = value;
        }

        public static Red parse(final CharSequence charSequence) {
            return new Red("CharSequence: " + charSequence.toString());
        }

        public static Red parse(final String string) {
            return new Red("String: " + string);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Red{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public static class Blue {
        private final String value;

        private Blue(final String value) {
            this.value = value;
        }

        public static Blue two(final String string) {
            return new Blue("two: " + string);
        }

        public static Blue one(final String string) {
            return new Blue("one: " + string);
        }

        public static Blue three(final String string) {
            return new Blue("three: " + string);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Blue{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public static class Purple {
        private final String value;

        public Purple(final CharSequence charSequence) {
            this.value = "CharSequence: " + charSequence.toString();
        }

        @Override
        public String toString() {
            return "Purple{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public static class Brown {
        private final String value;

        private Brown(final String value) {
            this.value = value;
        }

        public static Brown parse(final CharSequence charSequence) {
            return new Brown("CharSequence: " + charSequence.toString());
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Brown{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }


}
