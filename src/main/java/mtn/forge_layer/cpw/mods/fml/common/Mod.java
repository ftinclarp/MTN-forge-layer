package mtn.forge_layer.cpw.mods.fml.common;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.Mod} (Forge 1.7.10).
 * Placed in {@code mtn.forge_layer.*} to avoid any collision with real
 * Forge or Fabric classes.
 */
public @interface Mod {

    String modid();

    String name();

    String version();

    /**
     * Stand-in for {@code cpw.mods.fml.common.Mod.EventHandler} — marks a
     * lifecycle method as a Forge lifecycle event handler. Kept as a nested
     * annotation to match the original Forge layout.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EventHandler {
    }
}
