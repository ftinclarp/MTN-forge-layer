package net.minecraftforge.oredict;

import mtn.minecraft_layer.net.minecraft.block.Block;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.item.ItemStack;
import mtn.minecraft_layer.net.minecraft.item.crafting.ShapedRecipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Forge 1.7.10 {@code net.minecraftforge.oredict.ShapedOreRecipe} shape
 * parser. It does NOT match recipes in-game (that requires the full crafting
 * manager); it parses and holds the shape + ingredients so the pipeline's
 * transform stage can emit a Fabric recipe JSON. Cells may be a String ore
 * name, an {@link ItemStack}, a {@link Block}, an {@link Item}, or a
 * {@link List ItemStack list} (from {@code OreDictionary.getOres}).
 */
public class ShapedOreRecipe extends ShapedRecipes {

    private final ItemStack result;
    private final String[] shapeGrid;
    private final Map<Character, Object> ingredients;

    public ShapedOreRecipe(ItemStack result, Object... recipe) {
        super(result);
        this.result = result;
        List<String> rows = new ArrayList<>();
        int idx = 0;
        // The first element may be a String[] (one entry per row) or a plain
        // String (a single row given as a string).
        Object first = recipe.length > 0 ? recipe[0] : null;
        if (first instanceof String[] rowArray) {
            for (String row : rowArray) {
                rows.add(row);
            }
            idx = 1;
        } else {
            while (idx < recipe.length && recipe[idx] instanceof String s) {
                rows.add(s);
                idx++;
            }
        }
        this.shapeGrid = rows.toArray(new String[0]);
        int height = shapeGrid.length;
        int width = height == 0 ? 0 : shapeGrid[0].length();
        this.ingredients = new LinkedHashMap<>();
        while (idx + 1 < recipe.length) {
            Object key = recipe[idx];
            Object value = recipe[idx + 1];
            if (key instanceof Character c) {
                ingredients.put(c, value);
            }
            idx += 2;
        }
    }

    public ItemStack getResult() {
        return result;
    }

    /** The 3×3 (or N×M) shape grid, one string per row. */
    public String[] getShapeGrid() {
        return shapeGrid.clone();
    }

    /**
     * The raw ingredient at a grid cell, or {@code null} when the cell is
     * empty or the ingredient is undefined. The returned value is whatever the
     * mod passed at construction: ore-name String, ItemStack, Block, Item or
     * List<ItemStack>.
     */
    public Object getIngredientAt(int row, int col) {
        if (row < 0 || row >= shapeGrid.length) {
            return null;
        }
        String r = shapeGrid[row];
        if (col < 0 || col >= r.length()) {
            return null;
        }
        char c = r.charAt(col);
        if (c == ' ') {
            return null;
        }
        return ingredients.get(c);
    }
}
