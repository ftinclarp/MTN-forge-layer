package mtn.forge_layer.cpw.mods.fml.common;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.ObfuscationReflectionHelper}.
 * Reads a private field by index; placeholder returns null.
 */
public final class ObfuscationReflectionHelper {
    private ObfuscationReflectionHelper() {
    }

    // TODO(mtn-layer): look up the SRG name for the field index and read it.
    public static <T, E> T getPrivateValue(Class<? super E> classToAccess, E instance, int fieldIndex) {
        return null;
    }
}
