package mtn.forge_layer.cpw.mods.fml.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import mtn.forge_layer.cpw.mods.fml.common.network.internal.FMLProxyPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Forge 1.7.10 {@code cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec}
 * shape: a netty {@code MessageToMessageCodec} that grows a discriminator byte in
 * front of each mod message and dispatches the rest to the overridable
 * {@code encodeInto}/{@code decodeInto} hooks.
 *
 * <p>The abstract hooks use the real netty signatures
 * ({@code ChannelHandlerContext} + {@code ByteBuf} + the mod message generic), which
 * is exactly what ported mods like IronChest override — NO {@code MessageContext} and
 * NO {@code FriendlyByteBuf}. The concrete netty {@code encode}/{@code decode} methods
 * write/read the discriminator byte first, mirroring Forge 1.7.10.
 *
 * <p>Marked {@code @Sharable} so one instance may be placed in the client and server
 * embedded pipelines of a channel pair, matching Forge.
 */
@Sharable
public abstract class FMLIndexedMessageToMessageCodec<A>
        extends MessageToMessageCodec<FMLProxyPacket, A> {

    private final Map<Byte, Class<? extends A>> idToClass = new HashMap<>();
    private final Map<Class<? extends A>, Byte> classToId = new HashMap<>();

    protected FMLIndexedMessageToMessageCodec() {
    }

    public FMLIndexedMessageToMessageCodec<A> addDiscriminator(
            int discriminator, Class<? extends A> type) {
        idToClass.put((byte) discriminator, type);
        classToId.put(type, (byte) discriminator);
        return this;
    }

    /** Real Forge abstract hook overridden by mod packets. */
    public abstract void encodeInto(ChannelHandlerContext ctx, A msg, ByteBuf target)
            throws Exception;

    /** Real Forge abstract hook overridden by mod packets. */
    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf source, A msg)
            throws Exception;

    @Override
    protected final void encode(ChannelHandlerContext ctx, A msg,
            List<Object> out) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        byte discriminator = classToId.get(msg.getClass());
        buffer.writeByte(discriminator);
        encodeInto(ctx, msg, buffer);
        String channel = channelName(ctx);
        out.add(new FMLProxyPacket(buffer.copy(), channel));
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, FMLProxyPacket msg,
            List<Object> out) throws Exception {
        ByteBuf payload = msg.payload().duplicate();
        byte discriminator = payload.readByte();
        Class<? extends A> type = idToClass.get(discriminator);
        if (type == null) {
            throw new RuntimeException("Undefined message for discriminator "
                    + discriminator + " in channel " + msg.channel());
        }
        A newMsg = type.getDeclaredConstructor().newInstance();
        decodeInto(ctx, payload, newMsg);
        out.add(newMsg);
    }

    /** Best-effort channel name from the netty pipeline attribute, else empty. */
    private static String channelName(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return "";
        }
        Object name = ctx.channel().attr(
                NetworkRegistry.FML_CHANNEL_KEY).get();
        return name == null ? "" : String.valueOf(name);
    }

    // Public accessors used by the wrapper/bridge to look up class→id.
    public Map<Byte, Class<? extends A>> idToClass() {
        return idToClass;
    }

    public Map<Class<? extends A>, Byte> classToId() {
        return classToId;
    }
}
