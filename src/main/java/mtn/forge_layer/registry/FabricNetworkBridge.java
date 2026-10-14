package mtn.forge_layer.registry;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import mtn.forge_layer.cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import mtn.forge_layer.cpw.mods.fml.common.network.FMLEmbeddedChannel;
import mtn.forge_layer.cpw.mods.fml.common.network.NetworkRegistry;
import mtn.forge_layer.cpw.mods.fml.common.network.internal.FMLProxyPacket;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessage;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mtn.forge_layer.cpw.mods.fml.relauncher.Side;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges the Forge-1.7.10 simple + embedded network APIs onto Fabric's Networking API v1.
 *
 * <p>Both network styles share one envelope payload ({@link RawPayload}) carrying the
 * logical channel name and the encoded bytes. On receive the bytes are fed into the
 * matching side's {@link FMLEmbeddedChannel} embedded netty pipeline (where the codec
 * decodes them) — if the channel was registered through
 * {@link NetworkRegistry#newChannel(String, io.netty.channel.ChannelHandler...)},
 * mod-added netty handlers such as IronChest's {@code SimpleChannelInboundHandler}
 * run; otherwise the codec is decoded directly and the registered
 * {@link IMessageHandler} is dispatched to.
 *
 * <p>The {@code SimpleNetworkWrapper} path (com.example ironchest's GUI open message)
 * registers its codec through {@link #registerCodec} and sends via {@link #encode}.
 */
public final class FabricNetworkBridge {

    /** FML channel prefix (channel names are rewritten from {@code <modid>:<name>}). */
    private static final String CHANNEL_PREFIX = "mtnexample";

    /** Wire envelope id; the logical FML channel is carried inside the payload. */
    public static final ResourceLocation ENVELOPE_ID =
            ResourceLocation.fromNamespaceAndPath(CHANNEL_PREFIX, "mtn_net");

    /** codec -> logical FML channel. */
    private static final Map<FMLIndexedMessageToMessageCodec<?>, String> CHANNELS = new HashMap<>();
    /** message class -> handler. */
    private static final Map<Class<? extends IMessage>, IMessageHandler<?, ?>> HANDLERS =
            new HashMap<>();

    private FabricNetworkBridge() {
    }

    /** Registers a codec->channel mapping; also records the codec in NetworkRegistry. */
    public static void registerCodec(FMLIndexedMessageToMessageCodec<?> codec, String channel) {
        CHANNELS.put(codec, channel);
        NetworkRegistry.INSTANCE.registerMessageCodec(codec);
    }

    /** Records a message-class -> handler mapping for inbound dispatch. */
    public static void registerHandler(Class<? extends IMessage> type,
            IMessageHandler<?, ?> handler) {
        HANDLERS.put(type, handler);
    }

    /**
     * Called from {@code FabricEntry.onInitialize()} after the ported mod's lifecycle.
     * Installs the single wire envelope + receivers for every registered codec.
     */
    public static void install(FabricLoader loader) {
        int n = NetworkRegistry.INSTANCE.codecs().size();
        if (n == 0) {
            System.out.println("[MTN-LAYER] network bridge installed (0 codecs)");
            return;
        }
        for (FMLIndexedMessageToMessageCodec<?> codec : NetworkRegistry.INSTANCE.codecs()) {
            String channelName = CHANNELS.getOrDefault(codec, CHANNEL_PREFIX);
            registerReceiver(channelName);
        }
        System.out.println("[MTN-LAYER] network bridge installed (" + n + " codecs)");
    }

    private static boolean receiversInstalled = false;

    private static synchronized void registerReceiver(String channelName) {
        if (receiversInstalled) {
            return;
        }
        receiversInstalled = true;
        PayloadTypeRegistry.playC2S().register(RawPayload.TYPE, RawPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RawPayload.TYPE, RawPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RawPayload.TYPE,
                (payload, context) -> handle(payload, MessageContext.Side.SERVER));
        ClientPlayNetworking.registerGlobalReceiver(RawPayload.TYPE,
                (payload, context) -> handle(payload, MessageContext.Side.CLIENT));
    }

    /** Decode the payload bytes with the matching codec and dispatch to its handler. */
    private static void handle(RawPayload payload, MessageContext.Side side) {
        String channel = payload.channel().getPath();
        Side fmlSide = side == MessageContext.Side.CLIENT ? Side.CLIENT : Side.SERVER;
        FMLEmbeddedChannel embedded = NetworkRegistry.INSTANCE.getChannel(channel, fmlSide);
        if (embedded != null) {
            // Channel pair path (old-style netty): feed bytes into the pipeline so the
            // codec + mod-added handlers (e.g. IronChestMessageHandler) process them.
            embedded.acceptInbound(
                    new FMLProxyPacket(Unpooled.wrappedBuffer(payload.data()), channel));
            return;
        }
        for (Map.Entry<FMLIndexedMessageToMessageCodec<?>, String> e : CHANNELS.entrySet()) {
            if (!e.getValue().equals(channel)) {
                continue;
            }
            FMLIndexedMessageToMessageCodec<?> codec = e.getKey();
            try {
                IMessage msg = decode(codec, channel, payload.data());
                IMessageHandler<?, ?> handler = HANDLERS.get(msg.getClass());
                if (handler != null) {
                    dispatch(handler, msg, new MessageContext(side));
                } else {
                    System.out.println("[MTN-LAYER] no handler for "
                            + msg.getClass().getSimpleName());
                }
            } catch (RuntimeException ex) {
                System.out.println("[MTN-LAYER] packet decode/dispatch failed for channel "
                        + channel + ": " + ex.getMessage());
            }
            return;
        }
        System.out.println("[MTN-LAYER] received packet on unknown channel: " + channel);
    }

    /** Decode bytes into a message by running them through a one-shot embedded pipeline. */
    private static IMessage decode(FMLIndexedMessageToMessageCodec<?> codec, String channel,
            byte[] data) {
        EmbeddedChannel ch = new EmbeddedChannel(codec);
        ch.writeInbound(new FMLProxyPacket(Unpooled.wrappedBuffer(data), channel));
        Object out = ch.readInbound();
        ch.finish();
        if (!(out instanceof IMessage msg)) {
            throw new IllegalStateException("codec produced " +
                    String.valueOf(out));
        }
        return msg;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void dispatch(IMessageHandler handler, IMessage msg, MessageContext ctx) {
        IMessage reply = (IMessage) handler.onMessage(msg, ctx);
        if (reply != null) {
            System.out.println("[MTN-LAYER] reply generated for "
                    + msg.getClass().getSimpleName() + " (reply re-send not wired)");
        }
    }

    /** Encode a message through its registered codec into a wire payload. */
    public static RawPayload encode(IMessage msg, String channel) {
        for (Map.Entry<FMLIndexedMessageToMessageCodec<?>, String> e : CHANNELS.entrySet()) {
            FMLIndexedMessageToMessageCodec<?> codec = e.getKey();
            if (!e.getValue().equals(channel)
                    || !codec.classToId().containsKey(msg.getClass())) {
                continue;
            }
            EmbeddedChannel ch = new EmbeddedChannel(codec);
            try {
                ch.writeOutbound(msg);
                Object out = ch.readOutbound();
                FMLProxyPacket proxy = out instanceof FMLProxyPacket p
                        ? p
                        : new FMLProxyPacket(
                                new FriendlyByteBuf(Unpooled.wrappedBuffer(
                                        out instanceof byte[] b ? b : new byte[0])), channel);
                byte[] data = proxy.toBytes();
                return new RawPayload(
                        ResourceLocation.fromNamespaceAndPath(CHANNEL_PREFIX, channel), data);
            } catch (RuntimeException ex) {
                System.out.println("[MTN-LAYER] encode failed for "
                        + msg.getClass().getSimpleName() + ": " + ex.getMessage());
            } finally {
                ch.finish();
            }
        }
        throw new IllegalStateException("Message " + msg.getClass().getName()
                + " has no registered codec for channel " + channel);
    }

    /** Send a client->server payload. */
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    /** Best-effort broadcast; R3a example never calls this. */
    public static void sendToAll(CustomPacketPayload payload) {
        System.out.println("[MTN-LAYER] sendToAll not yet implemented (R3b)");
    }

    /** Send a server->player payload. */
    public static void sendTo(CustomPacketPayload payload, Object player) {
        if (player instanceof ServerPlayer sp) {
            ServerPlayNetworking.send(sp, payload);
        } else {
            System.out.println("[MTN-LAYER] sendTo: player is not a ServerPlayer: "
                    + String.valueOf(player));
        }
    }

    /**
     * Wire payload: the logical FML channel + the encoded packet bytes. Registered under
     * one envelope id; each receiver filters on the carried channel name.
     */
    public record RawPayload(ResourceLocation channel, byte[] data)
            implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<RawPayload> TYPE =
                new CustomPacketPayload.Type<>(ENVELOPE_ID);

        public static final StreamCodec<FriendlyByteBuf, RawPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeResourceLocation(payload.channel());
                            buf.writeByteArray(payload.data());
                        },
                        buf -> new RawPayload(buf.readResourceLocation(), buf.readByteArray()));

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
