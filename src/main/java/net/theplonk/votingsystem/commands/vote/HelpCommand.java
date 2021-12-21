package net.theplonk.votingsystem.commands.vote;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "help";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(MiniMessage.get().parse("<yellow>Help Menu"));
        return true;
    }
}
