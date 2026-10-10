package mtn.forge_layer.cpw.mods.fml.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;

/**
 * Convenience helpers for writing to / reading from {@link FriendlyByteBuf}
 * (Forge 1.7.10 {@code cpw.mods.fml.common.network.ByteBufUtils}).
 */
public class ByteBufUtils {

    private ByteBufUtils() {
    }

    public static void writeUTF8String(FriendlyByteBuf buf, String s) {
        buf.writeUtf(s == null ? "" : s);
    }

    public static String readUTF8String(FriendlyByteBuf buf) {
        return buf.readUtf();
    }

    public static void writeTag(FriendlyByteBuf buf, CompoundTag tag) {
        buf.writeNbt(tag);
    }

    public static CompoundTag readTag(FriendlyByteBuf buf) {
        return buf.readNbt();
    }
}
