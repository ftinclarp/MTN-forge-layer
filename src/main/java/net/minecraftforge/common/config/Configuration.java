package net.minecraftforge.common.config;

import java.io.File;

/**
 * Dummy stand-in for {@code net.minecraftforge.common.config.Configuration}
 * (Forge 1.7.10). No-op config that compiles and never persists.
 */
public class Configuration {

    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_BLOCK = "block";
    public static final String CATEGORY_ITEM = "item";

    private File file;

    public Configuration(File file) {
        this.file = file;
    }

    // TODO(mtn-layer): parse the real config file (properties or TOML).
    public void load() {
    }

    // TODO(mtn-layer): return the value for the given key, falling back to
    //   the default; placeholder always returns the default.
    public Property get(String category, String key, boolean defaultValue) {
        return new Property(category, key, defaultValue);
    }

    // TODO(mtn-layer): return the value for the given key, falling back to
    //   the default; placeholder always returns the default.
    public Property get(String category, String key, String defaultValue) {
        return new Property(category, key, defaultValue);
    }

    // TODO(mtn-layer): return the value for the given key, falling back to
    //   the default; placeholder always returns the default.
    public Property get(String category, String key, int defaultValue) {
        return new Property(category, key, defaultValue);
    }

    public boolean hasChanged() {
        return false;
    }

    // TODO(mtn-layer): persist the config back to the file.
    public void save() {
    }

    /**
     * Dummy stand-in for {@code net.minecraftforge.common.config.Property}.
     */
    public static class Property {
        private final Object value;

        public Property(String category, String key, Object defaultValue) {
            this.value = defaultValue;
        }

        public boolean getBoolean(boolean defaultValue) {
            return value instanceof Boolean ? (Boolean) value : defaultValue;
        }

        public String getString() {
            return String.valueOf(value);
        }

        public int getInt() {
            return value instanceof Number ? ((Number) value).intValue() : 0;
        }
    }
}
