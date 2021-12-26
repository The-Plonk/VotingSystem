package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.managers.VoteManager;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.sql.SQLHelper;

import java.util.List;

public class ActiveCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "active";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        SQLHelper sqlHelper = plugin.getSqlDatabase();
        VotingSystemConfig config = plugin.getVotingConfig();
        Audience audience = plugin.getAdventure().sender(sender);

        if (!sender.hasPermission("votingsystem.question.publish")) {
            audience.sendMessage(config.getMessageComponentPlain("no permission"));
            return true;
        }

        if (!VoteManager.isVoteRunning()) {
            audience.sendMessage(config.getMessageComponentPlain("vote not running"));
            return true;
        }

        String title = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='title';");
        String description = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='description';");
        int countYes = sqlHelper.querySingleResult("SELECT COUNT(vote) FROM votes WHERE vote = 'yes';");
        int countNo = sqlHelper.querySingleResult("SELECT COUNT(vote) FROM votes WHERE vote = 'no';");

        List<Template> templates = List.of(
                Template.of("title", title),
                Template.of("description", description),
                Template.of("yes-count", String.valueOf(countYes)),
                Template.of("no-count", String.valueOf(countNo))

        );

        for (Component component : config.getMessageComponentListPlain("active message", templates)) {
            audience.sendMessage(component);
        }

        return true;
    }
}
