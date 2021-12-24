package net.theplonk.votingsystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.theplonk.votingsystem.commands.BaseCommand;
import net.theplonk.votingsystem.commands.VoteCommand;
import net.theplonk.votingsystem.managers.SettingsManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VotingSystem extends JavaPlugin {

    @Getter public static VotingSystem instance;
    @Getter private SettingsManager config;
    private BukkitAudiences adventure;
    private final Map<String, Object> configMap = new HashMap<>();
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands();
        // Create the BukkitAudience (adventure-api)
        this.adventure = BukkitAudiences.create(this);
        // Load Configurations using SettingsManager
        config.init();
    }

    @Override
    public void onDisable() {
        // Close the BukkitAudience
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    // Register commands
    public void registerCommands() {
        BaseCommand questionCommand = new BaseCommand();
        // Register /vote and /question command
        Objects.requireNonNull(this.getCommand("vote")).setExecutor(new VoteCommand());
        Objects.requireNonNull(this.getCommand("question")).setExecutor(questionCommand);

    }

    // Get the adventure API
    public @NonNull BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public void setConfigValues() {

    }



}
