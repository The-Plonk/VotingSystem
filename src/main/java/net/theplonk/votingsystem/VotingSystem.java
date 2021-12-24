package net.theplonk.votingsystem;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.theplonk.votingsystem.commands.BaseCommand;
import net.theplonk.votingsystem.commands.VoteCommand;
import net.theplonk.votingsystem.commands.questions.PublishCommand;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import redempt.redlib.config.ConfigManager;

import java.util.Objects;

public class VotingSystem extends JavaPlugin {

    @Getter public static VotingSystem instance;
    private BukkitAudiences adventure;
    @Getter private final VotingSystemConfig votingConfig = new VotingSystemConfig();
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands();
        // Create the BukkitAudience (adventure-api)
        this.adventure = BukkitAudiences.create(this);
        this.loadConfigurations();

    }

    @Override
    public void onDisable() {
        // Close the BukkitAudience
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        configManager.save();
    }

    // Register commands
    public void registerCommands() {
        BaseCommand questionCommand = new BaseCommand();
        // Register /vote and /question command
        Objects.requireNonNull(this.getCommand("vote")).setExecutor(new VoteCommand());
        Objects.requireNonNull(this.getCommand("question")).setExecutor(questionCommand);
        questionCommand.registerSubCommand(new PublishCommand());

    }

    // Get the adventure API
    public @NonNull BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public void loadConfigurations() {
        getLogger().info("Loading configurations...");
        configManager = ConfigManager.create(this)
                .target(votingConfig).saveDefaults().load();
        getLogger().info("Loaded configurations!");
    }



}
