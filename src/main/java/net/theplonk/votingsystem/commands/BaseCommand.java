package net.theplonk.votingsystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BaseCommand implements CommandExecutor, TabExecutor {

    private final Map<String, AbstractSubCommand> subCommands = new HashMap<>();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check if args length is greater than 0
        if (args.length > 0) {
            String trySubCommand = args[0];
            // Run specified sub command and return true
            if (subCommands.containsKey(trySubCommand.toLowerCase())) {
                subCommands.get(trySubCommand.toLowerCase()).onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }
        // If failed, then return help menu with no args
        subCommands.get("help").onCommand(sender, null);
        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Return the sub commands
        if (args.length == 1) {
            return new ArrayList<>(subCommands.keySet());
        }

        return null;
    }

    public void registerSubCommand(AbstractSubCommand command) {
        // Add the specified sub command to the hashmap
        subCommands.put(command.getLabel().toLowerCase(), command);
    }
}
