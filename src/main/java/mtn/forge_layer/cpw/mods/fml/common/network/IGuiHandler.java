package mtn.forge_layer.cpw.mods.fml.common.network;

import mtn.minecraft_layer.net.minecraft.entity.player.EntityPlayer;
import mtn.minecraft_layer.net.minecraft.world.World;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.IGuiHandler}.
 *
 * <p>Matches the real Forge 1.7.10 signatures: both methods take the concrete
 * {@link EntityPlayer} and {@link World} (not {@code Object}s). The layer
 * resolves these via {@link NetworkRegistry} when opening a GUI over the
 * network (see {@code NetworkRegistry.openGui}).
 */
public interface IGuiHandler {

    /** Returns the server-side container element (a Container). */
    Object getServerGuiElement(int id, EntityPlayer player, World world,
            int x, int y, int z);

    /** Returns the client-side screen element (a Gui). */
    Object getClientGuiElement(int id, EntityPlayer player, World world,
            int x, int y, int z);
}
