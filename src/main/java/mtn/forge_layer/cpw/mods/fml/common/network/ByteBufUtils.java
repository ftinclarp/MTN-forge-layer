package mtn.forge_layer.cpw.mods.fml.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.item.ItemStack;
import mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound;

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

    /**
     * Forge 1.7.10 {@code ByteBufUtils.writeItemStack}: an item-stack serialized
     * through the layer item's registered id. Matches the real Forge short/short
     * layout (item id, size, damage, tag).
     */
    public static void writeItemStack(ByteBuf buf, ItemStack stack) {
        int itemId = stack == null || stack.getItem() == null
                ? -1
                : Item.getIdFromItem(stack.getItem());
        buf.writeShort(itemId);
        if (itemId < 0) {
            return;
        }
        buf.writeByte(stack.stackSize);
        buf.writeShort(stack.getItemDamage());
        NBTTagCompound forgeTag = stack.getTagCompound();
        writeTag(buf, forgeTag == null ? null : mtn.forge_layer.registry.NbtBridge.toFabric(forgeTag));
    }

    /**
     * Forge 1.7.10 {@code ByteBufUtils.readItemStack}: the inverse of
     * {@link #writeItemStack(ByteBuf, ItemStack)}.
     */
    public static ItemStack readItemStack(ByteBuf buf) {
        short itemId = buf.readShort();
        if (itemId < 0) {
            return null;
        }
        byte size = buf.readByte();
        short damage = buf.readShort();
        CompoundTag tag = readTag(buf);
        Item item = Item.getItemById(itemId);
        ItemStack stack = new ItemStack(item, size, damage);
        if (tag != null) {
            stack.setTagCompound(mtn.forge_layer.registry.NbtBridge.toForge(tag));
        }
        return stack;
    }
}
