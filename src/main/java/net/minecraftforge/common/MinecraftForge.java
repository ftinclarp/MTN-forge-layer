package net.minecraftforge.common;

import mtn.forge_layer.cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stand-in for {@code net.minecraftforge.common.MinecraftForge}
 * (Forge 1.7.10): the global event bus.
 *
 * <p>Classic Forge semantics are preserved: an event is a POJO whose
 * dispatch type is the method's parameter type; {@code register} scans
 * the subscriber class reflectively, groups handlers by event class, and
 * {@code fire} invokes them in registration order.
 */
public final class MinecraftForge {

    private static final Logger LOGGER = LoggerFactory.getLogger("mtn_forge_layer_events");

    /** The single Forge event bus. */
    public static final EventBus EVENT_BUS = new EventBus();

    private MinecraftForge() {
    }

    /** Reflective event bus with real dispatch, an in-memory registry alongside. */
    public static final class EventBus {

        /** eventClass -> ordered handlers. */
        private final Map<Class<?>, List<Handler>> byType =
                new ConcurrentHashMap<>();

        private EventBus() {
        }

        /**
         * Scan {@code subscriber}'s class (and supertypes) for methods
         * annotated {@link SubscribeEvent} with exactly one parameter;
         * group by that parameter type.
         */
        public void register(Object subscriber) {
            if (subscriber == null) {
                return;
            }
            Class<?> type = subscriber.getClass();
            for (Method m : type.getMethods()) {
                if (!m.isAnnotationPresent(SubscribeEvent.class)
                        || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> eventClass = m.getParameterTypes()[0];
                MethodHandle handle;
                try {
                    handle = MethodHandles.lookup().unreflect(m);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Cannot access {}#{}", type.getSimpleName(), m.getName(), e);
                    continue;
                }
                lru(eventClass).add(new Handler(subscriber, m, handle));
                LOGGER.info("Registered {}#{} for {}",
                        type.getSimpleName(), m.getName(), eventClass.getName());
            }
        }

        /** Remove every handler whose subscriber instance is the given one. */
        public void unregister(Object subscriber) {
            if (subscriber == null) {
                return;
            }
            byType.values().removeIf(
                    list -> list.removeIf(h -> h.subscriber == subscriber)
                            && list.isEmpty());
        }

        /** Invoke all registered handlers for {@code event}'s class in order. */
        public void fire(Object event) {
            if (event == null) {
                return;
            }
            List<Handler> handlers = byType.get(event.getClass());
            if (handlers == null || handlers.isEmpty()) {
                return;
            }
            for (Handler h : new ArrayList<>(handlers)) {
                try {
                    h.invoke(event);
                } catch (Throwable t) {
                    LOGGER.error("Event handler threw in {}", event.getClass().getSimpleName(), t);
                }
            }
        }

        /** Whether any handler is registered for the given event class. */
        public boolean hasHandlers(Class<?> eventClass) {
            List<Handler> handlers = byType.get(eventClass);
            return handlers != null && !handlers.isEmpty();
        }

        private List<Handler> lru(Class<?> eventClass) {
            return byType.computeIfAbsent(eventClass, c -> new ArrayList<>());
        }

        private record Handler(Object subscriber, Method method, MethodHandle handle) {
            void invoke(Object event) throws Throwable {
                handle.bindTo(subscriber).invoke(event);
            }
        }
    }
}
