package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "help";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        VotingSystemConfig config = plugin.getVotingConfig();
        Audience audience = plugin.getAdventure().sender(sender);

        if (sender.hasPermission("votingsystem.question.help")) {
            for (Component component : config.getMessageComponentListPlain("help message")) {
                audience.sendMessage(component);
            }

            return true;
        }

        audience.sendMessage(config.getMessageComponentPlain("no permission"));

        return true;
    }
}
