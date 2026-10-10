package mtn.forge_layer;

import mtn.forge_layer.cpw.mods.fml.common.Mod;
import mtn.forge_layer.cpw.mods.fml.common.SidedProxy;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLInitializationEvent;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLPostInitializationEvent;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLPreInitializationEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Fabric entry point for the Forge 1.7.10 compatibility shim.
 *
 * <p>At {@code onInitialize} it loads the ported Forge mod's {@code @Mod}
 * class through the {@code "mtn:forge-mod-class"} entrypoint declared in
 * the mod's {@code fabric.mod.json} (emitted by the MTN transform stage),
 * instantiates it, injects {@link SidedProxy} fields based on the current
 * environment, then dispatches the {@code @Mod.EventHandler} lifecycle
 * methods in order: pre-init, init, post-init.
 *
 * <p>Discovery approach: the {@code "mtn:forge-mod-class"} entrypoint names
 * the fully-qualified {@code @Mod} class explicitly, so no classpath
 * scanning is needed. This works under Fabric's Knot class loader (which is
 * not a {@link java.net.URLClassLoader}) and avoids scanning entirely.
 * The transform stage emits the entrypoint automatically; see its
 * {@code mtn:forge-mod-class} handling in TransformStage.
 *
 * <p>If no entrypoint is present, an explicit warning is logged and nothing
 * else happens — the game continues normally.
 */
public final class FabricEntry implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mtn_forge_layer");

    /** Fabric entrypoint key that names the ported mod's {@code @Mod} class. */
    public static final String FORGE_MOD_CLASS_ENTRYPOINT = "mtn:forge-mod-class";

    /** Ensures the dispatch runs at most once even if {@code onInitialize} is called twice. */
    private static boolean dispatched = false;

    @Override
    public void onInitialize() {
        if (dispatched) {
            return;
        }
        dispatched = true;
        List<Object> modInstances = loadModInstances();
        if (modInstances.isEmpty()) {
            LOGGER.warn(
                    "No {} entrypoint found; nothing to dispatch. "
                            + "Is fabric.mod.json missing \"{}\"?",
                    FORGE_MOD_CLASS_ENTRYPOINT, FORGE_MOD_CLASS_ENTRYPOINT);
            return;
        }
        for (Object instance : modInstances) {
            try {
                dispatch(instance);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to initialize ported mod {}", instance.getClass().getName(), e);
            }
        }
    }

    /**
     * Obtain every instance named by the {@code mtn:forge-mod-class}
     * entrypoint. Fabric Loader's default language adapter instantiates each
     * named class via its no-arg constructor and casts it to the requested
     * entrypoint type — so {@code Object.class} requests yield the instances
     * themselves (casting a class to {@code String.class} would fail).
     */
    private static List<Object> loadModInstances() {
        List<Object> result = new ArrayList<>();
        List<EntrypointContainer<Object>> containers;
        try {
            containers = FabricLoader.getInstance()
                    .getEntrypointContainers(FORGE_MOD_CLASS_ENTRYPOINT, Object.class);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to read {} entrypoint: {}", FORGE_MOD_CLASS_ENTRYPOINT, e.getMessage());
            return result;
        }
        for (EntrypointContainer<Object> container : containers) {
            Object instance = container.getEntrypoint();
            if (instance == null || !instance.getClass().isAnnotationPresent(Mod.class)) {
                LOGGER.warn("Entrypoint {} is not annotated with {}; skipping.",
                        instance == null ? "<null>" : instance.getClass().getName(), Mod.class.getName());
                continue;
            }
            LOGGER.info("Found ported @Mod class via entrypoint: {}", instance.getClass().getName());
            result.add(instance);
        }
        return result;
    }

    /** Wire proxies on the ported mod instance, then run lifecycle handlers. */
    private static void dispatch(Object instance) throws ReflectiveOperationException {
        Class<?> modClass = instance.getClass();
        LOGGER.info("Initializing ported mod instance {}", modClass.getName());

        EnvType env = FabricLoader.getInstance().getEnvironmentType();
        for (Field field : modClass.getDeclaredFields()) {
            SidedProxy sided = field.getAnnotation(SidedProxy.class);
            if (sided == null) {
                continue;
            }
            String side = env == EnvType.CLIENT ? sided.clientSide() : sided.serverSide();
            Class<?> proxyType = Class.forName(side, true, modClass.getClassLoader());
            Object proxy = proxyType.getConstructor().newInstance();
            field.setAccessible(true);
            field.set(instance, proxy);
            LOGGER.info("Injected @SidedProxy {} = {}", field.getName(), side);
        }

        List<Method> pre = handlersFor(modClass, FMLPreInitializationEvent.class);
        List<Method> init = handlersFor(modClass, FMLInitializationEvent.class);
        List<Method> post = handlersFor(modClass, FMLPostInitializationEvent.class);

        for (Method m : pre) {
            invoke(m, instance, new FMLPreInitializationEvent());
        }
        for (Method m : init) {
            invoke(m, instance, new FMLInitializationEvent());
        }
        for (Method m : post) {
            invoke(m, instance, new FMLPostInitializationEvent());
        }
    }

    /** All {@code @Mod.EventHandler} methods that take the given event type. */
    private static List<Method> handlersFor(Class<?> modClass, Class<?> eventType) {
        List<Method> result = new ArrayList<>();
        for (Method m : modClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Mod.EventHandler.class)
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == eventType) {
                result.add(m);
            }
        }
        return result;
    }

    private static void invoke(Method m, Object instance, Object event)
            throws ReflectiveOperationException {
        m.setAccessible(true);
        m.invoke(instance, event);
        LOGGER.info("Dispatched {}({})", m.getName(), event.getClass().getSimpleName());
    }
}
