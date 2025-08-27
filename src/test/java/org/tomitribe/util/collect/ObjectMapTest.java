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

import java.util.HashMap;
import java.util.Map;

public class ObjectMapTest extends TestCase {

    public void test() throws Exception {
        final Foo foo = new Foo();
        final ObjectMap map = new ObjectMap(foo);

        assertEquals("default", map.get("myString"));
        assertEquals((byte) 1, map.get("mybyte"));
        assertEquals((char) 1, map.get("mychar"));
        assertEquals((short) 1, map.get("myshort"));
        assertEquals((int) 1, map.get("myint"));
        assertEquals((long) 1, map.get("mylong"));
        assertEquals((float) 1, map.get("myfloat"));
        assertEquals((double) 1, map.get("mydouble"));
        assertEquals(true, map.get("myboolean"));

        assertEquals("default", map.put("myString", "newvalue"));
        assertEquals((byte) 1, map.put("mybyte", (byte) 2));
        assertEquals((char) 1, map.put("mychar", (char) 2));
        assertEquals((short) 1, map.put("myshort", (short) 2));
        assertEquals((int) 1, map.put("myint", (int) 2));
        assertEquals((long) 1, map.put("mylong", (long) 2));
        assertEquals((float) 1, map.put("myfloat", (float) 2));
        assertEquals((double) 1, map.put("mydouble", (double) 2));
        assertEquals(true, map.put("myboolean", false));

        assertEquals("newvalue", map.get("myString"));
        assertEquals((byte) 2, map.get("mybyte"));
        assertEquals((char) 2, map.get("mychar"));
        assertEquals((short) 2, map.get("myshort"));
        assertEquals((int) 2, map.get("myint"));
        assertEquals((long) 2, map.get("mylong"));
        assertEquals((float) 2, map.get("myfloat"));
        assertEquals((double) 2, map.get("mydouble"));
        assertEquals(false, map.get("myboolean"));

        assertEquals("newvalue", map.put("myString", "value3"));
        assertEquals((byte) 2, map.put("mybyte", "3"));
        assertEquals((char) 2, map.put("mychar", "3"));
        assertEquals((short) 2, map.put("myshort", "3"));
        assertEquals((int) 2, map.put("myint", "3"));
        assertEquals((long) 2, map.put("mylong", "3"));
        assertEquals((float) 2, map.put("myfloat", "3"));
        assertEquals((double) 2, map.put("mydouble", "3"));
        assertEquals(false, map.put("myboolean", "true"));

        assertEquals("value3", map.get("myString"));
        assertEquals((byte) 3, map.get("mybyte"));
        assertEquals('3', map.get("mychar"));
        assertEquals((short) 3, map.get("myshort"));
        assertEquals((int) 3, map.get("myint"));
        assertEquals((long) 3, map.get("mylong"));
        assertEquals((float) 3, map.get("myfloat"));
        assertEquals((double) 3, map.get("mydouble"));
        assertEquals(true, map.get("myboolean"));
    }

    public void testGetType() throws Exception {
        final Map<String, ObjectMap.Member> map = members(new ObjectMap(new Foo()));
        assertEquals(String.class, map.get("myString").getType());
        assertEquals(byte.class, map.get("mybyte").getType());
        assertEquals(char.class, map.get("mychar").getType());
        assertEquals(short.class, map.get("myshort").getType());
        assertEquals(int.class, map.get("myint").getType());
        assertEquals(long.class, map.get("mylong").getType());
        assertEquals(float.class, map.get("myfloat").getType());
        assertEquals(double.class, map.get("mydouble").getType());
        assertEquals(boolean.class, map.get("myboolean").getType());
    }

    public void testIsReadOnly() throws Exception {
        final Map<String, ObjectMap.Member> map = members(new ObjectMap(new Bar()));
        assertEquals(true, map.get("myString").isReadOnly());
        assertEquals(false, map.get("mybyte").isReadOnly());
        assertEquals(true, map.get("mychar").isReadOnly());
        assertEquals(false, map.get("myshort").isReadOnly());
        assertEquals(true, map.get("myint").isReadOnly());
        assertEquals(false, map.get("mylong").isReadOnly());
        assertEquals(true, map.get("myfloat").isReadOnly());
        assertEquals(false, map.get("mydouble").isReadOnly());
        assertEquals(true, map.get("myboolean").isReadOnly());
    }

    public void testHashMap() throws Exception {
        final HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("one", "uno");

        final Map<String, ObjectMap.Member> map = members(new ObjectMap(hashMap.entrySet().iterator().next()));
        final ObjectMap.Member key = map.get("key");
        assertEquals("one", key.getValue());

        final ObjectMap.Member value = map.get("value");
        assertEquals("uno", value.getValue());
    }
    private Map<String, ObjectMap.Member> members(final ObjectMap objectMap) {
        final Map<String, ObjectMap.Member> map = new HashMap<String, ObjectMap.Member>();
        for (final Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if (entry instanceof ObjectMap.Member) {
                ObjectMap.Member member = (ObjectMap.Member) entry;
                map.put(member.getKey(), member);
            }
        }
        return map;
    }

    public void testReadOnlyKeys() throws Exception {

        final ObjectMap map = new ObjectMap(new ReadOnlyKeys(42, true));

        assertEquals((int) 42, map.get("orange"));
        assertEquals(true, map.get("red"));

        try {
            map.put("orange", 54);
            fail();
        } catch (IllegalArgumentException pass) {
        }

        try {
            map.put("red", true);
            fail();
        } catch (IllegalArgumentException pass) {
        }
    }


