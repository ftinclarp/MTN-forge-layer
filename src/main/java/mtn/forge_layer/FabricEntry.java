package mtn.forge_layer;

import mtn.forge_layer.cpw.mods.fml.common.Mod;
import mtn.forge_layer.cpw.mods.fml.common.SidedProxy;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLInitializationEvent;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLPostInitializationEvent;
import mtn.forge_layer.cpw.mods.fml.common.event.FMLPreInitializationEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Fabric entry point for the Forge 1.7.10 compatibility shim.
 *
 * <p>At {@code onInitialize} it scans the classpath for a class carrying
 * {@link Mod}, instantiates it, injects {@link SidedProxy} fields based on
 * the current environment, then dispatches the {@code @Mod.EventHandler}
 * lifecycle methods in order: pre-init, init, post-init.
 *
 * <p>Scan approach: enumerate the URLs of the Fabric Loader class loader
 * (a {@link URLClassLoader}), open each jar/directory, and reflectively load
 * the well-known namespaces the ported mod could not live in is skipped for
 * speed. If no {@code @Mod} class is found, an explicit warning is logged
 * and nothing else happens — the game continues normally.
 */
public final class FabricEntry implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mtn_forge_layer");

    /** Root class loader of Fabric Loader. */
    private static final ClassLoader CLASS_LOADER = FabricEntry.class.getClassLoader();

    /** Prefixes of packages the ported mod would never define (skip for speed). */
    private static final String[] SKIP_PREFIXES = {
        "net.minecraft.", "net.fabricmc.", "com.mojang.", "org.slf4j.",
        "org.apache.", "com.google.", "io.netty.", "it.unimi.", "org.lwjgl.",
        "com.ibm.", "org.joml.", "mtn.forge_layer."
    };

    @Override
    public void onInitialize() {
        Class<?> modClass = findModClass();
        if (modClass == null) {
            LOGGER.warn(
                    "No class annotated with {} found on the classpath; nothing to dispatch.",
                    Mod.class.getName());
            return;
        }
        try {
            dispatch(modClass);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to initialize ported mod {}", modClass.getName(), e);
        }
    }

    /** Scan the classpath for the single {@code @Mod}-annotated class. */
    private static Class<?> findModClass() {
        if (!(CLASS_LOADER instanceof URLClassLoader urlLoader)) {
            LOGGER.warn("Class loader is not a URLClassLoader; cannot scan classpath.");
            return null;
        }
        for (URL url : urlLoader.getURLs()) {
            String spec = url.getFile();
            if (spec == null || spec.isEmpty()) {
                continue;
            }
            if (spec.endsWith(".jar")) {
                Class<?> found = scanJar(url);
                if (found != null) {
                    return found;
                }
            } else {
                Class<?> found = scanDirectory(new java.io.File(url.getPath()));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Class<?> scanJar(URL url) {
        try (JarFile jar = new JarFile(url.getFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                Class<?> candidate = loadCandidate(entry.getName());
                if (candidate != null) {
                    return candidate;
                }
            }
        } catch (java.io.IOException e) {
            LOGGER.debug("Skipping unreadable jar {}: {}", url, e.getMessage());
        }
        return null;
    }

    private static Class<?> scanDirectory(java.io.File dir) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (java.io.File file : files) {
            Class<?> found = file.isDirectory() ? scanDirectory(file) : null;
            if (found == null && file.isFile() && file.getName().endsWith(".class")) {
                found = loadCandidate(file.getPath());
            }
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Convert a {@code .class} path fragment to a binary class name, skip
     * known-shared namespaces, load the class and check for {@link Mod}.
     *
     * @param classPath path fragment like {@code com/mtn/example/ExampleMod.class}
     */
    private static Class<?> loadCandidate(String classPath) {
        String name = classPath
                .replace('\\', '/')
                .replace('/', '.')
                .replaceFirst("\\.class$", "");
        for (String prefix : SKIP_PREFIXES) {
            if (name.startsWith(prefix)) {
                return null;
            }
        }
        try {
            Class<?> type = Class.forName(name, false, CLASS_LOADER);
            if (type.isAnnotationPresent(Mod.class)
                    && type.getAnnotation(Mod.class) != null) {
                LOGGER.info("Found ported @Mod class: {}", type.getName());
                return type;
            }
        } catch (ClassNotFoundException | LinkageError e) {
            // class depends on MC at load time with initialize=false only the
            // type name is resolved; still safe to skip un-initializable types
            // like traits or synthetic helpers.
        }
        return null;
    }

    /** Instantiate the ported mod, wire proxies, then run lifecycle handlers. */
    private static void dispatch(Class<?> modClass) throws ReflectiveOperationException {
        Object instance = modClass.getConstructor().newInstance();
        LOGGER.info("Instantiated ported mod {}", modClass.getName());

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
