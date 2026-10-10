package mtn.forge_layer.net.minecraft.block;

/**
 * Dummy stand-in for {@code net.minecraft.block.Block} (Forge 1.7.10).
 * No real registration — only exists so ported mod code compiles.
 */
public class Block {
    private final String name;

    public Block(String name) {
        this.name = name;
    }

    public String getUnlocalizedName() {
        return name;
    }
}
