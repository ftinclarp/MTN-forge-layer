package mtn.forge_layer.net.minecraft.nbt;

/**
 * Dummy stand-in for {@code net.minecraft.nbt.NBTTagByte} (Forge 1.7.10).
 * Trivial byte wrapper — enough to compile typical {@code new NBTTagByte(...)}.
 */
public class NBTTagByte {
    private final byte value;

    public NBTTagByte(byte value) {
        this.value = value;
    }

    public byte getByte() {
        return value;
    }

    @Override
    public String toString() {
        return "NBTTagByte(" + value + ")";
    }
}
