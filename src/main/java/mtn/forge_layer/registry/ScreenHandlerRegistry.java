package mtn.forge_layer.registry;

import mtn.minecraft_layer.net.minecraft.inventory.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps a GUI id to a function producing the {@link Container} (server side) that
 * backs the screen handler for that GUI. The example mod creates its
 * {@code Container} in the GUI handler; this registry lets the layer create the
 * menu independently of the player-side handler lookup.
 */
public final class ScreenHandlerRegistry {

    /** function to build the forge container from (syncId is unused here). */
    private static final Map<Integer, Function<Object[], Container>> BUILDERS =
            new HashMap<>();

    private ScreenHandlerRegistry() {
    }

    public static void register(int guiId, Function<Object[], Container> builder) {
        BUILDERS.put(guiId, builder);
    }

    public static Container build(int guiId, Object player, Object world,
            int x, int y, int z) {
        Function<Object[], Container> builder = BUILDERS.get(guiId);
        if (builder == null) {
            return null;
        }
        return builder.apply(new Object[]{player, world, x, y, z});
    }

    public static boolean has(int guiId) {
        return BUILDERS.containsKey(guiId);
    }
}
