package net.theplonk.votingsystem.commands.questions;

import net.kyori.adventure.audience.Audience;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.commands.AbstractSubCommand;
import net.theplonk.votingsystem.managers.VoteManager;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnpublishCommand extends AbstractSubCommand {

    @Override
    public String getLabel() {
        return "unpublish";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        VotingSystemConfig config = plugin.getVotingConfig();
        Audience audience = plugin.getAdventure().sender(sender);

        if (VoteManager.isVoteRunning()) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "yes":
                        if (VoteManager.unpublish(true))
                            audience.sendMessage(config.getMessageComponentPlain("success unpublish vote"));
                        break;
                    case "no":
                        if (VoteManager.unpublish(false))
                            audience.sendMessage(config.getMessageComponentPlain("success unpublish vote"));
                        break;
                    default:
                        audience.sendMessage(config.getMessageComponentPlain("invalid unpublish vote"));
                        break;
                }
            }
        } else {
            audience.sendMessage(config.getMessageComponentPlain("vote not running"));
        }

        return true;
    }
}
