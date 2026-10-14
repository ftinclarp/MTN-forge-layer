package net.minecraftforge.oredict;

import mtn.minecraft_layer.net.minecraft.block.Block;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Forge 1.7.10 {@code net.minecraftforge.oredict.OreDictionary} logic:
 * an in-memory map from ore name to a list of equivalent items. Registrations
 * are collected here so the pipeline's transform stage can serialize them into
 * Fabric item-tag JSON files. This is genuine logic, not a stub.
 */
public final class OreDictionary {

    private static final Map<String, List<ItemStack>> ORES = new LinkedHashMap<>();
    private static final Map<String, Integer> ID_BY_NAME = new LinkedHashMap<>();
    private static int nextId = 0;

    private OreDictionary() {
    }

    public static void registerOre(String name, ItemStack ore) {
        ORES.computeIfAbsent(name, k -> new ArrayList<>()).add(ore);
        ID_BY_NAME.putIfAbsent(name, nextId++);
    }

    public static void registerOre(String name, Item ore) {
        registerOre(name, new ItemStack(ore));
    }

    public static void registerOre(String name, Block ore) {
        registerOre(name, new ItemStack(ore, 1, 0));
    }

    /** Real Forge 1.7.10 generic entry point. */
    public static void registerOre(String name, Object ore) {
        if (ore instanceof ItemStack stack) {
            registerOre(name, stack);
        } else if (ore instanceof Item item) {
            registerOre(name, item);
        } else if (ore instanceof Block block) {
            registerOre(name, block);
        }
    }

    public static List<ItemStack> getOres(String name) {
        List<ItemStack> list = ORES.get(name);
        return list == null ? List.of() : List.copyOf(list);
    }

    public static int getOreID(String name) {
        Integer id = ID_BY_NAME.get(name);
        return id == null ? -1 : id;
    }

    /** Names whose registered stacks match (by item equality). */
    public static int[] getOreIDs(ItemStack stack) {
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<String, List<ItemStack>> e : ORES.entrySet()) {
            for (ItemStack o : e.getValue()) {
                if (o.isItemEqual(stack)) {
                    ids.add(ID_BY_NAME.get(e.getKey()));
                    break;
                }
            }
        }
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    public static String[] getOreNames() {
        return ORES.keySet().toArray(new String[0]);
    }
}
