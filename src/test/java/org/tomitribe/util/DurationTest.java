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
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.tomitribe.util.Join.join;

public class DurationTest extends TestCase {
    public void testLongTimeDuration() {
        assertEquals(2592000000L, Duration.parse(new Duration("30 days").getTime(MILLISECONDS) + "ms").getTime());
    }

    public void test() throws Exception {

        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000ms"));
        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000 ms"));
        assertEquals(new Duration(1000, MILLISECONDS), new Duration("1000  ms"));

        assertEquals(new Duration(60, SECONDS), new Duration("1m"));
        assertEquals(new Duration(3600, SECONDS), new Duration("1h"));
        assertEquals(new Duration(86400, SECONDS), new Duration("1d"));

        assertEquals(new Duration(1000, MICROSECONDS), new Duration("1000 microseconds"));
        assertEquals(new Duration(1000, NANOSECONDS), new Duration("1000 nanoseconds"));

        assertEquals(new Duration(1, null), new Duration("1"));
        assertEquals(new Duration(234, null), new Duration("234"));
        assertEquals(new Duration(123, null), new Duration("123"));
        assertEquals(new Duration(-1, null), new Duration("-1"));
    }

    public void testUnitConversion() throws Exception {
        assertEquals(2 * 1000, MILLISECONDS.convert(2, SECONDS));
        assertEquals(2 * 1000 * 1000, MICROSECONDS.convert(2, SECONDS));
        assertEquals(2 * 1000 * 1000 * 1000, NANOSECONDS.convert(2, SECONDS));

        assertEquals(2, SECONDS.convert(2 * 1000 * 1000 * 1000, NANOSECONDS));
        assertEquals(2, SECONDS.convert(2 * 1000 * 1000, MICROSECONDS));
        assertEquals(2, SECONDS.convert(2 * 1000, MILLISECONDS));

        // The verbose way of doing the above
        assertEquals(2 * 1000, new Duration(2, SECONDS).getTime(MILLISECONDS));
        assertEquals(2 * 1000 * 1000, new Duration(2, SECONDS).getTime(MICROSECONDS));
        assertEquals(2 * 1000 * 1000 * 1000, new Duration(2, SECONDS).getTime(NANOSECONDS));

        assertEquals(2, new Duration(2 * 1000 * 1000 * 1000, NANOSECONDS).getTime(SECONDS));
        assertEquals(2, new Duration(2 * 1000 * 1000, MICROSECONDS).getTime(SECONDS));
        assertEquals(2, new Duration(2 * 1000, MILLISECONDS).getTime(SECONDS));
    }

    public void testMultiple() throws Exception {

        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds and 300 milliseconds"));
        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds, 300 milliseconds"));
        assertEquals(new Duration(2300, MILLISECONDS), Duration.parse("2 seconds,300 milliseconds"));

        assertEquals(new Duration(125, SECONDS), Duration.parse("2 minutes and 5 seconds"));

    }

    public void testDefaultDuration() throws Exception {
        assertEquals(new Duration(15, SECONDS), new Duration("15 seconds", NANOSECONDS));

        assertEquals(new Duration(15, null), new Duration("1 and 2 and 3 and 4 and 5"));
        assertEquals(new Duration(15, NANOSECONDS), new Duration("1 and 2 and 3 and 4 and 5", NANOSECONDS));
        assertEquals(new Duration(15, MILLISECONDS), new Duration("1 and 2 and 3 and 4 and 5", MILLISECONDS));
        assertEquals(new Duration(15, SECONDS), new Duration("1 and 2 and 3 and 4 and 5", SECONDS));
        assertEquals(new Duration(15, MINUTES), new Duration("1 and 2 and 3 and 4 and 5", MINUTES));
        assertEquals(new Duration(15, HOURS), new Duration("1 and 2 and 3 and 4 and 5", HOURS));

        for (final TimeUnit defaultUnit : TimeUnit.values()) {
            final Duration expected = new Duration(0 +
                    DAYS.toNanos(1) +
                    defaultUnit.toNanos(2) +
                    HOURS.toNanos(3) +
                    defaultUnit.toNanos(4) +
                    MINUTES.toNanos(5) +
                    defaultUnit.toNanos(6) +
                    SECONDS.toNanos(7) +
                    defaultUnit.toNanos(8) +
                    MILLISECONDS.toNanos(9) +
                    defaultUnit.toNanos(10) +
                    MICROSECONDS.toNanos(11) +
                    defaultUnit.toNanos(12) +
                    NANOSECONDS.toNanos(13)
                    , NANOSECONDS);
            final Duration actual = new Duration("1d and 2 and 3hr and 4 and 5min and 6 and 7s and 8 and 9ms and 10 and 11micros and 12 and 13ns", defaultUnit);

            assertEquals(expected, actual);
        }
    }

