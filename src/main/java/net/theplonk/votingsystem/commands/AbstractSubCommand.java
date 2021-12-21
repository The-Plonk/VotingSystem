package net.theplonk.votingsystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSubCommand implements CommandExecutor {

    public abstract String getLabel();
    public abstract void onCommand(@NotNull CommandSender sender, @NotNull String[] args);

}
