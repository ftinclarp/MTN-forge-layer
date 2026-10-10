package mtn.forge_layer.net.minecraft.item;

import mtn.forge_layer.net.minecraft.nbt.NBTTagCompound;

/**
 * Dummy stand-in for {@code net.minecraft.item.ItemStack} (Forge 1.7.10).
 * Holds an item, a stack count, damage, and an optional NBT tag. Enough to
 * compile and exercise ported mod code; no real Minecraft integration.
 */
public class ItemStack {
    private final Item item;
    private int count;
    private int damage;
    private NBTTagCompound tag;

    public ItemStack(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public ItemStack(Item item, int count, int damage) {
        this.item = item;
        this.count = count;
        this.damage = damage;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int c) {
        count = c;
    }

    public void stackSize(int c) {
        count = c;
    }

    public int getItemDamage() {
        return damage;
    }

    public void setItemDamage(int d) {
        damage = d;
    }

    public NBTTagCompound getTagCompound() {
        return tag;
    }

    public void setTagCompound(NBTTagCompound t) {
        tag = t;
    }

    public boolean hasTagCompound() {
        return tag != null;
    }

    public ItemStack copy() {
        ItemStack s = new ItemStack(item, count, damage);
        s.tag = tag;
        return s;
    }

    public boolean isItemEqual(ItemStack other) {
        return other != null && other.item == item;
    }

    @Override
    public String toString() {
        return "ItemStack{" + item + " x" + count + "}";
    }
}
