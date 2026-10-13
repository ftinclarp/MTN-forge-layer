package net.minecraftforge.event.entity.living;

import mtn.minecraft_layer.net.minecraft.entity.EntityLivingBase;

/**
 * Dummy stand-in for {@code net.minecraftforge.event.entity.living.LivingEvent}
 * (Forge 1.7.10). Base class of the living entity events.
 */
public class LivingEvent {

    // The entity that fired the event (for 1.7.10 this field is public).
    public EntityLivingBase entityLiving;

    public static class LivingUpdateEvent extends LivingEvent {

        // TODO(mtn-layer): fire this each tick for living entities.
        public LivingUpdateEvent(EntityLivingBase entity) {
            this.entityLiving = entity;
        }
    }
}
