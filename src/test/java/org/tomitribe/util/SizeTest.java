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

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Arrays;

import static org.tomitribe.util.Join.join;
import static org.tomitribe.util.SizeUnit.*;

public class SizeTest extends TestCase {

    public void test() throws Exception {

        assertEquals(new Size(3, BYTES), new Size("3 bytes"));
        assertEquals(new Size(3, BYTES), new Size("3bytes"));
        assertEquals(new Size(3, BYTES), new Size("3byte"));
        assertEquals(new Size(3, BYTES), new Size("3b"));

        assertEquals(new Size(4, KILOBYTES), new Size("4 kilobytes"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kilobytes"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kilobyte"));
        assertEquals(new Size(4, KILOBYTES), new Size("4kb"));
        assertEquals(new Size(4, KILOBYTES), new Size("4k"));

        assertEquals(new Size(5, MEGABYTES), new Size("5 megabytes"));
        assertEquals(new Size(5, MEGABYTES), new Size("5megabyte"));
        assertEquals(new Size(5, MEGABYTES), new Size("5mb"));
        assertEquals(new Size(5, MEGABYTES), new Size("5m"));

        assertEquals(new Size(6, GIGABYTES), new Size("6 gigabytes"));
        assertEquals(new Size(6, GIGABYTES), new Size("6gigabyte"));
        assertEquals(new Size(6, GIGABYTES), new Size("6gb"));
        assertEquals(new Size(6, GIGABYTES), new Size("6g"));

        assertEquals(new Size(7, TERABYTES), new Size("7 terabytes"));
        assertEquals(new Size(7, TERABYTES), new Size("7 terabyte"));
        assertEquals(new Size(7, TERABYTES), new Size("7 tb"));
        assertEquals(new Size(7, TERABYTES), new Size("7 t"));


        assertEquals(new Size(1, null), new Size("1"));
        assertEquals(new Size(234, null), new Size("234"));
        assertEquals(new Size(123, null), new Size("123"));
        assertEquals(new Size(-1, null), new Size("-1"));
    }

    public void testDecimals() throws Exception {
        assertEquals(new Size(1234223277, BYTES), new Size("1.14946gb"));
        assertEquals(new Size("1.20224609", GIGABYTES).getSize(KILOBYTES), new Size("1.2gb and 2.3mb").getSize(KILOBYTES));
    }

    public void testUnitConversion() throws Exception {
        assertEquals(3, new Size(3, BYTES).getSize(BYTES));
        assertEquals(3072, new Size(3, KILOBYTES).getSize(BYTES));
        assertEquals(3145728, new Size(3, MEGABYTES).getSize(BYTES));
        assertEquals(3221225472l, new Size(3, GIGABYTES).getSize(BYTES));
        assertEquals(3298534883328l, new Size(3, TERABYTES).getSize(BYTES));

        assertEquals(3, new Size(3072, BYTES).getSize(KILOBYTES));
        assertEquals(3, new Size(3, KILOBYTES).getSize(KILOBYTES));
        assertEquals(3072, new Size(3, MEGABYTES).getSize(KILOBYTES));
        assertEquals(3145728, new Size(3, GIGABYTES).getSize(KILOBYTES));
        assertEquals(3221225472l, new Size(3, TERABYTES).getSize(KILOBYTES));

        assertEquals(3, new Size(3145728, BYTES).getSize(MEGABYTES));
        assertEquals(3, new Size(3072, KILOBYTES).getSize(MEGABYTES));
        assertEquals(3, new Size(3, MEGABYTES).getSize(MEGABYTES));
        assertEquals(3072, new Size(3, GIGABYTES).getSize(MEGABYTES));
        assertEquals(3145728, new Size(3, TERABYTES).getSize(MEGABYTES));

        assertEquals(3, new Size(3221225472l, BYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3145728, KILOBYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3072, MEGABYTES).getSize(GIGABYTES));
        assertEquals(3, new Size(3, GIGABYTES).getSize(GIGABYTES));
        assertEquals(3072, new Size(3, TERABYTES).getSize(GIGABYTES));

        assertEquals(3, new Size(3298534883328l, BYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3221225472l, KILOBYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3145728, MEGABYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3072, GIGABYTES).getSize(TERABYTES));
        assertEquals(3, new Size(3, TERABYTES).getSize(TERABYTES));
    }

    public void testMultiple() throws Exception {
        assertEquals(new Size(1101662261253l, BYTES), Size.parse("1tb and 2gb and 3mb and 4kb and 5 bytes"));
        assertEquals(new Size(1101662261253l, BYTES), Size.parse("1 TB and 2 gb and 3 mb and 4 kb and 5 bytes"));
    }

