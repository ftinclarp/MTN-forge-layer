package mtn.forge_layer.cpw.mods.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.SidedProxy} (Forge 1.7.10).
 * Must be {@link RetentionPolicy#RUNTIME} so the compatibility layer can
 * reflectively discover and inject the proxy field at runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SidedProxy {

    String clientSide();

    String serverSide();
}
