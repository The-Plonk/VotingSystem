package net.theplonk.votingsystem.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import litebans.api.Database;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.managers.VoteManager;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import net.theplonk.votingsystem.util.DiscordWebhook;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.sql.SQLCache;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VoteCommand implements CommandExecutor {

    VotingSystem plugin = VotingSystem.getInstance();
    SQLCache cacheData = plugin.getSqlDataCache();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        BukkitAudiences adventure = plugin.getAdventure();
        VotingSystemConfig config = plugin.getVotingConfig();

        if (sender instanceof Player player) {
            Audience playerAudience = adventure.player(player);

            if (!VoteManager.isVoteRunning()) {
                playerAudience.sendMessage(config.getMessageComponentPlain("vote not running"));
                return true;
            }

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
            staticPane.addItem(new GuiItem(yesVote, event -> {
                if (event.getWhoClicked() instanceof Player playerClicked) {
                    if (cacheData.select(playerClicked.getUniqueId().toString()) != null) {
                        this.updateVote(event, yesVote, yesVoteMeta, playerClicked, gui, true, "yes");
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            this.updateVote(event, yesVote, yesVoteMeta, playerClicked, gui, false, "yes");
                        }
                    }
                }
            }), 2, 1);



            ItemStack noVote = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta noVoteMeta = noVote.getItemMeta();
            noVoteMeta.displayName(MiniMessage.get().parse("<red>NO!"));
            noVote.setItemMeta(noVoteMeta);
            staticPane.addItem(new GuiItem(noVote, event -> {

                if (event.getWhoClicked() instanceof Player playerClicked) {
                    if (cacheData.select(playerClicked.getUniqueId().toString()) != null) {
                        this.updateVote(event, yesVote, yesVoteMeta, playerClicked, gui, true, "no");
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            this.updateVote(event, yesVote, yesVoteMeta, playerClicked, gui, false, "no");
                        }
                    }
                }
            }), 6, 1);

            staticPane.fillWith(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            gui.addPane(staticPane);
            gui.show(player);
        }

        adventure.sender(sender).sendMessage(config.getMessageComponentPlain("only-players"));
        return true;
    }

    public CompletableFuture<Boolean> duplicateIPAddress(Player player) {
        String IP = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        return CompletableFuture.supplyAsync(() -> {
                Collection<UUID> playersWithIP = Database.get().getUsersByIP(IP);
                return playersWithIP.size() > 1;
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
        });
    }

    public boolean playedMoreThanTwoHours(Player player) {
        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) >= 144000;
    }

    public void updateVote(InventoryClickEvent event, ItemStack item, ItemMeta itemMeta, Player playerClicked, Gui gui, boolean changedVote, String vote) {
        DiscordWebhook.EmbedObject embedObject = plugin.getEmbedObject();
        if (changedVote) {
            embedObject.setTitle(playerClicked.getName() + " changed their vote to " + vote);
        } else {
            embedObject.setTitle(playerClicked.getName() + " voted " + vote);
        }
        embedObject.setDescription("Title: " + plugin.getSqlSettingsCache().select("title").toString() + "\\n" +
                "Description: " + plugin.getSqlSettingsCache().select("description").toString());
        cacheData.update(vote, playerClicked.getUniqueId().toString());
        item.addUnsafeEnchantment(Enchantment.LUCK, 1);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(itemMeta);
        gui.update();
        plugin.executeWebhook();
    }
}