    public void testEverySingleImplementedAbbreviation() {
        assertEquals(new Duration(3, TimeUnit.NANOSECONDS), Duration.parse("3 NANOSECONDS"));
        assertEquals(new Duration(3, TimeUnit.NANOSECONDS), Duration.parse("3 NANOSECOND"));
        assertEquals(new Duration(3, TimeUnit.NANOSECONDS), Duration.parse("3 NANOS"));
        assertEquals(new Duration(3, TimeUnit.NANOSECONDS), Duration.parse("3 NANO"));
        assertEquals(new Duration(3, TimeUnit.NANOSECONDS), Duration.parse("3 NS"));

        assertEquals(new Duration(3, TimeUnit.MICROSECONDS), Duration.parse("3 MICROSECONDS"));
        assertEquals(new Duration(3, TimeUnit.MICROSECONDS), Duration.parse("3 MICROSECOND"));
        assertEquals(new Duration(3, TimeUnit.MICROSECONDS), Duration.parse("3 MICROS"));
        assertEquals(new Duration(3, TimeUnit.MICROSECONDS), Duration.parse("3 MICRO"));

        assertEquals(new Duration(3, TimeUnit.MILLISECONDS), Duration.parse("3 MILLISECONDS"));
        assertEquals(new Duration(3, TimeUnit.MILLISECONDS), Duration.parse("3 MILLISECOND"));
        assertEquals(new Duration(3, TimeUnit.MILLISECONDS), Duration.parse("3 MILLIS"));
        assertEquals(new Duration(3, TimeUnit.MILLISECONDS), Duration.parse("3 MILLI"));
        assertEquals(new Duration(3, TimeUnit.MILLISECONDS), Duration.parse("3 MS"));

        assertEquals(new Duration(3, TimeUnit.SECONDS), Duration.parse("3 SECONDS"));
        assertEquals(new Duration(3, TimeUnit.SECONDS), Duration.parse("3 SECOND"));
        assertEquals(new Duration(3, TimeUnit.SECONDS), Duration.parse("3 SEC"));
        assertEquals(new Duration(3, TimeUnit.SECONDS), Duration.parse("3 S"));

        assertEquals(new Duration(3, TimeUnit.MINUTES), Duration.parse("3 MINUTES"));
        assertEquals(new Duration(3, TimeUnit.MINUTES), Duration.parse("3 MINUTE"));
        assertEquals(new Duration(3, TimeUnit.MINUTES), Duration.parse("3 MIN"));
        assertEquals(new Duration(3, TimeUnit.MINUTES), Duration.parse("3 M"));

        assertEquals(new Duration(3, TimeUnit.HOURS), Duration.parse("3 HOURS"));
        assertEquals(new Duration(3, TimeUnit.HOURS), Duration.parse("3 HOUR"));
        assertEquals(new Duration(3, TimeUnit.HOURS), Duration.parse("3 HRS"));
        assertEquals(new Duration(3, TimeUnit.HOURS), Duration.parse("3 HR"));
        assertEquals(new Duration(3, TimeUnit.HOURS), Duration.parse("3 H"));

        assertEquals(new Duration(3, TimeUnit.DAYS), Duration.parse("3 DAYS"));
        assertEquals(new Duration(3, TimeUnit.DAYS), Duration.parse("3 DAY"));
        assertEquals(new Duration(3, TimeUnit.DAYS), Duration.parse("3 D"));
    }

    public void testComparable() throws Exception {
        final Duration[] expected = {
                new Duration("2 nanosecond"),
                new Duration("10 nanosecond"),
                new Duration("2 microsecond"),
                new Duration("10 microsecond"),
                new Duration("2 millisecond"),
                new Duration("10 millisecond"),
                new Duration("2 second"),
                new Duration("10 second"),
                new Duration("2 minute"),
                new Duration("10 minute"),
                new Duration("2 hour"),
                new Duration("10 hour"),
                new Duration("2 day"),
                new Duration("10 day")
        };
        final Duration[] actual = {
                new Duration("10 second"),
                new Duration("10 minute"),
                new Duration("2 hour"),
                new Duration("2 microsecond"),
                new Duration("10 millisecond"),
                new Duration("10 hour"),
                new Duration("10 microsecond"),
                new Duration("2 minute"),
                new Duration("10 nanosecond"),
                new Duration("2 second"),
                new Duration("2 day"),
                new Duration("2 millisecond"),
                new Duration("2 nanosecond"),
                new Duration("10 day")
        };

        Assert.assertNotEquals(join("\n", expected), join("\n", actual));

        Arrays.sort(actual);

        Assert.assertEquals(join("\n", expected), join("\n", actual));
    }
}
