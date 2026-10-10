package mtn.forge_layer.net.minecraft.nbt;

import java.util.Map;
import java.util.Set;

/**
 * Dummy stand-in for {@code net.minecraft.nbt.NBTTagCompound}
 * (Forge 1.7.10). In-memory map-backed; enough to compile and exercise
 * ported mod code. No real NBT serialization.
 */
public class NBTTagCompound {
    private final Map<String, Object> data = new java.util.HashMap<>();

    public void setString(String key, String value) {
        data.put(key, value);
    }

    public String getString(String key) {
        Object v = data.get(key);
        return v instanceof String ? (String) v : "";
    }

    public void setInteger(String key, int value) {
        data.put(key, value);
    }

    public int getInteger(String key) {
        Object v = data.get(key);
        return v instanceof Integer ? (Integer) v : 0;
    }

    public void setBoolean(String key, boolean value) {
        data.put(key, value);
    }

    public boolean getBoolean(String key) {
        Object v = data.get(key);
        return v instanceof Boolean && (Boolean) v;
    }

    public void setFloat(String key, float value) {
        data.put(key, value);
    }

    public float getFloat(String key) {
        Object v = data.get(key);
        return v instanceof Float ? (Float) v : 0f;
    }

    public void setByte(String key, byte value) {
        data.put(key, value);
    }

    public byte getByte(String key) {
        Object v = data.get(key);
        return v instanceof Byte ? (Byte) v : 0;
    }

    public boolean hasKey(String key) {
        return data.containsKey(key);
    }

    public void removeTag(String key) {
        data.remove(key);
    }

    public Set<String> getKeySet() {
        return data.keySet();
    }

    public void setTag(String key, NBTTagCompound tag) {
        data.put(key, tag);
    }

    public NBTTagCompound getCompoundTag(String key) {
        Object v = data.get(key);
        return v instanceof NBTTagCompound ? (NBTTagCompound) v : new NBTTagCompound();
    }

    public void setTag(String key, NBTTagList list) {
        data.put(key, list);
    }

    public NBTTagList getTagList(String key, int type) {
        Object v = data.get(key);
        return v instanceof NBTTagList ? (NBTTagList) v : new NBTTagList();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
