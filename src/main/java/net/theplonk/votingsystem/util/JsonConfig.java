package net.theplonk.votingsystem.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A JSON configuration
 */
public class JsonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Plugin main;
    private Path path;
    private Map<String, Object> values = new HashMap<>();

    /**
     * Creates a JSON config
     *
     * @param main The plugin
     * @param path The path to the config
     */
    public JsonConfig(Plugin main, Path path) {
        this.main = main;
        this.path = path;
    }

    /**
     * Creates a JSON config in the plugin's data folder
     *
     * @param main The plugin
     * @param name The name of the config
     *
     * @see #JsonConfig(Plugin, Path)
     */
    public JsonConfig(Plugin main, String name) {
        this(main, main.getDataFolder().toPath().resolve(name));
    }

    /**
     * Returns a reference to the underlying map
     *
     * @return All values in this map
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Reloads the config
     *
     * @throws RuntimeException wrapping any IO exception that may occur
     */
    public void reload() {
        if (Files.notExists(path)) {
            main.saveResource(path.getFileName().toString(), false);
        }
        try {
            values.putAll(GSON.fromJson(Files.newBufferedReader(path), values.getClass()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the config
     *
     * @throws RuntimeException wrapping any IO exception that may occur
     */
    public void save() {
        String json = GSON.toJson(values);
        try {
            Files.write(
                    path,
                    Collections.singletonList(json),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}