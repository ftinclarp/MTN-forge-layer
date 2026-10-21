package mtn.forge_layer.cpw.mods.fml.common.registry;

import mtn.minecraft_layer.net.minecraft.block.Block;
import mtn.minecraft_layer.net.minecraft.item.Item;
import mtn.minecraft_layer.net.minecraft.item.ItemStack;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;
import mtn.forge_layer.registry.ForgeBlockAdapter;
import mtn.forge_layer.registry.ForgeItemAdapter;

import net.minecraftforge.oredict.ShapedOreRecipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

/**
 * Stand-in for {@code cpw.mods.fml.common.registry.GameRegistry}
 * (Forge 1.7.10). Unlike the earlier no-op, these methods perform real
 * registration: the dummy Forge block/item is wrapped in an adapter and
 * registered under {@code <forge-modid>:<name>}.
 */
public final class GameRegistry {

    /** Mod id of the ported {@code @Mod} currently being dispatched, set by FabricEntry. */
    public static String CURRENT_MOD_ID = "mtnexample";

    private static final java.util.Map<Class<?>, BlockEntityType<?>> BY_CLASS = new java.util.HashMap<>();
    private static final java.util.Map<String, BlockEntityType<?>> BY_ID = new java.util.HashMap<>();
    private static final java.util.List<ForgeBlockAdapter> REGISTERED_BLOCKS =
            new java.util.ArrayList<>();
    /** originalName -> normalizedName, for lookups that still use the Forge name. */
    private static final java.util.Map<String, String> NORMALIZED_NAMES = new java.util.HashMap<>();

    private GameRegistry() {
    }

