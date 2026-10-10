package mtn.forge_layer.cpw.mods.fml.common.network;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.FMLProxyPacket}.
 *
 * <p>In Forge 1.7.10, {@code FMLProxyPacket} wraps a {@code ChannelBuffer} and a
 * channel id carried across the wire by the backend. Here it wraps raw bytes the
 * Fabric networking bridge (the {@code mtn.forge_layer.registry.FabricNetworkBridge}
 * phase-2 implementation) carries as a custom payload.
 */
public class FMLProxyPacket {

    private final byte[] data;

    public FMLProxyPacket(byte[] data) {
        this.data = data;
    }

    public byte[] toBytes() {
        return data;
    }
}
