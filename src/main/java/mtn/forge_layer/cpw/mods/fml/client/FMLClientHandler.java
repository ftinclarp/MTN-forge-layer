package mtn.forge_layer.cpw.mods.fml.client;

import mtn.minecraft_layer.net.minecraft.client.Minecraft;

/**
 * Stand-in for {@code cpw.mods.fml.client.FMLClientHandler} — the client-side
 * handler. {@link #getClient()} returns the layer's {@link Minecraft} facade
 * whose {@code theWorld} field carries the Fabric client world, so the ported
 * {@code ClientProxy.getClientWorld()} works.
 */
public class FMLClientHandler {

    private static final FMLClientHandler INSTANCE = new FMLClientHandler();

    public static FMLClientHandler instance() {
        return INSTANCE;
    }

    public Minecraft getClient() {
        return Minecraft.getMinecraft();
    }
}
