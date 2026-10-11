package mtn.forge_layer.cpw.mods.fml.common.network;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.IGuiHandler}.
 *
 * <p>Provides the server-side and client-side elements for a GUI. The layer
 * resolves these via {@link NetworkRegistry} when opening a GUI over the
 * network (see {@code openGui}).
 */
public interface IGuiHandler {

    /** Returns the server-side container element (a Container). */
    Object getServerGuiElement(int id, Object player, Object world,
            int x, int y, int z);

    /** Returns the client-side screen element (a Screen). */
    Object getClientGuiElement(int id, Object player, Object world,
            int x, int y, int z);
}
