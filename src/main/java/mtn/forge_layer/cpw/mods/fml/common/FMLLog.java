package mtn.forge_layer.cpw.mods.fml.common;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.FMLLog} (Forge 1.7.10).
 * Logs into the Fabric/log4j logger; placeholder swallows.
 */
public final class FMLLog {
    private FMLLog() {
    }

    // TODO(mtn-layer): route into the Fabric-side logger with the right level.
    public static void log(org.apache.logging.log4j.Level level, Throwable throwable, String format, Object... args) {
    }

    public static void log(org.apache.logging.log4j.Level level, String format, Object... args) {
    }

    public static void warning(String format, Object... args) {
    }
}
