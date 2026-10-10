package mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl;

/**
 * Context passed to {@link IMessageHandler#onMessage}.
 *
 * <p>Forge 1.7.10 {@code MessageContext} exposes which {@link Side} the handler runs
 * on; the side is often used to build the reply's {@code Side} for
 * {@code NetworkRegistry.newSimpleChannel(...)} calls. {@code getSide()} returns a
 * {@link Side} from {@code mtm.forge_layer.cpw.mods.fml.relauncher}.
 */
public class MessageContext {

    /**
     * Which side this message arrived on.
     */
    public enum Side {
        CLIENT,
        SERVER
    }

    private final Side side;

    public MessageContext(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    /** The enum value as a FML relauncher {@code Side} (CLIENT/SERVER identical). */
    public mtn.forge_layer.cpw.mods.fml.relauncher.Side fmlSide() {
        return side == Side.CLIENT
                ? mtn.forge_layer.cpw.mods.fml.relauncher.Side.CLIENT
                : mtn.forge_layer.cpw.mods.fml.relauncher.Side.SERVER;
    }
}
