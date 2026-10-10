package mtn.forge_layer.cpw.mods.fml.common.network;

import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessage;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.MessageContext;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec}.
 *
 * <p>Assigns a one-byte discriminator to each message class registered via
 * {@link #addDiscriminator(int, Class)}. {@code encode}/{@code decode} look up the
 * class from its discriminator and delegate the actual (de)serialization to the
 * overridable {@link #encodeInto}/{@link #decodeInto} hooks. The Fabric networking
 * bridge walks {@link NetworkRegistry#codecs()} to install wire receivers.
 */
public abstract class FMLIndexedMessageToMessageCodec<A> {

    private final Map<Byte, Class<? extends IMessage>> idToClass = new HashMap<>();
    private final Map<Class<? extends IMessage>, Byte> classToId = new HashMap<>();

    protected FMLIndexedMessageToMessageCodec() {
    }

    public FMLIndexedMessageToMessageCodec<A> addDiscriminator(
            int id, Class<? extends IMessage> type) {
        idToClass.put((byte) id, type);
        classToId.put(type, (byte) id);
        return this;
    }

    // encode/decode helpers — subclasses override encodeInto/decodeInto
    protected abstract void encodeInto(MessageContext ctx, IMessage msg,
            FriendlyByteBuf buf);

    protected abstract void decodeInto(MessageContext ctx,
            FriendlyByteBuf buf, IMessage msg);

    // package-private accessors for the runtime bridge
    public Map<Byte, Class<? extends IMessage>> idToClass() {
        return idToClass;
    }

    public Map<Class<? extends IMessage>, Byte> classToId() {
        return classToId;
    }

    // public API used by mods:
    public void encode(MessageContext ctx, IMessage msg,
            FriendlyByteBuf buf) {
        encodeInto(ctx, msg, buf);
    }

    public IMessage decode(MessageContext ctx,
            FriendlyByteBuf buf) {
        byte id = buf.readByte();
        Class<? extends IMessage> type = idToClass.get(id);
        if (type == null) {
            throw new RuntimeException("Unknown discriminator: " + id);
        }
        try {
            IMessage msg = type.getDeclaredConstructor().newInstance();
            decodeInto(ctx, buf, msg);
            return msg;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
