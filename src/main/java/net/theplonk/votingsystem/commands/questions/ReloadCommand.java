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
        Audience audience = plugin.getAdventure().sender(sender);
        VotingSystemConfig config = plugin.getVotingConfig();

        if (!sender.hasPermission("votingsystem.question.reload")) {
            audience.sendMessage(config.getMessageComponentPlain("no permission"));
            return true;
        }

        plugin.reloadConfigs();
        audience.sendMessage(config.getMessageComponentPlain("successfully reloaded"));
        return true;
    }
}
