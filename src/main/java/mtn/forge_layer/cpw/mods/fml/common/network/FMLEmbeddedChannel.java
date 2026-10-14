package mtn.forge_layer.cpw.mods.fml.common.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import mtn.forge_layer.cpw.mods.fml.common.network.internal.FMLProxyPacket;
import mtn.forge_layer.cpw.mods.fml.relauncher.Side;
import mtn.minecraft_layer.net.minecraft.network.Packet;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Real Forge 1.7.10 {@code cpw.mods.fml.common.network.FMLEmbeddedChannel}
 * wrapper around a genuine netty {@code EmbeddedChannel} pipeline.
 *
 * <p>Both side channels of a pair created by
 * {@link NetworkRegistry#newChannel(String, ChannelHandler...)} hold the same codec
 * handler, so the ported mod can {@code findChannelHandlerNameForType(...)} and
 * {@code pipeline().addAfter(...)} real netty handlers — exactly the API IronChest's
 * {@code PacketHandler} uses.
 *
 * <p>The Fabric side is bridged: outbound {@code generatePacketFrom} produces an
 * {@link FMLProxyPacket} whose bytes are carried by Fabric; the receiving side calls
 * {@link #acceptInbound(FMLProxyPacket)} to run the same pipeline in reverse. IMPORTANT:
 * the codec instances are shared between the two side channels (like Forge), so they
 * must stay {@code @Sharable}-compatible netty handlers.
 */
public class FMLEmbeddedChannel {
    /** Real netty embedded pipeline: codec + mod handlers live here. */
    private final EmbeddedChannel channel;
    private final String name;
    private final Side source;

    public FMLEmbeddedChannel(String channelName, Side source,
            ChannelHandler... handlers) {
        this.name = channelName;
        this.source = source;
        this.channel = new EmbeddedChannel();
        for (ChannelHandler handler : handlers) {
            channel.pipeline().addLast(handler);
        }
        channel.attr(NetworkRegistry.FML_CHANNEL_KEY).set(channelName);
    }

    public ChannelPipeline pipeline() {
        return channel.pipeline();
    }

    public String findChannelHandlerNameForType(
            Class<? extends ChannelHandler> type) {
        String targetName = null;
        for (Map.Entry<String, ChannelHandler> entry : channel.pipeline()) {
            if (type.isInstance(entry.getValue())) {
                targetName = entry.getKey();
                break;
            }
        }
        return targetName;
    }

    /**
     * Utility method: run the outbound (error) packet through this channel's
     * codec and return the {@link Packet} (FMLProxyPacket) produced, suitable for
     * passing to vanilla network code (e.g. {@code TileEntity.getDescriptionPacket}).
     */
    public Packet generatePacketFrom(Object msg) {
        channel.writeOutbound(msg);
        Object out = channel.readOutbound();
        return out instanceof Packet ? (Packet) out : null;
    }

    /** Route an inbound wire packet's bytes through the pipeline. */
    public void acceptInbound(FMLProxyPacket packet) {
        channel.writeInbound(packet);
        channel.inboundMessages().clear();
    }

    public String name() {
        return name;
    }

    public Side source() {
        return source;
    }

    /** The raw netty channel, for advanced users. */
    public EmbeddedChannel raw() {
        return channel;
    }
}
