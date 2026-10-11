package mtn.forge_layer.cpw.mods.fml.common.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Static holder remembering which mod owns a GUI id. When an
 * {@code OpenGuiMessage} arrives client-side, the owning mod is resolved from
 * the gui id so its {@link IGuiHandler} can be looked up.
 */
public class GuiIdTracker {

    private static final Map<Integer, Object> modByGuiId = new HashMap<>();

    private GuiIdTracker() {
    }

    public static void put(int guiId, Object mod) {
        modByGuiId.put(guiId, mod);
    }

    public static Object get(int guiId) {
        return modByGuiId.get(guiId);
    }
}
