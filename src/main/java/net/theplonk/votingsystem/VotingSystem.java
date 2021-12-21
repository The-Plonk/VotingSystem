package net.theplonk.votingsystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.theplonk.votingsystem.commands.BaseCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VotingSystem extends JavaPlugin {

    private File configFile;
    private final Map<String, Object> configMap = new HashMap<>();
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable() {
        this.registerCommands();
    }

    @Override
    public void onDisable() {

    }

    public void registerCommands() {
        BaseCommand voteCommand = new BaseCommand();
        BaseCommand questionCommand = new BaseCommand();
        // Register /vote and /question command
        Objects.requireNonNull(this.getCommand("vote")).setExecutor(voteCommand);
        Objects.requireNonNull(this.getCommand("question")).setExecutor(questionCommand);
        // Register /vote sub-commands
//        voteCommand.registerSubCommand();
        // Register /question sub-commands
//        questionCommand.registerSubCommand();
    }




}
