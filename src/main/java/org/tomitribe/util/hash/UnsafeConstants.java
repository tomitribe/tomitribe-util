package org.tomitribe.util.hash;

import static sun.misc.Unsafe.getUnsafe;

@SuppressWarnings("restriction")
public final class UnsafeConstants {

    public static final int ARRAY_BOOLEAN_BASE_OFFSET = getUnsafe().arrayBaseOffset(boolean[].class);
    public static final int ARRAY_BYTE_BASE_OFFSET = getUnsafe().arrayBaseOffset(byte[].class);
    public static final int ARRAY_SHORT_BASE_OFFSET = getUnsafe().arrayBaseOffset(short[].class);
    public static final int ARRAY_CHAR_BASE_OFFSET = getUnsafe().arrayBaseOffset(char[].class);
    public static final int ARRAY_INT_BASE_OFFSET = getUnsafe().arrayBaseOffset(int[].class);
    public static final int ARRAY_LONG_BASE_OFFSET = getUnsafe().arrayBaseOffset(long[].class);
    public static final int ARRAY_FLOAT_BASE_OFFSET = getUnsafe().arrayBaseOffset(float[].class);
    public static final int ARRAY_DOUBLE_BASE_OFFSET = getUnsafe().arrayBaseOffset(double[].class);
    public static final int ARRAY_OBJECT_BASE_OFFSET = getUnsafe().arrayBaseOffset(Object[].class);
    public static final int ARRAY_BOOLEAN_INDEX_SCALE = getUnsafe().arrayIndexScale(boolean[].class);
    public static final int ARRAY_BYTE_INDEX_SCALE = getUnsafe().arrayIndexScale(byte[].class);
    public static final int ARRAY_SHORT_INDEX_SCALE = getUnsafe().arrayIndexScale(short[].class);
    public static final int ARRAY_CHAR_INDEX_SCALE = getUnsafe().arrayIndexScale(char[].class);
    public static final int ARRAY_INT_INDEX_SCALE = getUnsafe().arrayIndexScale(int[].class);
    public static final int ARRAY_LONG_INDEX_SCALE = getUnsafe().arrayIndexScale(long[].class);
    public static final int ARRAY_FLOAT_INDEX_SCALE = getUnsafe().arrayIndexScale(float[].class);
    public static final int ARRAY_DOUBLE_INDEX_SCALE = getUnsafe().arrayIndexScale(double[].class);
    public static final int ARRAY_OBJECT_INDEX_SCALE = getUnsafe().arrayIndexScale(Object[].class);

    private UnsafeConstants() {
    }
}
