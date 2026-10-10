package mtn.forge_layer.net.minecraft.item;

/**
 * Dummy stand-in for {@code net.minecraft.item.Item} (Forge 1.7.10).
 * No real registration — only exists so ported mod code compiles.
 */
public class Item {
    private final String name;

    public Item(String name) {
        this.name = name;
    }

    public String getUnlocalizedName() {
        return name;
    }
}
