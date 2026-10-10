package mtn.forge_layer.registry;

import mtn.forge_layer.cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import mtn.forge_layer.cpw.mods.fml.common.network.NetworkRegistry;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessage;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.MessageContext;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges the Forge-1.7.10 simple network API onto Fabric's Networking API v1.
 *
 * <p>Each {@link FMLIndexedMessageToMessageCodec} registered via
 * {@link NetworkRegistry#codecs()} becomes a custom payload + global receiver on its
 * channel. The payload ({@link RawPayload}) carries the logical channel name and the
 * already-encoded packet bytes; the receiver decodes them with the matching codec and
 * dispatches to the {@link IMessageHandler} registered for the message class.
 *
 * <p>R3a goal is install + registration: the example mod only REGISTERS a message and
 * never sends, so the receivers exist but are silent until an actual send (R3b).
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
     * Called from {@code FabricEntry.onInitialize()} after the ported mod's lifecycle
     * (preInit registers messages -> codecs). Installs a receiver per registered codec.
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

    /** Installs client + server play receivers for one FML channel. */
    private static void registerReceiver(String channelName) {
        // Play + server: a payload type must be registered before a receiver can use it.
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
        for (Map.Entry<FMLIndexedMessageToMessageCodec<?>, String> e : CHANNELS.entrySet()) {
            if (!e.getValue().equals(channel)) {
                continue;
            }
            FMLIndexedMessageToMessageCodec<?> codec = e.getKey();
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
            try {
                IMessage msg = codec.decode(new MessageContext(side), buf);
                IMessageHandler<?, ?> handler = HANDLERS.get(msg.getClass());
                if (handler != null) {
                    dispatch(handler, msg, new MessageContext(side));
                } else {
                    System.out.println("[MTN-LAYER] no handler for " + msg.getClass().getSimpleName());
                }
            } catch (RuntimeException ex) {
                System.out.println("[MTN-LAYER] packet decode/dispatch failed for channel "
                        + channel + ": " + ex.getMessage());
            }
            return;
        }
        System.out.println("[MTN-LAYER] received packet on unknown channel: " + channel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void dispatch(IMessageHandler handler, IMessage msg, MessageContext ctx) {
        IMessage reply = (IMessage) handler.onMessage(msg, ctx);
        // R3a: replies are not resent yet (R3b wires the reply path).
        if (reply == null) {
            return;
        }
        System.out.println("[MTN-LAYER] reply generated for " + msg.getClass().getSimpleName()
                + " (reply send in R3b)");
    }

    /** Encode a message through its registered codec into a wire payload. */
    public static RawPayload encode(IMessage msg, String channel) {
        for (Map.Entry<FMLIndexedMessageToMessageCodec<?>, String> e : CHANNELS.entrySet()) {
            FMLIndexedMessageToMessageCodec<?> codec = e.getKey();
            if (!codec.classToId().containsKey(msg.getClass())) {
                continue;
            }
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            MessageContext ctx = new MessageContext(MessageContext.Side.CLIENT);
            try {
                codec.encode(ctx, msg, buf);
            } catch (RuntimeException ex) {
                System.out.println("[MTN-LAYER] encode failed for " + msg.getClass().getSimpleName()
                        + ": " + ex.getMessage());
            }
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new RawPayload(ResourceLocation.fromNamespaceAndPath(CHANNEL_PREFIX, channel), data);
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
