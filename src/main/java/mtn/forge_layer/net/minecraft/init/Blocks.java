package mtn.forge_layer.net.minecraft.init;

import mtn.forge_layer.net.minecraft.block.Block;

/**
 * Dummy stand-in for {@code net.minecraft.init.Blocks} (Forge 1.7.10).
 * A handful of named block constants backed by the dummy {@link Block};
 * enough to compile typical {@code Blocks.X} references.
 */
public class Blocks {
    public static final Block air = new Block("air");
    public static final Block stone = new Block("stone");
    public static final Block dirt = new Block("dirt");
}
