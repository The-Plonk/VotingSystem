package net.theplonk.votingsystem.commands;

import net.kyori.adventure.Adventure;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.theplonk.votingsystem.VotingSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        BukkitAudiences adventure = VotingSystem.getInstance().getAdventure();

        if (sender instanceof Player player) {
            Audience playerAudience = adventure.player(player);
            // OPEN GUI
        }

        adventure.sender(sender).sendMessage(
                MiniMessage.get().parse("<red>This command can only be ran by players.")
        );
        return true;
    }
}
