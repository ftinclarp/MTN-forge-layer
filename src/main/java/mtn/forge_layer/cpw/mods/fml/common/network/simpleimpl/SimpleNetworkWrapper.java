package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

import mtn.forge_layer.cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import mtn.forge_layer.cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper}.
 *
 * <p>Defers all wire work to {@code mtn.forge_layer.registry.FabricNetworkBridge}:
 * each wrapper owns one {@link FMLIndexedMessageToMessageCodec} that indexes message
 * classes by discriminator, and every send path encodes the message through the
 * bridge ({@code bridge.encode}) before handing the resulting
 * {@code CustomPacketPayload} to Fabric's client/server play networking. Handlers
 * and codecs are recorded on the bridge so it can route inbound messages.
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
     *
     * <p>Lazily builds the per-wrapper codec (a {@code FMLIndexedMessageToMessageCodec}
     * whose {@code encodeInto}/{@code decodeInto} hook into the message's own
     * {@code toBytes}/{@code fromBytes}), indexes {@code requestType} under the given
     * discriminator, and registers both the codec and the handler with
     * {@code FabricNetworkBridge}. The {@code side} argument uses {@link Side}
     * (cpw.mods.fml.relauncher.Side).
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
        // Note: the bridge's encode may return the narrower RawPayload type; the
        // local is typed as CustomPacketPayload to be safe.
        net.minecraft.network.protocol.common.custom.CustomPacketPayload payload =
                mtn.forge_layer.registry.FabricNetworkBridge.encode(msg, channel);
        mtn.forge_layer.registry.FabricNetworkBridge.sendTo(payload, player);
    }

    private FMLIndexedMessageToMessageCodec<Object> codec() {
        if (codec == null) {
            codec = new FMLIndexedMessageToMessageCodec<Object>() {
                {
                    // Instance initializer: nothing to do; maps start empty.
                }

                @Override
                protected void encodeInto(MessageContext ctx, IMessage msg,
                        net.minecraft.network.FriendlyByteBuf buf) {
                    java.lang.Byte id = classToId(msg);
                    buf.writeByte(id == null ? 0 : id.byteValue());
                    msg.toBytes(buf);
                }

                @Override
                protected void decodeInto(MessageContext ctx,
                        net.minecraft.network.FriendlyByteBuf buf, IMessage msg) {
                    msg.fromBytes(buf);
                }

                private java.lang.Byte classToId(IMessage m) {
                    return classToId().get(m.getClass());
                }
            };
            codecs.add(codec);
        }
        return codec;
    }
}
