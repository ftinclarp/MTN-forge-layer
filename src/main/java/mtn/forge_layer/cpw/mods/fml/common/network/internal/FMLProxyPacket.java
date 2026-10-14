package mtn.forge_layer.cpw.mods.fml.common.network.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mtn.minecraft_layer.net.minecraft.network.Packet;

/**
 * Real-Forge-shaped {@code cpw.mods.fml.common.network.internal.FMLProxyPacket}
 * extends the mine network {@code Packet} and wraps an open {@link ByteBuf} payload
 * plus the FML channel name it belongs to.
 *
 * <p>In Forge 1.7.10 {@code FMLProxyPacket} is produced by the outbound embedded
 * channel as the wire form of a mod message. At runtime our {@code FabricNetworkBridge}
 * carries this packet's bytes (via {@link #toBytes()}) as a Fabric custom payload, and
 * the receiving side feeds the same bytes back into its inb embedded channel so the
 * ported mod message handler runs through a real netty pipeline.
 */
public class FMLProxyPacket extends Packet {
    private final String channel;
    private final ByteBuf payload;

    public FMLProxyPacket(ByteBuf payload, String channel) {
        this.channel = channel;
        this.payload = payload == null ? Unpooled.buffer() : payload;
    }

    public String channel() {
        return channel;
    }

    public ByteBuf payload() {
        return payload;
    }

    public byte[] toBytes() {
        byte[] data = new byte[payload.readableBytes()];
        payload.getBytes(payload.readerIndex(), data);
        return data;
    }

    public ByteBuf copy() {
        return payload.copy();
    }
}
