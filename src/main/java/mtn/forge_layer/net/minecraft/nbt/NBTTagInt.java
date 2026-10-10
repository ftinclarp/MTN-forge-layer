package mtn.forge_layer.net.minecraft.nbt;

/**
 * Dummy stand-in for {@code net.minecraft.nbt.NBTTagInt} (Forge 1.7.10).
 * Trivial int wrapper — enough to compile typical {@code new NBTTagInt(...)}.
 */
public class NBTTagInt {
    private final int value;

    public NBTTagInt(int value) {
        this.value = value;
    }

    public int getInt() {
        return value;
    }

    @Override
    public String toString() {
        return "NBTTagInt(" + value + ")";
    }
}