    public void testBooleanIsGetter() throws Exception {

        final ObjectMap map = new ObjectMap(new BooleanIsGetter(true, false));

        assertEquals(true, map.get("orange"));
        assertEquals(false, map.get("red"));
        assertEquals(true, map.get("yellow"));

    }

    public void testPlainGetMethod() throws Exception {
        final HasPlainGet object = new HasPlainGet();
        object.setRed(255);
        object.setGreen(165);
        object.setBlue(0);

        final ObjectMap map = new ObjectMap(object);

        assertEquals(255, map.get("red"));
        assertEquals(165, map.get("green"));
        assertEquals(0, map.get("blue"));

    }

    public void testHasStatics() {
        final HasStatics hasStatics = new HasStatics();
        hasStatics.setRed("255");
        hasStatics.setGreen("155");
        hasStatics.setBlue("0");

        final ObjectMap map = new ObjectMap(hasStatics);
        assertEquals("255", map.get("red"));
        assertEquals("155", map.get("green"));
        assertEquals("0", map.get("blue"));

        final String keys = map.keySet().stream().sorted().reduce((s, s2) -> s + ", " + s2).get();
        assertEquals("blue, class, green, red", keys);
    }

    public static class BooleanIsGetter {

        private boolean orange;
        private final boolean red;

        public BooleanIsGetter(boolean orange, boolean red) {
            this.orange = orange;
            this.red = red;
        }

        public boolean isOrange() {
            return orange;
        }

        public boolean isRed() {
            return red;
        }

        public Boolean isYellow() {
            return Boolean.TRUE;
        }

        public void setOrange(boolean orange) {
            this.orange = orange;
        }
    }

    public static class ReadOnlyKeys {

        private final int orange;
        private final boolean red;

        public ReadOnlyKeys(int orange, boolean red) {
            this.orange = orange;
            this.red = red;
        }

        public int getOrange() {
            return orange;
        }

        public boolean getRed() {
            return red;
        }
    }

    public static class HasPlainGet {
        private int red;
        private int green;
        private int blue;

        public HasPlainGet get() {
            return this;
        }

        public int getRed() {
            return red;
        }

        public void setRed(final int red) {
            this.red = red;
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(final int green) {
            this.green = green;
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(final int blue) {
            this.blue = blue;
        }
    }

    public static class Foo {

        private String myString = "default";
        private byte mybyte = 1;
        private char mychar = 1;
        private short myshort = 1;
        private int myint = 1;
        private long mylong = 1;
        private float myfloat = 1;
        private double mydouble = 1;
        private boolean myboolean = true;

        public String getMyString() {
            return myString;
        }

        public void setMyString(final String myString) {
            this.myString = myString;
        }

        public byte getMybyte() {
            return mybyte;
        }

        public void setMybyte(final byte mybyte) {
            this.mybyte = mybyte;
        }

        public char getMychar() {
            return mychar;
        }

        public void setMychar(final char mychar) {
            this.mychar = mychar;
        }

        public short getMyshort() {
            return myshort;
        }

        public void setMyshort(final short myshort) {
            this.myshort = myshort;
        }

        public int getMyint() {
            return myint;
        }

        public void setMyint(final int myint) {
            this.myint = myint;
        }

        public long getMylong() {
            return mylong;
        }

        public void setMylong(final long mylong) {
            this.mylong = mylong;
        }

        public float getMyfloat() {
            return myfloat;
        }

        public void setMyfloat(final float myfloat) {
            this.myfloat = myfloat;
        }

        public double getMydouble() {
            return mydouble;
        }

        public void setMydouble(final double mydouble) {
            this.mydouble = mydouble;
        }

        public boolean getMyboolean() {
            return myboolean;
        }

        public void setMyboolean(final boolean myboolean) {
            this.myboolean = myboolean;
        }
    }

    public static class Bar {

        private String myString = "default";
        private byte mybyte = 1;
        private char mychar = 1;
        private short myshort = 1;
        private int myint = 1;
        private long mylong = 1;
        private float myfloat = 1;
        private double mydouble = 1;
        private boolean myboolean = true;

        public String getMyString() {
            return myString;
        }

        public void setMyString(final String myString) {
            this.myString = myString;
        }

        public byte getMybyte() {
            return mybyte;
        }

        public char getMychar() {
            return mychar;
        }

        public void setMychar(final char mychar) {
            this.mychar = mychar;
        }

        public short getMyshort() {
            return myshort;
        }

        public int getMyint() {
            return myint;
        }

        public void setMyint(final int myint) {
            this.myint = myint;
        }

        public long getMylong() {
            return mylong;
        }


        public float getMyfloat() {
            return myfloat;
        }

        public void setMyfloat(final float myfloat) {
            this.myfloat = myfloat;
        }

        public double getMydouble() {
            return mydouble;
        }

        public boolean getMyboolean() {
            return myboolean;
        }

        public void setMyboolean(final boolean myboolean) {
            this.myboolean = myboolean;
        }
    }

    public static class HasStatics {
        private static final String IGNORE = "ignore";
        private static String ALSO_IGNORE = "also ignore";
        private String red;
        private String green;
        private String blue;

        public static String getIgnore() {
            return IGNORE;
        }

        public static String getAlsoIgnore() {
            return ALSO_IGNORE;
        }

        public static void setAlsoIgnore(final String alsoIgnore) {
            ALSO_IGNORE = alsoIgnore;
        }

        public String getRed() {
            return red;
        }

        public void setRed(final String red) {
            this.red = red;
        }

        public String getGreen() {
            return green;
        }

        public void setGreen(final String green) {
            this.green = green;
        }

        public String getBlue() {
            return blue;
        }

        public void setBlue(final String blue) {
            this.blue = blue;
        }
    }

}
