package mtn.forge_layer.net.minecraft.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy stand-in for {@code net.minecraft.nbt.NBTTagList} (Forge 1.7.10).
 * In-memory list-backed; enough to compile and exercise ported mod code.
 */
public class NBTTagList {
    private final List<Object> list = new ArrayList<>();

    public void appendTag(NBTTagCompound tag) {
        list.add(tag);
    }

    public int tagCount() {
        return list.size();
    }

    public NBTTagCompound getCompoundTagAt(int i) {
        Object v = list.get(i);
        return v instanceof NBTTagCompound ? (NBTTagCompound) v : new NBTTagCompound();
    }
}
