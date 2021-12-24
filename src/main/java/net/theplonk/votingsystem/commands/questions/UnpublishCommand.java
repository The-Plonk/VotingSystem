package net.theplonk.votingsystem.commands.questions;

import net.theplonk.votingsystem.commands.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnpublishCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "unpublish";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        return false;
    }
}
