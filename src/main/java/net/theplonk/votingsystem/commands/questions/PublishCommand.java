package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.Template;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.managers.VoteManager;
import net.theplonk.votingsystem.objects.Question;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
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
        VotingSystemConfig config = plugin.getVotingConfig();
        Audience audience = plugin.getAdventure().sender(sender);

        if (!sender.hasPermission("votingsystem.question.publish")) {
            audience.sendMessage(config.getMessageComponentPlain("no permission"));
            return true;
        }

        Map<String, Question> questions = config.getQuestions();

        if (VoteManager.isVoteRunning()) {
            audience.sendMessage(config.getMessageComponentPlain("vote running"));
            return true;
        }

        if (args.length > 0) {
            if (questions.containsKey(args[0])) {
                // Only move forward if setQuestion returns true meaning everything ran correctly!
                if (VoteManager.question(questions.get(args[0]))) {
                    audience.sendMessage(config.getMessageComponentPlain("success vote running"));
                };
            } else {
                audience.sendMessage(config.getMessageComponentPlain("id does not exist", List.of(Template.of("valid", questions.keySet().toString()))));
            }
        } else {
            audience.sendMessage(config.getMessageComponentPlain("provide publish arg", List.of(Template.of("valid", questions.keySet().toString()))));
        }
        return true;
    }
}
