package mtn.forge_layer.cpw.mods.fml.common.network;

import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import mtn.forge_layer.cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.NetworkRegistry}.
 *
 * <p>Implements the real Forge 1.7.10 channel factory
 * {@link #newChannel(String, ChannelHandler...)} returning an
 * {@code EnumMap<Side, FMLEmbeddedChannel>}, and records every channel so the
 * Fabric networking bridge can route inbound bytes to the matching side channel's
 * netty pipeline. Also holds the per-mod {@link IGuiHandler} registry and mirrors the
 * factory {@code newSimpleChannel}.
 */
public class NetworkRegistry {

    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    /** Attribute key used to tag the channel name onto an {@link FMLEmbeddedChannel}. */
    public static final AttributeKey<String> FML_CHANNEL_KEY =
            AttributeKey.valueOf("fml:channelName");
    public static final AttributeKey<Side> CHANNEL_SOURCE_KEY =
            AttributeKey.valueOf("fml:channelSource");

    static {
        // R3b: register the layer's OpenGuiMessage channel during preInit (before install).
        mtn.forge_layer.registry.OpenGuiHandler.initChannel();
    }

    private final EnumMap<Side, Map<String, FMLEmbeddedChannel>> channels =
            new EnumMap<>(Side.class);
    private final Map<Integer, IGuiHandler> guiHandlers = new HashMap<>();

    // Legacy codec list (kept for the simpleimpl bridge paths that still use it).
    private final List<FMLIndexedMessageToMessageCodec<?>> codecs = new ArrayList<>();

    private NetworkRegistry() {
        channels.put(Side.CLIENT, new ConcurrentHashMap<>());
        channels.put(Side.SERVER, new ConcurrentHashMap<>());
    }

    /**
     * Real Forge 1.7.10 signature: create a channel pair. One embedded pipeline is
     * created per {@link Side}, each receiving the same {@code handlers} (must be
     * {@code @Sharable}-compatible so a single codec instance can sit in both).
     */
    public EnumMap<Side, FMLEmbeddedChannel> newChannel(String name,
            ChannelHandler... handlers) {
        if (channels.get(Side.CLIENT).containsKey(name)
                || channels.get(Side.SERVER).containsKey(name)) {
            throw new RuntimeException("That channel is already registered: " + name);
        }
        EnumMap<Side, FMLEmbeddedChannel> result = new EnumMap<>(Side.class);
        for (Side side : Side.values()) {
            FMLEmbeddedChannel channel =
                    new FMLEmbeddedChannel(name, side, handlers);
            channels.get(side).put(name, channel);
            result.put(side, channel);
        }
        return result;
    }

    /** Real Forge signature: look up a channel by name + source side. */
    public FMLEmbeddedChannel getChannel(String name, Side source) {
        return channels.get(source).get(name);
    }

    public boolean hasChannel(String channelName, Side source) {
        return channels.get(source).containsKey(channelName);
    }

    public java.util.Set<String> channelNamesFor(Side side) {
        return channels.get(side).keySet();
    }

    /** Legacy: keep the codec list for the bridge. */
    public void registerMessageCodec(FMLIndexedMessageToMessageCodec<?> codec) {
        if (codec != null && !codecs.contains(codec)) {
            codecs.add(codec);
        }
    }

    public List<FMLIndexedMessageToMessageCodec<?>> codecs() {
        return codecs;
    }

    /** Creates a new simple channel wrapper for the given channel name. */
    public SimpleNetworkWrapper newSimpleChannel(String channelName) {
        return new SimpleNetworkWrapper(channelName);
    }

    /** Registers the GUI handler owned by the given mod (keyed by identity). */
    public void registerGuiHandler(Object mod, IGuiHandler handler) {
        guiHandlers.put(System.identityHashCode(mod), handler);
    }

    /** Returns the GUI handler registered for the mod, or null. */
    public IGuiHandler getGuiHandler(Object mod) {
        return guiHandlers.get(System.identityHashCode(mod));
    }

    /**
     * Opens a GUI for the given player. On the server this sends an
     * {@link OpenGuiMessage}; on the client it resolves and opens the screen.
     * Delegates to {@code mtn.forge_layer.registry.OpenGuiHandler}.
     */
    public void openGui(Object mod, int id, Object player, Object world,
            int x, int y, int z) {
        mtn.forge_layer.registry.OpenGuiHandler.openGui(mod, id, player, world, x, y, z);
    }

    /** Real Forge 1.7.10 style: open a GUI for the current (unknown) player. */
    public void openGui(Object mod, int modGuiId,
            mtn.minecraft_layer.net.minecraft.world.World world,
            int x, int y, int z) {
        mtn.forge_layer.registry.OpenGuiHandler.openGui(mod, modGuiId, null, world, x, y, z);
    }

    /** Real Forge 1.7.10 style: open a GUI for a specific player. */
    public void openGui(mtn.minecraft_layer.net.minecraft.entity.player.EntityPlayer player,
            Object mod, int modGuiId,
            mtn.minecraft_layer.net.minecraft.world.World world,
            int x, int y, int z) {
        mtn.forge_layer.registry.OpenGuiHandler.openGui(mod, modGuiId, player, world, x, y, z);
    }
}