    public void testDefaultUnit() throws Exception {
        assertEquals(new Size(15, MEGABYTES), new Size("15 megabytes", BYTES));

        assertEquals(new Size(15, null), new Size("1 and 2 and 3 and 4 and 5"));
        assertEquals(new Size(15, BYTES), new Size("1 and 2 and 3 and 4 and 5", BYTES));
        assertEquals(new Size(15, KILOBYTES), new Size("1 and 2 and 3 and 4 and 5", KILOBYTES));
        assertEquals(new Size(15, MEGABYTES), new Size("1 and 2 and 3 and 4 and 5", MEGABYTES));
        assertEquals(new Size(15, GIGABYTES), new Size("1 and 2 and 3 and 4 and 5", GIGABYTES));
        assertEquals(new Size(15, TERABYTES), new Size("1 and 2 and 3 and 4 and 5", TERABYTES));

        assertEquals(new Size(1102738096134l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", BYTES));
        assertEquals(new Size(1102738102272l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", KILOBYTES));
        assertEquals(new Size(1102744387584l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", MEGABYTES));
        assertEquals(new Size(1109180547073l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb and 1byte", GIGABYTES));
        assertEquals(new Size(7343109, MEGABYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", TERABYTES));
        assertEquals(new Size(7343109 * 1024l * 1024l, BYTES), new Size("1tb and 2 and 3gb and 4 and 5mb", TERABYTES));
    }

    public void testEverySinglePossibleAbbrevation() {
        assertEquals(new Size(3, SizeUnit.BYTES), Size.parse("3 BYTES"));
        assertEquals(new Size(3, SizeUnit.BYTES), Size.parse("3 BYTE"));
        assertEquals(new Size(3, SizeUnit.BYTES), Size.parse("3 B"));

        assertEquals(new Size(3, SizeUnit.KILOBYTES), Size.parse("3 KILOBYTES"));
        assertEquals(new Size(3, SizeUnit.KILOBYTES), Size.parse("3 KILOBYTE"));
        assertEquals(new Size(3, SizeUnit.KILOBYTES), Size.parse("3 KB"));
        assertEquals(new Size(3, SizeUnit.KILOBYTES), Size.parse("3 K"));

        assertEquals(new Size(3, SizeUnit.MEGABYTES), Size.parse("3 MEGABYTES"));
        assertEquals(new Size(3, SizeUnit.MEGABYTES), Size.parse("3 MEGABYTE"));
        assertEquals(new Size(3, SizeUnit.MEGABYTES), Size.parse("3 MB"));
        assertEquals(new Size(3, SizeUnit.MEGABYTES), Size.parse("3 M"));

        assertEquals(new Size(3, SizeUnit.GIGABYTES), Size.parse("3 GIGABYTES"));
        assertEquals(new Size(3, SizeUnit.GIGABYTES), Size.parse("3 GIGABYTE"));
        assertEquals(new Size(3, SizeUnit.GIGABYTES), Size.parse("3 GB"));
        assertEquals(new Size(3, SizeUnit.GIGABYTES), Size.parse("3 G"));

        assertEquals(new Size(3, SizeUnit.TERABYTES), Size.parse("3 TERABYTES"));
        assertEquals(new Size(3, SizeUnit.TERABYTES), Size.parse("3 TERABYTE"));
        assertEquals(new Size(3, SizeUnit.TERABYTES), Size.parse("3 TB"));
        assertEquals(new Size(3, SizeUnit.TERABYTES), Size.parse("3 T"));
    }

    public void testComparable() throws Exception {
        final Size[] expected = {
                new Size("2b"),
                new Size("10b"),
                new Size("2kb"),
                new Size("10kb"),
                new Size("2mb"),
                new Size("10mb"),
                new Size("2gb"),
                new Size("10gb"),
                new Size("2tb"),
                new Size("10tb")
        };
        final Size[] actual = {
                new Size("2b"),
                new Size("10gb"),
                new Size("2kb"),
                new Size("10kb"),
                new Size("10b"),
                new Size("10tb"),
                new Size("2mb"),
                new Size("2gb"),
                new Size("10mb"),
                new Size("2tb"),
        };

        Assert.assertNotEquals(join("\n", expected), join("\n", actual));

        Arrays.sort(actual);

        Assert.assertEquals(join("\n", expected), join("\n", actual));
    }

}
