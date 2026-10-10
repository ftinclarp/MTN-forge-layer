package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A network message (Forge 1.7.10 {@code cpw.mods.fml.common.network.simpleimpl.IMessage}).
 *
 * <p>Uses the real Mojmap {@link FriendlyByteBuf} (the pass-through class the import
 * rewriter preserves), so ported {@code fromBytes}/{@code toBytes} bodies can call the
 * vanilla buffer helpers directly or through {@code ByteBufUtils}.
 */
public interface IMessage {

    /** Populate this message from the given byte buffer (decoding). */
    void fromBytes(FriendlyByteBuf buf);

    /** Serialize this message to the given byte buffer (encoding). */
    void toBytes(FriendlyByteBuf buf);
}
