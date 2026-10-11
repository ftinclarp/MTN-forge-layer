package mtn.forge_layer.cpw.mods.fml.common.network;

import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.NetworkRegistry}.
 *
 * <p>Keeps a list of every codec registered via
 * {@link #registerMessageCodec(FMLIndexedMessageToMessageCodec)}; the Fabric
 * networking bridge installs wire receivers from these codecs. Also holds the
 * per-mod {@link IGuiHandler} registry and mirrors the Forge factory
 * {@code NetworkRegistry.newSimpleChannel("name")}.
 */
public class NetworkRegistry {

    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    static {
        // R3b: register the layer's OpenGuiMessage channel during preInit (before install).
        mtn.forge_layer.registry.OpenGuiHandler.initChannel();
    }

    private final List<FMLIndexedMessageToMessageCodec<?>> codecs = new ArrayList<>();
    private final Map<Integer, IGuiHandler> guiHandlers = new HashMap<>();

    public void registerMessageCodec(FMLIndexedMessageToMessageCodec<?> codec) {
        if (codec != null && !codecs.contains(codec)) {
            codecs.add(codec);
        }
    }

    public List<FMLIndexedMessageToMessageCodec<?>> codecs() {
        return codecs;
    }

    /** Creates a new simple channel wrapper for the given channel name. */
    public SimpleNetworkWrapper newSimpleChannel(String channelName) {
        return new SimpleNetworkWrapper(channelName);
    }

    /** Registers the GUI handler owned by the given mod (keyed by identity). */
    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        guiHandlers.put(System.identityHashCode(mod), handler);
    }

    /** Returns the GUI handler registered for the mod, or null. */
    public IGuiHandler getGuiHandler(Object mod) {
        return guiHandlers.get(System.identityHashCode(mod));
    }

    /**
     * Opens a GUI for the given player. On the server this sends an
     * {@link OpenGuiMessage}; on the client it resolves and opens the screen.
     * Delegates to {@code mtn.forge_layer.registry.OpenGuiHandler}.
     */
    public void openGui(Object mod, int id, Object player, Object world,
            int x, int y, int z) {
        mtn.forge_layer.registry.OpenGuiHandler.openGui(mod, id, player, world, x, y, z);
    }
}

