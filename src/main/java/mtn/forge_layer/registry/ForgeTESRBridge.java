package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Fabric↔Forge bridge for {@link TileEntitySpecialRenderer} registration and
 * invocation.
 *
 * <p>When a ported mod calls
 * {@code ClientRegistry.bindTileEntitySpecialRenderer(teClass, tesr)} this
 * bridge registers a real Fabric {@code BlockEntityRenderer} against the
 * Fabric {@code BlockEntityType} that {@code GameRegistry} built, so the block
 * renders through the vanilla block-entity pipeline.
 *
 * <p>The registered {@link BridgedBlockEntityRenderer} installs the Fabric
 * render context (the {@code PoseStack} + {@code MultiBufferSource}) onto the
 * layer dispatcher and the base-class {@code matrices} field, then delegates to
 * the ported {@code tesr.renderTileEntityAt(...)}. The ported TESR's body was
 * rewritten by MTN-lwjgl-transformer so its GL11 calls are now
 * {@code matrices.pushPose()/translate()/mulPose()/scale()}, and the
 * {@code ModelRenderer} bridge emits cube quads through
 * {@code dispatcher.getCurrentVertexConsumer()}.
 */
public final class ForgeTESRBridge {

    private ForgeTESRBridge() {
    }

    /**
     * Register a Fabric BlockEntityRenderer for the tile class that delegates to
     * the ported TESR.
     */
    public static void register(Class<? extends TileEntity> teClass,
                                TileEntitySpecialRenderer<?> tesr) {
        if (teClass == null) {
            // e.g. IronChestType.WOOD uses the vanilla chest and has no tile
            // class; there is nothing to bind a renderer to. Not an error.
            System.out.println("[MTN-LAYER] ForgeTESRBridge: register skipped — "
                    + "no tile class to bind a renderer to.");
            return;
        }
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
     * The registered Fabric block-entity renderer. It wires the PoseStack +
     * MultiBufferSource into the layer dispatcher and the TESR base-class
     * {@code matrices} field, then delegates to the ported TESR.
     */
    private static class BridgedBlockEntityRenderer
            implements BlockEntityRenderer<net.minecraft.world.level.block.entity.BlockEntity> {

        private final Class<? extends TileEntity> teClass;
        // Raw type on purpose: the same TESR is used for many tile classes, and
        // we call the untyped renderTileEntityAt(Object, ...) entry point.
        @SuppressWarnings("rawtypes")
        private final TileEntitySpecialRenderer tesr;

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
            try {
                TileEntity forgeTe = ((net.minecraft.world.level.block.entity
                        .BlockEntity) entity) instanceof FabricBlockEntityAdapter fbea
                        ? fbea.getForgeTe() : null;
                if (forgeTe == null) {
                    return;
                }
                // Install the Fabric render context for the ModelRenderer bridge.
                // Fabric's per-block light is normally reliable, but some
                // pipelines deliver 0 here; compute the real world light at the
                // block position so the model is never pitch black.
                int realLight = light;
                try {
                    net.minecraft.client.Minecraft mc =
                            net.minecraft.client.Minecraft.getInstance();
                    if (mc.level != null) {
                        realLight = net.minecraft.client.renderer.LevelRenderer
                                .getLightColor(mc.level, entity.getBlockPos());
                    }
                } catch (Throwable ignored) {
                    // keep the passed-in light on any failure
                }
                mtn.minecraft_layer.net.minecraft.client.renderer.tileentity
                        .TileEntityRendererDispatcher dispatcher =
                        mtn.minecraft_layer.net.minecraft.client.renderer.tileentity
                                .TileEntityRendererDispatcher.instance;
                dispatcher.installRenderContext(matrices, consumers, realLight, overlay);
                dispatcher.field_147550_f =
                        mtn.forge_layer.registry.ForgeWorldBridge
                                .fromClientLevel(net.minecraft.client.Minecraft
                                        .getInstance().level);
                dispatcher.field_147560_j = entity.getBlockPos().getX();
                dispatcher.field_147561_k = entity.getBlockPos().getY();
                dispatcher.field_147558_l = entity.getBlockPos().getZ();

                // The transformed mod code references `matrices`; it inherits this
                // field from the TESR base class.
                tesr.matrices = matrices;
                tesr.renderTileEntityAt(forgeTe, 0.0D, 0.0D, 0.0D, tickDelta);
            } catch (Throwable t) {
                System.out.println("[MTN-LAYER] ForgeTESRBridge.render failed for "
                        + teClass.getName() + ": " + t);
            } finally {
                mtn.minecraft_layer.net.minecraft.client.renderer.tileentity
                        .TileEntityRendererDispatcher.instance.clearRenderContext();
            }
        }
    }
}