    /**
     * Normalizes a Forge 1.7.10 registry name (any case) to a Fabric-valid
     * id path segment: lowercase, and any char outside [a-z0-9._-] replaced
     * with '_', then consecutive underscores collapsed. Throws on empty
     * result. Internal — does not change the public API.
     */
    private static String normalizeName(String original) {
        String lower = original.toLowerCase(java.util.Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-') {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_' && c == '_') {
                    continue; // collapse consecutive underscores
                }
                sb.append(c);
            } else {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_') {
                    continue; // collapse when the last char was already an underscore
                }
                sb.append('_');
            }
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_') {
            sb.setLength(sb.length() - 1);
        }
        String normalized = sb.toString();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("registry name normalized to empty: '" + original + "'");
        }
        NORMALIZED_NAMES.put(original, normalized);
        System.out.println("[MTN-LAYER] registry name normalized: " + original + " -> " + normalized);
        return normalized;
    }

    /** Reverse lookup: the normalized id for a Forge name, or null if not seen. */
    public static String normalizedNameFor(String original) {
        return NORMALIZED_NAMES.get(original);
    }

    /** Public accessor for a pre-registered block entity type by tile class. */
    public static BlockEntityType<?> blockEntityTypeFor(
            Class<? extends TileEntity> teClass) {
        return BY_CLASS.get(teClass);
    }

    /** Public accessor for a pre-registered block entity type by id string. */
    public static BlockEntityType<?> blockEntityTypeFor(String id) {
        return BY_ID.get(id);
    }

    /** Real Forge 1.7.10 signature: returns the registered Block. */
    public static Block registerBlock(Block forgeBlock, String name) {
        return registerBlock(forgeBlock, mtn.minecraft_layer.net.minecraft.item.ItemBlock.class, name);
    }

    /** Real Forge 1.7.10 signature: {@code registerBlock(Block, Class<? extends ItemBlock>, String)}. */
    public static Block registerBlock(Block forgeBlock,
            Class<? extends mtn.minecraft_layer.net.minecraft.item.ItemBlock> itemclass,
            String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, normalizeName(name));
        ForgeBlockAdapter adapter = new ForgeBlockAdapter(forgeBlock, id);
        Registry.register(BuiltInRegistries.BLOCK, id, adapter);
        REGISTERED_BLOCKS.add(adapter);

        // BlockItem so it can exist in inventory
        net.minecraft.world.item.Item blockItem = new BlockItem(adapter, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        System.out.println("[MTN-LAYER] registered block: " + id);
        return forgeBlock;
    }

    /** Real Forge 1.7.10 signature: returns Item. */
    public static Item registerItem(Item forgeItem, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, normalizeName(name));
        net.minecraft.world.item.Item fabricItem = new ForgeItemAdapter(forgeItem, new net.minecraft.world.item.Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, id, fabricItem);
        System.out.println("[MTN-LAYER] registered item: " + id);
        return forgeItem;
    }

    public static void registerTileEntity(
            Class<? extends TileEntity> teClass,
            String id) {
        if (teClass == null) {
            // e.g. IronChestType.WOOD uses the vanilla chest and has no TE
            // class. Forge 1.7.10 tolerated this; skip registration.
            System.out.println("[MTN-LAYER] registerTileEntity skipped: no tile class for id '" + id + "'");
            return;
        }
        try {
            // Find the adapter(s) whose Forge block produces this TE class, so the
            // BlockEntityType's validBlocks include the owning block. Without it,
            // vanilla BlockEntity.validateSupports rejects placement.
            // NOTE: Iron Chest creates its TE in createTileEntity(world, meta),
            // while createNewTileEntity is overridden to return null. Metadata 0
            // maps to IronChestType.WOOD which has no clazz, so probe every meta
            // value and skip ones that throw or return null.
            java.util.List<ForgeBlockAdapter> owning = new java.util.ArrayList<>();
            for (ForgeBlockAdapter adapter : REGISTERED_BLOCKS) {
                mtn.minecraft_layer.net.minecraft.block.Block fb = adapter.getForgeBlock();
                if (!(fb instanceof mtn.minecraft_layer.net.minecraft.block.BlockContainer bc)) {
                    continue;
                }
                mtn.minecraft_layer.net.minecraft.tileentity.TileEntity probe =
                        probeTileEntity(bc, teClass);
                if (probe != null) {
                    owning.add(adapter);
                }
            }
            net.minecraft.world.level.block.Block[] validBlocks =
                    new net.minecraft.world.level.block.Block[owning.size()];
            for (int i = 0; i < owning.size(); i++) {
                validBlocks[i] = owning.get(i);
            }

            // Instantiate a fresh Forge TE so each placed block gets its own instance.
            // FabricBlockEntityTypeBuilder.create's factory must return a NEW BlockEntity per call.
            final net.minecraft.world.level.block.entity.BlockEntityType<?>[] holder =
                    new net.minecraft.world.level.block.entity.BlockEntityType<?>[1];
            BlockEntityType<?> type = FabricBlockEntityTypeBuilder
                    .<mtn.forge_layer.registry.FabricBlockEntityAdapter>create(
                            (pos, state) -> {
                                try {
                                    TileEntity fresh =
                                            (TileEntity) teClass.getDeclaredConstructor().newInstance();
                                    return new mtn.forge_layer.registry.FabricBlockEntityAdapter(
                                            holder[0], pos, state, fresh);
                                } catch (ReflectiveOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            validBlocks)
                    .build();

            holder[0] = type;

            ResourceLocation resId = ResourceLocation.fromNamespaceAndPath(CURRENT_MOD_ID, normalizeName(id));
            registerIntoRegistry(resId, type);

            BY_CLASS.put(teClass, type);
            BY_ID.put(id, type);

            // Give the owning adapters their type so placement resolves instantly.
            for (ForgeBlockAdapter adapter : owning) {
                adapter.setBlockEntityType(type);
            }

            System.out.println("[MTN-LAYER] registerTileEntity: " + id
                    + " (" + teClass.getSimpleName() + ") blocks=" + owning.size());
        } catch (RuntimeException e) {
            System.out.println("[MTN-LAYER] registerTileEntity FAILED for " + id
                    + " (" + teClass.getSimpleName() + "): " + e.getMessage());
        }
    }

    /**
     * Probes a Forge block for a tile entity of the given class. Tries
     * {@code createTileEntity} (the method Iron Chest actually implements)
     * across every metadata value, since metadata 0 (WOOD) has no clazz and
     * throws. Returns the first non-null probe, or null.
     */
    private static mtn.minecraft_layer.net.minecraft.tileentity.TileEntity probeTileEntity(
            mtn.minecraft_layer.net.minecraft.block.BlockContainer bc,
            Class<? extends TileEntity> teClass) {
        mtn.minecraft_layer.net.minecraft.world.World w =
                new mtn.minecraft_layer.net.minecraft.world.World();
        for (int meta = 0; meta < 16; meta++) {
            try {
                mtn.minecraft_layer.net.minecraft.tileentity.TileEntity probe =
                        bc.createTileEntity(w, meta);
                if (probe != null && probe.getClass().equals(teClass)) {
                    return probe;
                }
            } catch (RuntimeException ignored) {
                // e.g. metadata 0 = WOOD with null clazz; try the next metadata.
            }
        }
        return null;
    }

    /** Registers a block entity type under the current mod id. */
    private static void registerIntoRegistry(
            ResourceLocation r, BlockEntityType<?> t) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, r, t);
    }

    public static void registerTileEntity(
            Class<? extends TileEntity> teClass,
            String id, String legacyId) {
        registerTileEntity(teClass, id);
    }

    /** Forge 1.7.10: register a TE with both a namespaced and legacy id. */
    public static void registerTileEntityWithAlternatives(
            Class<? extends TileEntity> teClass,
            String id, String legacyId) {
        registerTileEntity(teClass, id);
        // TODO(mtn-layer): also index the legacy id so old saves keep working.
    }

    public static void registerCustomItemStack(String name, ItemStack stack) {
        // TODO(mtn-layer): add the stack to the custom-item common list.
    }

    /** Forge 1.7.10: register a shaped recipe (oredict-aware). */
    public static void addRecipe(ShapedOreRecipe recipe) {
        System.out.println("[MTN-LAYER] GameRegistry.addRecipe: " + recipe.getResult());
        // The transform stage separately serializes these into data/<modid>/recipes.
        // TODO(mtn-layer): also feed ShapedOreRecipe into the Fabric recipes/bridge.
    }
}
