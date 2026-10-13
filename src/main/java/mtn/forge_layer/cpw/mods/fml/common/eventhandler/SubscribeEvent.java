package mtn.forge_layer.cpw.mods.fml.common.eventhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.eventhandler.SubscribeEvent}
 * (Forge 1.7.10). Marks a method as an event handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
}
