package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mtn.forge_layer.cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import mtn.forge_layer.cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper}.
 *
 * <p>Each wrapper owns one {@link FMLIndexedMessageToMessageCodec} (real netty
 * signature) that indexes message classes by discriminator; because the base class's
 * netty {@code encode}/{@code decode} already write/read the discriminator byte, the
 * anonymous codec here only bridges the remaining bytes to/from the {@link IMessage}.
 * Every send path encodes the message through the bridge ({@code bridge.encode}) before
 * handing the resulting {@code CustomPacketPayload} to Fabric's play networking, and
 * every receive path decodes through the same codec before dispatch to the
 * {@link IMessageHandler}.
 */
public class SimpleNetworkWrapper {

    private final String channel;
    private final List<FMLIndexedMessageToMessageCodec<?>> codecs =
            new ArrayList<>();
    private FMLIndexedMessageToMessageCodec<Object> codec;

    public SimpleNetworkWrapper(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    /**
     * Registers a message class with its handler and discriminator id.
     */
    public <REQ extends IMessage, REPLY extends IMessage>
    void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> handler,
            Class<REQ> requestType, int discriminator, Side side) {
        FMLIndexedMessageToMessageCodec<Object> codec = codec();
        codec.addDiscriminator(discriminator, requestType);
        mtn.forge_layer.registry.FabricNetworkBridge.registerCodec(codec, channel);
        mtn.forge_layer.registry.FabricNetworkBridge.registerHandler(requestType, handler);
    }

    public void sendToServer(IMessage msg) {
        mtn.forge_layer.registry.FabricNetworkBridge.sendToServer(
                mtn.forge_layer.registry.FabricNetworkBridge.encode(msg, channel));
    }

    public void sendToAll(IMessage msg) {
        mtn.forge_layer.registry.FabricNetworkBridge.sendToAll(
                mtn.forge_layer.registry.FabricNetworkBridge.encode(msg, channel));
    }

    public void sendTo(IMessage msg, Object player) {
        net.minecraft.network.protocol.common.custom.CustomPacketPayload payload =
                mtn.forge_layer.registry.FabricNetworkBridge.encode(msg, channel);
        mtn.forge_layer.registry.FabricNetworkBridge.sendTo(payload, player);
    }

    private FMLIndexedMessageToMessageCodec<Object> codec() {
        if (codec == null) {
            codec = new FMLIndexedMessageToMessageCodec<Object>() {
                @Override
                public void encodeInto(ChannelHandlerContext ctx, Object msg,
                        ByteBuf target) throws Exception {
                    ((IMessage) msg).toBytes(target);
                }

                @Override
                public void decodeInto(ChannelHandlerContext ctx,
                        ByteBuf source, Object msg) {
                    ((IMessage) msg).fromBytes(source);
                }
            };
            codecs.add(codec);
        }
        return codec;
    }
}
