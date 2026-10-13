package mtn.forge_layer.cpw.mods.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.Mod} (Forge 1.7.10).
 * Placed in {@code mtn.forge_layer.*} to avoid any collision with real
 * Forge or Fabric classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {

    String modid();

    String name();

    /** Forge 1.7.10 element with an empty-string default. */
    String version() default "";

    /**
     * Stand-in for the Forge {@code dependencies()} element. Declared as
     * {@code String[]} with a default so a single String literal like
     * {@code "required-after:Forge@[10.10,)"} is accepted at the call site.
     */
    String[] dependencies() default {};

    /**
     * Stand-in for {@code cpw.mods.fml.common.Mod.EventHandler} — marks a
     * lifecycle method as a Forge lifecycle event handler. Kept as a nested
     * annotation to match the original Forge layout.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EventHandler {
    }

    /**
     * Stand-in for {@code cpw.mods.fml.common.Mod.Instance} — marks the
     * static field holding the mod's singleton instance.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Instance {
        /** Forge 1.7.10: the mod id this field names. */
        String value() default "";
    }
}
