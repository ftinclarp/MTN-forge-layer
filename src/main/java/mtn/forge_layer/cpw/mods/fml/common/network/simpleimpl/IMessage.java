package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

import io.netty.buffer.ByteBuf;

/**
 * A network message (Forge 1.7.10 {@code cpw.mods.fml.common.network.simpleimpl.IMessage}).
 *
 * <p>Matches the real Forge 1.7.10 signatures: {@code fromBytes}/{@code toBytes}
 * take {@link ByteBuf}. Ported code compiled against real Forge uses
 * {@code io.netty.buffer.ByteBuf} and {@code ByteBufUtils}, so the layer must as well.
 */
public interface IMessage {

    /** Populate this message from the given byte buffer (decoding). */
    void fromBytes(ByteBuf buf);

    /** Serialize this message to the given byte buffer (encoding). */
    void toBytes(ByteBuf buf);
}
