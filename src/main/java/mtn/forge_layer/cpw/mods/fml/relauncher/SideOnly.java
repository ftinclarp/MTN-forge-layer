package mtn.forge_layer.cpw.mods.fml.relauncher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy stand-in for {@code cpw.mods.fml.relauncher.SideOnly} (Forge 1.7.10).
 * Marks client-only members; the layer's runtime never loads server-only
 * classes, so this is a compile-time marker with no effect.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface SideOnly {
    Side value();
}
