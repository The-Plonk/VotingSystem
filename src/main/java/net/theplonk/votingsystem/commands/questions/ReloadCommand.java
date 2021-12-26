package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        VotingSystemConfig config = plugin.getVotingConfig();
        plugin.reloadConfigs();

        Audience audience = plugin.getAdventure().sender(sender);
        audience.sendMessage(config.getMessageComponentPlain("successfully reloaded"));
        return true;
    }
}
