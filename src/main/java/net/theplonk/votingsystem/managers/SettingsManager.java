package net.theplonk.votingsystem.managers;


import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.util.YamlConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SettingsManager {

    private static final VotingSystem plugin = VotingSystem.getInstance();
    public List<Map<?, ?>> questions;
    public int announceInterval;


    public void init() {
        // Load Configuration File
        plugin.getLogger().info("Loading configurations...");
        File file = new File(plugin.getDataFolder(), "config.yml");

        // Check if file exists, if not, then create it
        if(!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        YamlConfig yamlConfig = YamlConfig.loadConfiguration(file);
        // Try to sync with file and load
        try {
            yamlConfig.syncWithConfig(file, plugin.getResource("config.yml"));
            plugin.getLogger().info("Loaded configurations.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configurations. Printing stack trace:");
            e.printStackTrace();
        }

        questions = yamlConfig.getMapList("questions");
        announceInterval = yamlConfig.getInt("announce interval");
    }

    public boolean reloadConfiguration() {
        try {
            init();
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }

        return true;
    }
}