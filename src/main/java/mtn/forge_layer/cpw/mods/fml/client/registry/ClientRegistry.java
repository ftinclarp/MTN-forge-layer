package mtn.forge_layer.cpw.mods.fml.client.registry;

import mtn.minecraft_layer.net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import mtn.minecraft_layer.net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import mtn.forge_layer.registry.ForgeTESRBridge;

/**
 * Stand-in for {@code cpw.mods.fml.client.registry.ClientRegistry} — client-side
 * registrations. {@link #bindTileEntitySpecialRenderer} routes into the
 * {@link ForgeTESRBridge} which registers a real Fabric BlockEntityRenderer.
 */
public class ClientRegistry {

    private ClientRegistry() {
    }

    /**
     * Forge 1.7.10: register a special renderer for a tile entity class.
     * Routed into {@link ForgeTESRBridge#register} so a Fabric
     * BlockEntityRenderer gets registered for the Fabric BlockEntityType.
     */
    public static void bindTileEntitySpecialRenderer(
            Class<? extends TileEntity> teClass,
            TileEntitySpecialRenderer<?> renderer) {
        TileEntityRendererDispatcher.instance
                .bindTileEntitySpecialRenderer((Class) teClass,
                        (TileEntitySpecialRenderer) renderer);
        ForgeTESRBridge.register(teClass, renderer);
    }
}
