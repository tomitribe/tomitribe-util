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

package org.tomitribe.util.dir;

import lombok.Data;
import org.junit.Test;
import org.tomitribe.util.Archive;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TypesTest {

    @Test
    public void booleanDir() throws Exception {

        final File dir = new Archive()
                .add("true/foo.txt", "correct")
                .add("false/foo.txt", "incorrect")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(false), "incorrect");
        assertSection(types.section(true), "correct");
    }

    @Test
    public void charDir() throws Exception {

        final File dir = new Archive()
                .add("b/foo.txt", "B")
                .add("1/foo.txt", "one")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section('b'), "B");
        assertSection(types.section('1'), "one");
    }

    @Test
    public void byteDir() throws Exception {

        final File dir = new Archive()
                .add(Byte.MIN_VALUE + "/foo.txt", "min")
                .add(Byte.MAX_VALUE + "/foo.txt", "max")
                .add("112/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Byte.MIN_VALUE), "min");
        assertSection(types.section(Byte.MAX_VALUE), "max");
        assertSection(types.section((byte) 112), "middle");
    }

    @Test
    public void shortDir() throws Exception {

        final File dir = new Archive()
                .add(Short.MIN_VALUE + "/foo.txt", "min")
                .add(Short.MAX_VALUE + "/foo.txt", "max")
                .add("1123/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Short.MIN_VALUE), "min");
        assertSection(types.section(Short.MAX_VALUE), "max");
        assertSection(types.section((short) 1123), "middle");
    }

    @Test
    public void intDir() throws Exception {

        final File dir = new Archive()
                .add(Integer.MIN_VALUE + "/foo.txt", "min")
                .add(Integer.MAX_VALUE + "/foo.txt", "max")
                .add("1123/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Integer.MIN_VALUE), "min");
        assertSection(types.section(Integer.MAX_VALUE), "max");
        assertSection(types.section((int) 1123), "middle");
    }

    @Test
    public void longDir() throws Exception {

        final File dir = new Archive()
                .add(Long.MIN_VALUE + "/foo.txt", "min")
                .add(Long.MAX_VALUE + "/foo.txt", "max")
                .add("1123/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Long.MIN_VALUE), "min");
        assertSection(types.section(Long.MAX_VALUE), "max");
        assertSection(types.section((long) 1123), "middle");
    }

    @Test
    public void floatDir() throws Exception {

        final File dir = new Archive()
                .add(Float.MIN_VALUE + "/foo.txt", "min")
                .add(Float.MAX_VALUE + "/foo.txt", "max")
                .add("1123.0/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Float.MIN_VALUE), "min");
        assertSection(types.section(Float.MAX_VALUE), "max");
        assertSection(types.section((float) 1123), "middle");
    }

    @Test
    public void doubleDir() throws Exception {

        final File dir = new Archive()
                .add(Double.MIN_VALUE + "/foo.txt", "min")
                .add(Double.MAX_VALUE + "/foo.txt", "max")
                .add("1123.0/foo.txt", "middle")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(Double.MIN_VALUE), "min");
        assertSection(types.section(Double.MAX_VALUE), "max");
        assertSection(types.section((double) 1123), "middle");
    }

    @Test
    public void enumDir() throws Exception {

        final File dir = new Archive()
                .add( "HOURS/foo.txt", "horas")
                .add( "MINUTES/foo.txt", "minutos")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(TimeUnit.HOURS), "horas");
        assertSection(types.section(TimeUnit.MINUTES), "minutos");
    }

    @Test
    public void objectToString() throws Exception {

        final File dir = new Archive()
                .add( "orange/foo.txt", "naranja")
                .add( "red/foo.txt", "rojo")
                .toDir();

        final Types types = Dir.of(Types.class, dir);

        assertSection(types.section(new Color("orange")), "naranja");
        assertSection(types.section(new Color("red")), "rojo");
    }

    public static class Color {
        private final String value;

        public Color(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static void assertSection(final Section section, final String expected) throws IOException {
        final String actual = IO.slurp(section.file("foo.txt"));
        assertEquals(expected, actual);
    }

    public String path(final File file) {
        if (file.isDirectory()) return file.getAbsolutePath() + "/";
        else return file.getAbsolutePath();
    }

    private List<String> paths(final File dir, final List<File> list) {
        final int trim = path(dir).length() - 1;
        return list.stream()
                .map(this::path)
                .map(s -> s.substring(trim))
                .sorted()
                .collect(Collectors.toList());
    }

    public interface Types extends Dir {
        Section section(String name);

        Section section(boolean name);

        Section section(byte name);

        Section section(char name);

        Section section(short name);

        Section section(int name);

        Section section(long name);

        Section section(double name);

        Section section(float name);

        Section section(TimeUnit name);

        Section section(Color name);
    }

    public interface Section extends Dir {

    }
}
