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

import java.util.concurrent.TimeUnit;


public class TimeUtils {

    private TimeUtils() {
        // no-op
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     */
    public static String formatMillis(final long duration) {
        return format(duration, TimeUnit.MILLISECONDS, min(), max());
    }


    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String formatMillis(final long duration, final TimeUnit min, final TimeUnit max) {
        return format(duration, TimeUnit.MILLISECONDS, min, max);
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String formatNanos(final long duration, final TimeUnit min, final TimeUnit max) {
        return format(duration, TimeUnit.NANOSECONDS, min, max);
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     */
    public static String formatNanos(final long duration, final TimeUnit min) {
        return format(duration, TimeUnit.NANOSECONDS, min, max());
    }

    public static String format(final long duration, final TimeUnit sourceUnit, final TimeUnit min) {
        return format(duration, sourceUnit, min, max());
    }

    public static String format(final long duration, final TimeUnit sourceUnit) {
        return format(duration, sourceUnit, min(), max());
    }

    private static TimeUnit max() {
        final TimeUnit[] values = TimeUnit.values();
        return values[values.length - 1];
    }

    private static TimeUnit min() {
        return TimeUnit.values()[0];
    }

    /**
     * Converts time to a human readable abbreviate within the specified range
     *
     * @param duration the time in milliseconds to be converted
     */
    public static String abbreviateMillis(final long duration) {
        return abbreviate(duration, TimeUnit.MILLISECONDS, min(), max());
    }

    /**
     * Converts time to a human readable abbreviate within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String abbreviateMillis(final long duration, final TimeUnit min, final TimeUnit max) {
        return abbreviate(duration, TimeUnit.MILLISECONDS, min, max);
    }

    /**
     * Converts time to a human readable abbreviate within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     * @param max      the highest time unit of interest
     */
    public static String abbreviateNanos(final long duration, final TimeUnit min, final TimeUnit max) {
        return abbreviate(duration, TimeUnit.NANOSECONDS, min, max);
    }

    /**
     * Converts time to a human readable abbreviate within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param min      the lowest time unit of interest
     */
    public static String abbreviateNanos(final long duration, final TimeUnit min) {
        return abbreviate(duration, TimeUnit.NANOSECONDS, min, max());
    }

    public static String abbreviate(final long duration, final TimeUnit sourceUnit, final TimeUnit min) {
        return abbreviate(duration, sourceUnit, min, max());
    }

    public static String abbreviate(final long duration, final TimeUnit sourceUnit) {
        return abbreviate(duration, sourceUnit, min(), max());
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration   the time to be converted
     * @param sourceUnit the unit representing this time
     * @param min        the lowest time unit of interest
     * @param max        the highest time unit of interest
     */
    public static String abbreviate(long duration, final TimeUnit sourceUnit, final TimeUnit min, final TimeUnit max) {
        String format = format(duration, sourceUnit, min, max);
        return abbreviate(format);
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration   the time to be converted
     * @param sourceUnit the unit representing this time
     * @param min        the lowest time unit of interest
     * @param max        the highest time unit of interest
     */
    public static String format(long duration, final TimeUnit sourceUnit, final TimeUnit min, final TimeUnit max) {
        final StringBuilder res = new StringBuilder();

        String suffix = "";
        if (duration < 0) {
            duration = duration * -1;
            suffix = " ago";
        }

        TimeUnit current = max;

        while (duration > 0) {
            final long temp = current.convert(duration, sourceUnit);

            if (temp > 0) {

                duration -= sourceUnit.convert(temp, current);

                res.append(temp).append(" ").append(current.name().toLowerCase());

                if (temp < 2) {
                    res.deleteCharAt(res.length() - 1);
                }

                res.append(", ");
            }

            if (current == min) {
                break;
            }

            current = TimeUnit.values()[current.ordinal() - 1];
        }

        // we never got a hit, the time is lower than we care about
        if (res.lastIndexOf(", ") < 0) {
            return "0 " + min.name().toLowerCase();
        }

        // yank trailing  ", "
        res.deleteCharAt(res.length() - 1);
        res.deleteCharAt(res.length() - 1);

        //  convert last ", " to " and"
        final int i = res.lastIndexOf(", ");
        if (i > 0) {
            res.deleteCharAt(i);
            res.insert(i, " and");
        }

        res.append(suffix);

        return res.toString();
    }

    /**
     * Converts time to a human readable format within the specified range
     *
     * @param duration the time in milliseconds to be converted
     * @param max      the highest time unit of interest
     */
    public static String formatHighest(long duration, final TimeUnit max) {
        final TimeUnit[] units = TimeUnit.values();

        final StringBuilder res = new StringBuilder();

        TimeUnit current = max;

        while (duration > 0) {
            final long temp = current.convert(duration, TimeUnit.MILLISECONDS);

            if (temp > 0) {

                duration -= current.toMillis(temp);

                res.append(temp).append(" ").append(current.name().toLowerCase());

                if (temp < 2) {
                    res.deleteCharAt(res.length() - 1);
                }

                break;
            }

            if (current == TimeUnit.MILLISECONDS) {
                break;
            }

            current = units[(current.ordinal() - 1)];
        }

        // we never got a hit, the time is lower than we care about
        return res.toString();
    }

    public static String abbreviate(String time) {
        time = time.replaceAll(" days", "d");
        time = time.replaceAll(" day", "d");
        time = time.replaceAll(" hours", "hr");
        time = time.replaceAll(" hour", "hr");
        time = time.replaceAll(" minutes", "m");
        time = time.replaceAll(" minute", "m");
        time = time.replaceAll(" seconds", "s");
        time = time.replaceAll(" second", "s");
        time = time.replaceAll(" milliseconds", "ms");
        time = time.replaceAll(" millisecond", "ms");
        return time;
    }

    public static String daysAndMinutes(final long duration) {
        return formatMillis(duration, TimeUnit.MINUTES, TimeUnit.DAYS);
    }

    public static String hoursAndMinutes(final long duration) {
        return formatMillis(duration, TimeUnit.MINUTES, TimeUnit.HOURS);
    }

    public static String hoursAndSeconds(final long duration) {
        return formatMillis(duration, TimeUnit.SECONDS, TimeUnit.HOURS);
    }
}
