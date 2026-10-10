package mtn.forge_layer.cpw.mods.fml.common.network;

import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.NetworkRegistry}.
 *
 * <p>Keeps a list of every codec registered via
 * {@link #registerMessageCodec(FMLIndexedMessageToMessageCodec)}; the Fabric
 * networking bridge (phase 2) installs wire receivers from these codecs.
 * Also mirrors the Forge factory
 * {@code NetworkRegistry.newSimpleChannel("name")}.
 */
public class NetworkRegistry {

    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    private final List<FMLIndexedMessageToMessageCodec<?>> codecs = new ArrayList<>();

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
}
