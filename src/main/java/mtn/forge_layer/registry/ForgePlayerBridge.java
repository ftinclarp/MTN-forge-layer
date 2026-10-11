package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.entity.player.EntityPlayer;
import mtn.minecraft_layer.net.minecraft.inventory.InventoryPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Bridges a vanilla {@link Player} onto the Forge-style
 * {@link EntityPlayer} used by {@code onBlockActivated}. Carries the real
 * {@link net.minecraft.server.level.ServerPlayer} so the layer can open the
 * GUI and send the OpenGuiMessage over the network.
 */
public class ForgePlayerBridge extends EntityPlayer {

    private final Player player;

    public ForgePlayerBridge(Player player) {
        this.player = player;
        this.inventory = new InventoryPlayer();
        if (player.level() != null) {
            this.worldObj = new ForgeWorldBridge(player.level());
        }
    }

    public Player getPlayer() {
        return player;
    }

    public net.minecraft.server.level.ServerPlayer serverPlayer() {
        return player instanceof net.minecraft.server.level.ServerPlayer
                ? (net.minecraft.server.level.ServerPlayer) player
                : null;
    }
}
