package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.managers.SettingsManager;
import net.theplonk.votingsystem.managers.VoteManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PublishCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "publish";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        SettingsManager config = plugin.getConfig();
        Audience audience = plugin.getAdventure().sender(sender);

        List<Map<?, ?>> questions = config.questions;
        plugin.getLogger().info(questions.toString());

        if (VoteManager.voteRunning) {
            audience.sendMessage(MiniMessage.get().parse("<red>Vote already running!"));
            return true;
        }

        return true;
    }
}
