package mtn.forge_layer.cpw.mods.fml.common.event;

import java.io.File;
import java.util.Properties;

import mtn.forge_layer.cpw.mods.fml.common.ModMetadata;

/**
 * Dummy stand-in for {@code cpw.mods.fml.common.event.FMLPreInitializationEvent}
 * (Forge 1.7.10). Carries the pre-init metadata + suggested config file.
 */
public class FMLPreInitializationEvent {

    // TODO(mtn-layer): feed the real Fabric loader metadata + config dir.
    public File getSuggestedConfigurationFile() {
        return new File("config", "mod.cfg");
    }

    public Properties getVersionProperties() {
        return new Properties();
    }

    public ModMetadata getModMetadata() {
        return new ModMetadata();
    }
}
