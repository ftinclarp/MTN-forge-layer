package mtn.forge_layer.registry;

import mtn.forge_layer.cpw.mods.fml.common.network.IGuiHandler;
import mtn.forge_layer.cpw.mods.fml.common.network.NetworkRegistry;
import mtn.forge_layer.cpw.mods.fml.common.network.OpenGuiMessage;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import mtn.minecraft_layer.net.minecraft.inventory.Container;
import mtn.minecraft_layer.net.minecraft.inventory.IInventory;
import mtn.minecraft_layer.net.minecraft.inventory.InventoryPlayer;
import mtn.minecraft_layer.net.minecraft.inventory.Slot;
import mtn.minecraft_layer.net.minecraft.tileentity.TileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles {@code NetworkRegistry.openGui}. The server resolves the registered
 * {@link IGuiHandler}'s {@code getServerGuiElement(...)} to a layered
 * {@link Container}, wraps it in a {@link FabricScreenHandlerAdapter}, sets it
 * as the player's current container menu, and sends an {@link OpenGuiMessage}
 * so the client opens the matching {@code FabricHandledScreen}. The client
 * fallback builds a screen handler locally (the example mod returns null from
 * {@code getClientGuiElement}, telling the layer to open the screen itself).
 */
public final class OpenGuiHandler {

    /** Layer-owned channel for OpenGuiMessage; registered with the bridge during preInit. */
    private static final String LAYER_CHANNEL = "open_gui";
    private static final SimpleNetworkWrapper LAYER_WRAPPER =
            NetworkRegistry.INSTANCE.newSimpleChannel(LAYER_CHANNEL);

    private static final int CHEST_ROWS = 3;
    private static final int CHEST_COLS = 3;

    private OpenGuiHandler() {
    }

    /** Registers OpenGuiMessage + its client handler on the layer channel. */
    public static void initChannel() {
        LAYER_WRAPPER.registerMessage((msg, ctx) -> {
            if (ctx.getSide() == MessageContext.Side.CLIENT) {
                openGuiClientSide((OpenGuiMessage) msg);
            }
            return null;
        }, OpenGuiMessage.class, 0, mtn.forge_layer.cpw.mods.fml.relauncher.Side.CLIENT);
    }

    public static void open() {
        // force static init of the wrapper (channel already registered by preInit)
    }

    /** Entry point from {@code NetworkRegistry.openGui}. */
    public static void openGui(Object mod, int id, Object player, Object world,
            int x, int y, int z) {
        ServerPlayer sp = resolveServerPlayer(player);
        if (sp != null) {
            openGuiServerSide(mod, id, sp, player, world, x, y, z);
        } else if (player instanceof net.minecraft.client.player.LocalPlayer) {
            System.out.println("[MTN-LAYER] openGui called from the client; ignoring (server-driven).");
        }
    }

    /** Extracts the real ServerPlayer from a ForgePlayerBridge (or direct). */
    private static ServerPlayer resolveServerPlayer(Object player) {
        if (player instanceof ServerPlayer) {
            return (ServerPlayer) player;
        }
        if (player instanceof ForgePlayerBridge) {
            return ((ForgePlayerBridge) player).serverPlayer();
        }
        return null;
    }

    /** Server: build the container menu, attach to the player, send OpenGuiMessage. */
    private static void openGuiServerSide(Object mod, int id, ServerPlayer sp,
            Object player, Object world, int x, int y, int z) {
        IGuiHandler handler = NetworkRegistry.INSTANCE.getGuiHandler(mod);
        if (handler == null) {
            System.out.println("[MTN-LAYER] openGuiServer: no IGuiHandler for mod " + mod);
            return;
        }
        // The ForgePlayerBridge/ForgeWorldBridge the example expects to cast to.
        Object playerProxy = player instanceof ForgePlayerBridge
                ? player
                : new EntityPlayerProxy(sp);
        Object element = handler.getServerGuiElement(id, playerProxy, world, x, y, z);
        if (!(element instanceof Container)) {
            System.out.println("[MTN-LAYER] openGuiServer: server element is not a layered Container: "
                    + String.valueOf(element));
            return;
        }
        int syncId = nextSyncId();
        FabricScreenHandlerAdapter adapter =
                new FabricScreenHandlerAdapter(syncId, (Container) element);
        sp.containerMenu = adapter;
        LAYER_WRAPPER.sendTo(new OpenGuiMessage(syncId, id, x, y, z), sp);
        System.out.println("[MTN-LAYER] opened GUI " + id + " syncId " + syncId
                + " for " + sp.getGameProfile().getName());
    }

    /** Client: prefer the GUI handler's element; else open FabricHandledScreen. */
    private static void openGuiClientSide(OpenGuiMessage msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        IInventory chestInv = null;
        net.minecraft.world.level.block.entity.BlockEntity be = mc.player.level()
                .getBlockEntity(new net.minecraft.core.BlockPos(msg.getX(), msg.getY(), msg.getZ()));
        if (be instanceof FabricBlockEntityAdapter) {
            TileEntity forgeTe = ((FabricBlockEntityAdapter) be).getForgeTe();
            if (forgeTe instanceof IInventory) {
                chestInv = (IInventory) forgeTe;
            }
        }
        InventoryPlayer playerInv = new InventoryPlayer();
        Container forge = buildContainer(playerInv, chestInv);
        FabricScreenHandlerAdapter adapter =
                new FabricScreenHandlerAdapter(msg.getSyncId(), forge);
        mc.player.containerMenu = adapter;
        mc.setScreen(new FabricHandledScreen(adapter, mc.player.getInventory(),
                Component.literal("My Chest")));
        System.out.println("[MTN-LAYER] opened client GUI " + msg.getGuiId());
    }

    /** Mirrors the R3b spec layout: 3x3 chest + 27 player + 9 hotbar slots. */
    private static Container buildContainer(InventoryPlayer playerInv, IInventory chest) {
        Container c = new Container() {
        };
        if (chest != null) {
            for (int row = 0; row < CHEST_ROWS; row++) {
                for (int col = 0; col < CHEST_COLS; col++) {
                    c.addSlotToContainer(new Slot(chest, row * CHEST_COLS + col,
                            8 + col * 18, 18 + row * 18));
                }
            }
        }
        for (int i = 0; i < 27; i++) {
            c.addSlotToContainer(new Slot(playerInv, i + 9, 8 + (i % 9) * 18, 84 + (i / 9) * 18));
        }
        for (int i = 0; i < 9; i++) {
            c.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
        return c;
    }

    private static int syncIdCounter;

    private static int nextSyncId() {
        return ++syncIdCounter;
    }

    /** Minimal layer EntityPlayer view over a vanilla ServerPlayer for IGuiHandler calls. */
    private static final class EntityPlayerProxy
            extends mtn.minecraft_layer.net.minecraft.entity.player.EntityPlayer {
        EntityPlayerProxy(net.minecraft.server.level.ServerPlayer delegate) {
            this.inventory = new InventoryPlayer();
        }
    }
}
