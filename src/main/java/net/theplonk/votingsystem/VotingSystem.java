package net.theplonk.votingsystem;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.theplonk.votingsystem.commands.BaseCommand;
import net.theplonk.votingsystem.commands.VoteCommand;
import net.theplonk.votingsystem.commands.questions.*;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import net.theplonk.votingsystem.util.DiscordWebhook;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import redempt.redlib.config.ConfigManager;
import redempt.redlib.sql.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.Objects;

public class VotingSystem extends JavaPlugin {

    @Getter private static VotingSystem instance;
    @Getter private final VotingSystemConfig votingConfig = new VotingSystemConfig();
    @Getter private SQLHelper sqlDatabase;
    @Getter private final DiscordWebhook discordWebhook = new DiscordWebhook(this.votingConfig.getDiscord_url());
    @Getter private final DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
    private ConfigManager configManager;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        // Create the BukkitAudience (adventure-api)
        this.adventure = BukkitAudiences.create(this);
        this.loadConfigurations();
        Connection connection = SQLHelper.openSQLite(this.getDataFolder().toPath().resolve("database.db"));
        this.sqlDatabase = new SQLHelper(connection);
        this.initializeDatabase();
        this.initializeWebhook();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        // Close the BukkitAudience
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        this.sqlDatabase.close();
    }

    // Register commands
    public void registerCommands() {
        BaseCommand questionCommand = new BaseCommand();
        // Register /vote and /question command
        Objects.requireNonNull(this.getCommand("vote")).setExecutor(new VoteCommand());
        Objects.requireNonNull(this.getCommand("question")).setExecutor(questionCommand);
        questionCommand.registerSubCommand(new PublishCommand());
        questionCommand.registerSubCommand(new HelpCommand());
        questionCommand.registerSubCommand(new ReloadCommand());
        questionCommand.registerSubCommand(new UnpublishCommand());
        questionCommand.registerSubCommand(new ActiveCommand());

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
        this.votingConfig.setMessages();
        configManager = ConfigManager.create(this)
                .target(votingConfig).saveDefaults().load();
        getLogger().info("Loaded configurations!");
    }

    public void reloadConfigs() {
        getLogger().info("Loading configurations...");
        this.votingConfig.setMessages();
        configManager.reload();
        getLogger().info("Loaded configurations!");
    }


    public void initializeDatabase() {
        sqlDatabase.execute("CREATE TABLE IF NOT EXISTS votes ( uuid VARCHAR(36) PRIMARY KEY, vote BOOLEAN NOT NULL );");
        sqlDatabase.execute("CREATE TABLE IF NOT EXISTS vote_data ( setting VARCHAR(50) PRIMARY KEY, value VARCHAR(255) NOT NULL );");
    }

    public void initializeWebhook() {
        discordWebhook.setUsername("The Plonk SMP | Voting");
        discordWebhook.setAvatarUrl("https://i.imgur.com/biyu4wv.png");
        embedObject.setFooter("smp.theplonk.net", "https://i.imgur.com/biyu4wv.png");
    }

    public void executeWebhook()  {
        discordWebhook.setEmbed(embedObject);
        discordWebhook.setUrl(this.votingConfig.getDiscord_url());
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    discordWebhook.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }



}
