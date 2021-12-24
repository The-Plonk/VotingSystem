package net.theplonk.votingsystem.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        VotingSystem plugin = VotingSystem.getInstance();
        BukkitAudiences adventure = plugin.getAdventure();
        VotingSystemConfig config = plugin.getVotingConfig();

        if (sender instanceof Player player) {
            Audience playerAudience = adventure.player(player);
            Sound openMenuSound = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.BLOCK, 1f, 1.5f);
            playerAudience.playSound(openMenuSound, Sound.Emitter.self());

            ChestGui gui = new ChestGui(4, "The Plonk SMP | Voting");
            gui.setOnGlobalClick(e -> e.setCancelled(true));
            StaticPane staticPane = new StaticPane(9, 4);

            ItemStack signInfo = new ItemStack(Material.OAK_SIGN);
            ItemMeta signInfoMeta = signInfo.getItemMeta();
            signInfoMeta.displayName(MiniMessage.get().parse("<reset><red><bold>Test"));
            signInfo.setItemMeta(signInfoMeta);
            staticPane.addItem(new GuiItem(signInfo), 4, 3);

            ItemStack yesVote = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta yesVoteMeta = yesVote.getItemMeta();
            yesVoteMeta.displayName(MiniMessage.get().parse("<green>YES!"));
            yesVote.setItemMeta(yesVoteMeta);
            staticPane.addItem(new GuiItem(yesVote), 2, 1);

            ItemStack noVote = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta noVoteMeta = noVote.getItemMeta();
            noVoteMeta.displayName(MiniMessage.get().parse("<red>NO!"));
            noVote.setItemMeta(noVoteMeta);
            staticPane.addItem(new GuiItem(noVote), 6, 1);

            staticPane.fillWith(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            gui.addPane(staticPane);
            gui.show(player);
        }

        adventure.sender(sender).sendMessage(config.getMessageComponentPlain("only-players"));
        return true;
    }
}
