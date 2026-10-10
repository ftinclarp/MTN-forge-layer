package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

/**
 * Handler for a network message (Forge 1.7.10 {@code IMessageHandler}).
 *
 * @param <T>     message type handled
 * @param <REPLY> reply message type (may be the same message type), or
 *                {@code IMessage} for no reply
 */
public interface IMessageHandler<T extends IMessage, REPLY extends IMessage> {

    /** Handle the received message, optionally returning a reply. */
    REPLY onMessage(T message, MessageContext ctx);
}
