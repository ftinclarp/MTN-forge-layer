package mtn.forge_layer.registry;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Utility bridge between the Forge-1.7.10-style layer (mtn.minecraft_layer.net.minecraft.*)
 * and vanilla Mojmap NBT/ItemStack used on the Fabric side.
 *
 * <p>The Forge-side {@code NBTTagCompound} stores typed values privately behind an
 * {@code Object} and exposes no raw getter, so conversion to vanilla NBT cannot
 * recover the original value type exactly. {@link #toFabric(NBTTagCompound)} is
 * therefore a documented <em>best-effort</em> conversion: it special-cases the
 * {@code "Items"} key (an {@code NBTTagList} of compound slots, as written by the
 * R2 chest) and for every other key probes the typed getters in a fixed precedence
 * ({@code String}, {@code int}, {@code boolean}, {@code float}, {@code byte},
 * {@code compound}) using the fact that absent keys return defaults.
 *
 * <p>Item / player identity is not mapped: the Forge-side {@code Item} is a dummy
 * carrying only a name, and the Forge-side player/entity stack is unimplemented.
 * A future round extends the minecraft-layer NBTTagCompound with typed raw access
 * and introduces an item registry for a 1:1 mapping.
 */
public final class NbtBridge {

    private NbtBridge() {
        // utility class — no instances
    }

    /**
     * Best-effort conversion of a Forge-side NBTTagCompound to vanilla Mojmap CompoundTag.
     *
     * <p>Because the Forge NBTTagCompound does not expose raw value types, each key is
     * written using the first typed probe whose default-carrying getter reports a
     * non-default value, in the precedence String &gt; int &gt; boolean &gt; float &gt; byte
     * &gt; compound. {@code "Items"} is handled explicitly as a list of compound slots
     * (the layout written by the R2 chest). Values that are zero/empty at every probe
     * degrade to the compound fallback and are therefore not round-tripped faithfully —
     * documented limitation, not a bug.
     */
    public static CompoundTag toFabric(mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound forge) {
        CompoundTag out = new CompoundTag();
        if (forge == null) {
            return out;
        }
        for (String key : forge.getKeySet()) {
            if (key.equals("Items")) {
                // In the R2 chest, "Items" holds an NBTTagList of NBTTagCompound.
                mtn.minecraft_layer.net.minecraft.nbt.NBTTagList forgeList = forge.getTagList(key, 9);
                ListTag fabricList = new ListTag();
                for (int i = 0; i < forgeList.tagCount(); i++) {
                    fabricList.add(toFabric(forgeList.getCompoundTagAt(i)));
                }
                out.put(key, fabricList);
                continue;
            }
            // For everything else, use type probes. The dummy compound stores Object
            // values; getters return defaults (""/0/false/0f/0) when absent, so a
            // non-default probe result implies the stored value is of that kind.
            if (forge.hasKey(key)) {
                // Best-effort: strings round-trip most faithfully for text keys such as
                // "CustomName"; integers for "Slot"/"Count"/tier etc.
                String s = forge.getString(key);
                if (!s.isEmpty()) {
                    out.putString(key, s);
                    continue;
                }
                if (forge.getInteger(key) != 0) {
                    out.putInt(key, forge.getInteger(key));
                    continue;
                }
                if (forge.getBoolean(key)) {
                    out.putBoolean(key, true);
                    continue;
                }
                if (forge.getFloat(key) != 0f) {
                    out.putFloat(key, forge.getFloat(key));
                    continue;
                }
                if (forge.getByte(key) != 0) {
                    out.putByte(key, forge.getByte(key));
                    continue;
                }
                // Compound fallback.
                out.put(key, toFabric(forge.getCompoundTag(key)));
            }
        }
        return out;
    }

    /**
     * Conversion of a vanilla Mojmap CompoundTag to a Forge-side NBTTagCompound.
     * This direction <em>is</em> exact: vanilla NBT tags expose their real types, so
     * every supported tag kind is written with the matching Forge setter. Unsupported
     * kinds (e.g. LongTag, DoubleTag, IntArrayTag, EndTag) are skipped (best effort).
     */
    public static mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound toForge(net.minecraft.nbt.CompoundTag fabric) {
        mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound forge = new mtn.minecraft_layer.net.minecraft.nbt.NBTTagCompound();
        if (fabric == null) {
            return forge;
        }
        for (String key : fabric.getAllKeys()) {
            Tag value = fabric.get(key);
            if (value instanceof StringTag) {
                forge.setString(key, ((StringTag) value).getAsString());
            } else if (value instanceof IntTag) {
                forge.setInteger(key, ((IntTag) value).getAsInt());
            } else if (value instanceof FloatTag) {
                forge.setFloat(key, ((FloatTag) value).getAsFloat());
            } else if (value instanceof ByteTag) {
                // Booleans are stored as bytes (1/0) in NBT; treated as byte (best effort).
                forge.setByte(key, ((ByteTag) value).getAsByte());
            } else if (value instanceof CompoundTag) {
                forge.setTag(key, toForge((CompoundTag) value));
            } else if (value instanceof ListTag) {
                ListTag list = (ListTag) value;
                mtn.minecraft_layer.net.minecraft.nbt.NBTTagList forgeList = new mtn.minecraft_layer.net.minecraft.nbt.NBTTagList();
                for (int i = 0; i < list.size(); i++) {
                    Tag element = list.get(i);
                    if (element instanceof CompoundTag) {
                        forgeList.appendTag(toForge((CompoundTag) element));
                    }
                }
                forge.setTag(key, forgeList);
            }
            // any other tag kind: skipped (best effort)
        }
        return forge;
    }

    /**
     * Best-effort conversion of a Forge-side ItemStack to a vanilla Mojmap ItemStack.
     *
     * <p>If the Forge stack carries a real Mojmap stack (set by
     * {@link #toForgeStack(ItemStack)} when an item was placed through the GUI),
     * it is returned directly so identity and count round-trip faithfully. Otherwise
     * a neutral placeholder is built from the count.
     */
    public static ItemStack toFabricStack(mtn.minecraft_layer.net.minecraft.item.ItemStack forge) {
        if (forge == null || forge.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (forge.getFabricStack() != null) {
            return forge.getFabricStack();
        }
        // Placeholder item; identity mapping is a future round.
        // Mojmap 1.20.5+ ItemStack has no setTag (data-component refactor), so
        // NBT is not carried onto the vanilla stack here — best effort per R2.
        return new ItemStack(Items.AIR, forge.stackSize);
    }

    /**
     * Conversion of a vanilla Mojmap ItemStack to a Forge-side ItemStack.
     *
     * <p>Builds a layer stack that carries the real Mojmap stack through
     * {@code setFabricStack(...)} so the round-trip preserves item identity and
     * count. The layer {@code Item} itself remains a name-only dummy (used only
     * for {@code isEmpty()} and NBT persistence), while the authoritative item
     * lives in the carried Mojmap stack.
     */
    public static mtn.minecraft_layer.net.minecraft.item.ItemStack toForgeStack(net.minecraft.world.item.ItemStack fabric) {
        if (fabric == null || fabric.isEmpty()) {
            return null;
        }
        mtn.minecraft_layer.net.minecraft.item.ItemStack forge =
                new mtn.minecraft_layer.net.minecraft.item.ItemStack(
                        new mtn.minecraft_layer.net.minecraft.item.Item(), fabric.getCount());
        forge.setFabricStack(fabric);
        return forge;
    }

    /**
     * Conversion of a vanilla Mojmap Player to a Forge-side EntityPlayer.
     *
     * <p>The Forge-side player/entity wrapper is not implemented, and this method is only
     * ever used by {@code canPlayerUse()} which currently returns {@code true}. Returns
     * {@code null} by design.
     */
    public static mtn.minecraft_layer.net.minecraft.entity.player.EntityPlayer toForgePlayer(net.minecraft.world.entity.player.Player player) {
        return null;
    }
}
