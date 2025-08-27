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

import static java.lang.Long.MAX_VALUE;

public enum SizeUnit {
    BYTES {
        public long toBytes(final long s) {
            return s;
        }

        public long toKilobytes(final long s) {
            return s / (B1 / B0);
        }

        public long toMegabytes(final long s) {
            return s / (B2 / B0);
        }

        public long toGigabytes(final long s) {
            return s / (B3 / B0);
        }

        public long toTerabytes(final long s) {
            return s / (B4 / B0);
        }

        public long convert(final long s, final SizeUnit u) {
            return u.toBytes(s);
        }
    },

    KILOBYTES {
        public long toBytes(final long s) {
            return x(s, B1 / B0, MAX_VALUE / (B1 / B0));
        }

        public long toKilobytes(final long s) {
            return s;
        }

        public long toMegabytes(final long s) {
            return s / (B2 / B1);
        }

        public long toGigabytes(final long s) {
            return s / (B3 / B1);
        }

        public long toTerabytes(final long s) {
            return s / (B4 / B1);
        }

        public long convert(final long s, final SizeUnit u) {
            return u.toKilobytes(s);
        }
    },

    MEGABYTES {
        public long toBytes(final long s) {
            return x(s, B2 / B0, MAX_VALUE / (B2 / B0));
        }

        public long toKilobytes(final long s) {
            return x(s, B2 / B1, MAX_VALUE / (B2 / B1));
        }

        public long toMegabytes(final long s) {
            return s;
        }

        public long toGigabytes(final long s) {
            return s / (B3 / B2);
        }

        public long toTerabytes(final long s) {
            return s / (B4 / B2);
        }

        public long convert(final long s, final SizeUnit u) {
            return u.toMegabytes(s);
        }
    },

    GIGABYTES {
        public long toBytes(final long s) {
            return x(s, B3 / B0, MAX_VALUE / (B3 / B0));
        }

        public long toKilobytes(final long s) {
            return x(s, B3 / B1, MAX_VALUE / (B3 / B1));
        }

        public long toMegabytes(final long s) {
            return x(s, B3 / B2, MAX_VALUE / (B3 / B2));
        }

        public long toGigabytes(final long s) {
            return s;
        }

        public long toTerabytes(final long s) {
            return s / (B4 / B3);
        }

        public long convert(final long s, final SizeUnit u) {
            return u.toGigabytes(s);
        }
    },

    TERABYTES {
        public long toBytes(final long s) {
            return x(s, B4 / B0, MAX_VALUE / (B4 / B0));
        }

        public long toKilobytes(final long s) {
            return x(s, B4 / B1, MAX_VALUE / (B4 / B1));
        }

        public long toMegabytes(final long s) {
            return x(s, B4 / B2, MAX_VALUE / (B4 / B2));
        }

        public long toGigabytes(final long s) {
            return x(s, B4 / B3, MAX_VALUE / (B4 / B3));
        }

        public long toTerabytes(final long s) {
            return s;
        }

        public long convert(final long s, final SizeUnit u) {
            return u.toTerabytes(s);
        }
    };

    static final long B0 = 1L;
    static final long B1 = B0 * 1024L;
    static final long B2 = B1 * 1024L;
    static final long B3 = B2 * 1024L;
    static final long B4 = B3 * 1024L;


    static long x(final long d, final long m, final long over) {
        if (d > over) {
            return MAX_VALUE;
        }
        if (d < -over) {
            return Long.MIN_VALUE;
        }
        return d * m;
    }

    public long toBytes(final long size) {
        throw new AbstractMethodError();
    }

    public long toKilobytes(final long size) {
        throw new AbstractMethodError();
    }

    public long toMegabytes(final long size) {
        throw new AbstractMethodError();
    }

    public long toGigabytes(final long size) {
        throw new AbstractMethodError();
    }

    public long toTerabytes(final long size) {
        throw new AbstractMethodError();
    }

    public long convert(final long sourceSize, final SizeUnit sourceUnit) {
        throw new AbstractMethodError();
    }


}
