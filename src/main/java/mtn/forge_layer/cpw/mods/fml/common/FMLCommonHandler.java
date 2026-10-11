package mtn.forge_layer.cpw.mods.fml.common;

/**
 * Minimal stub standing in for {@code cpw.mods.fml.common.FMLCommonHandler}.
 *
 * <p>R3b only wires the {@code CPW} side ("common" proxy) of the example mod,
 * so the side is reported as CLIENT. Phase 2/3 of the actual MTN proper may
 * rework this, but for R3b this fixed value is sufficient.
 */
public class FMLCommonHandler {

    public static FMLCommonHandler instance() {
        return INSTANCE;
    }

    private static final FMLCommonHandler INSTANCE = new FMLCommonHandler();

    public mtn.forge_layer.cpw.mods.fml.relauncher.Side getSide() {
        return mtn.forge_layer.cpw.mods.fml.relauncher.Side.CLIENT;
    }
}
