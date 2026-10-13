package net.minecraftforge.common.util;

/**
 * Dummy stand-in for {@code net.minecraftforge.common.util.ForgeDirection}
 * (Forge 1.7.10). 6-axis enum used for block rotation.
 */
public enum ForgeDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    ForgeDirection(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    // TODO(mtn-layer): map 0-5 to DOWN, UP, NORTH, SOUTH, WEST, EAST as
    //   Forge does; placeholder returns from ordinal values.
    public static ForgeDirection getOrientation(int id) {
        ForgeDirection[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, id))];
    }

    // TODO(mtn-layer): real rotation matrix as in Forge.
    public ForgeDirection getRotation(ForgeDirection axis) {
        return this;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public ForgeDirection getOpposite() {
        return getOrientation(this.ordinal() ^ 1);
    }
}
