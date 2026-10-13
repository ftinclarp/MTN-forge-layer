package net.minecraftforge.common.util;

/**
 * Dummy stand-in for {@code net.minecraftforge.common.util.Constants}
 * (Forge 1.7.10). NBT tag type constants.
 */
public class Constants {
    /**
     * Dummy stand-in for {@code Constants.NBT}.
     */
    public static final class NBT {
        public static final int TAG_END = 0;
        public static final int TAG_BYTE = 1;
        public static final int TAG_SHORT = 2;
        public static final int TAG_INT = 3;
        public static final int TAG_LONG = 4;
        public static final int TAG_FLOAT = 5;
        public static final int TAG_DOUBLE = 6;
        public static final int TAG_BYTE_ARRAY = 7;
        public static final int TAG_STRING = 8;
        public static final int TAG_LIST = 9;
        public static final int TAG_COMPOUND = 10;
        public static final int TAG_INT_ARRAY = 11;

        private NBT() {
        }
    }

    private Constants() {
    }
}
