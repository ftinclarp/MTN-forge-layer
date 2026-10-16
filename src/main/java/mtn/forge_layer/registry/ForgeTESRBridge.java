package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Fabric↔Forge bridge for {@link TileEntitySpecialRenderer} registration.
 *
 * <p>When a ported mod calls
 * {@code ClientRegistry.bindTileEntitySpecialRenderer(teClass, tesr)} this
 * bridge registers a real Fabric {@code BlockEntityRenderer} against the
 * Fabric {@code BlockEntityType} that {@code GameRegistry} built for the same
 * tile class, so the block actually renders through the vanilla block-entity
 * pipeline.
 *
 * <p><b>RENDERING IS STOPPED THIS BATCH (GL11-heavy):</b> the IronChest TESR
 * body drives a legacy fixed-function GL11 matrix stack
 * ({@code glPushMatrix/glTranslatef/glScalef/glRotatef}) plus immediate-mode
 * quads from {@code ModelRenderer}. Hand-translating gl11 → PoseStack here
 * would be a manual hack the project defers to the dedicated
 * MTN-lwjgl-transformer project. So {@link #register} still wires the Fabric
 * BER, but its render body reports the situation once and renders nothing.
 */
public final class ForgeTESRBridge {

    private static boolean warned = false;

    private ForgeTESRBridge() {
    }

    /**
     * Register a Fabric BlockEntityRenderer for the tile class. The renderer is
     * a structural placeholder: it exists so the ported TESR is reachable, but
     * the rendering is deferred (see class javadoc).
     */
    public static void register(Class<? extends TileEntity> teClass,
                                TileEntitySpecialRenderer<?> tesr) {
        try {
            net.minecraft.world.level.block.entity.BlockEntityType<?> type =
                    mtn.forge_layer.cpw.mods.fml.common.registry.GameRegistry
                            .blockEntityTypeFor(teClass);
            if (type == null) {
                System.out.println("[MTN-LAYER] ForgeTESRBridge: no Fabric "
                        + "BlockEntityType for " + teClass.getName()
                        + " — renderer registered only in the layer dispatcher.");
                mtn.minecraft_layer.net.minecraft.client.renderer.tileentity
                        .TileEntityRendererDispatcher.instance
                        .bindTileEntitySpecialRenderer((Class) teClass,
                                (TileEntitySpecialRenderer) tesr);
                return;
            }
            BlockEntityRendererRegistry.register(type,
                    new BlockEntityRendererProvider<net.minecraft.world.level
                            .block.entity.BlockEntity>() {
                        @Override
                        public BlockEntityRenderer create(
                                BlockEntityRendererProvider.Context ctx) {
                            return new BridgedBlockEntityRenderer(
                                    (Class) teClass, (TileEntitySpecialRenderer) tesr);
                        }
                    });
            System.out.println("[MTN-LAYER] ForgeTESRBridge registered Fabric BER for "
                    + teClass.getName());
        } catch (Throwable t) {
            System.out.println("[MTN-LAYER] ForgeTESRBridge.register failed for "
                    + teClass.getName() + ": " + t);
        }
    }

    /**
     * The registered Fabric block-entity renderer. Delegation to the Forge TESR
     * is STOPPED pending GL11 translation; it logs once and draws nothing.
     */
    private static class BridgedBlockEntityRenderer
            implements BlockEntityRenderer<net.minecraft.world.level.block.entity.BlockEntity> {

        private final Class<? extends TileEntity> teClass;
        private final TileEntitySpecialRenderer<?> tesr;

        BridgedBlockEntityRenderer(Class<? extends TileEntity> teClass,
                                   TileEntitySpecialRenderer<?> tesr) {
            this.teClass = teClass;
            this.tesr = tesr;
        }

        @Override
        public void render(net.minecraft.world.level.block.entity.BlockEntity entity,
                           float tickDelta,
                           com.mojang.blaze3d.vertex.PoseStack matrices,
                           net.minecraft.client.renderer.MultiBufferSource consumers,
                           int light, int overlay) {
            if (!warned) {
                warned = true;
                System.out.println("[MTN-LAYER] ForgeTESRBridge: rendering of " + teClass.getName()
                        + " is STOPPED — the ported TESR is GL11-heavy "
                        + "(glPushMatrix/glTranslatef/glRotatef). GL11→PoseStack "
                        + "translation is deferred to MTN-lwjgl-transformer "
                        + "(TODO(mtn-layer)).");
            }
            // TODO(mtn-layer): drive tesr.renderTileEntityAt through a
            //  PoseStack/VertexConsumer adapter once the GL11 translation lands.
        }
    }
}
