package mtn.forge_layer.cpw.mods.fml.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;

/**
 * Convenience helpers for writing to / reading from {@link ByteBuf}
 * (Forge 1.7.10 {@code cpw.mods.fml.common.network.ByteBufUtils}).
 * Matches the real Forge 1.7.10 signatures (ByteBuf, not FriendlyByteBuf).
 */
public class ByteBufUtils {

    private ByteBufUtils() {
    }

    public static void writeUTF8String(ByteBuf buf, String s) {
        new FriendlyByteBuf(buf).writeUtf(s == null ? "" : s);
    }

    public static String readUTF8String(ByteBuf buf) {
        return new FriendlyByteBuf(buf).readUtf();
    }

    public static void writeTag(ByteBuf buf, CompoundTag tag) {
        new FriendlyByteBuf(buf).writeNbt(tag);
    }

    public static CompoundTag readTag(ByteBuf buf) {
        return new FriendlyByteBuf(buf).readNbt();
    }
}
