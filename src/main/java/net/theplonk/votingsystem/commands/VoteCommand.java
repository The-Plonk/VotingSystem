package net.theplonk.votingsystem.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.sql.SQLCache;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VoteCommand implements CommandExecutor {

    VotingSystem plugin = VotingSystem.getInstance();
    SQLCache cacheData = plugin.getSqlDataCache();
    int coolDownTime = 60;
    Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        BukkitAudiences adventure = plugin.getAdventure();
        VotingSystemConfig config = plugin.getVotingConfig();

        if (sender instanceof Player player) {
            Audience playerAudience = adventure.player(player);

            if (cooldowns.containsKey(player.getUniqueId())) {
                long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + coolDownTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    playerAudience.sendMessage(MiniMessage.get().parse(
                            "<red>There is a vote cooldown of 60 seconds. You have " + secondsLeft +
                                    " seconds left before you can run the command again!"));
                    return true;
                }
            }

            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

            if (!VoteManager.isVoteRunning()) {
                playerAudience.sendMessage(config.getMessageComponentPlain("vote not running player"));
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

            ItemStack noVote = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta noVoteMeta = noVote.getItemMeta();
            noVoteMeta.displayName(MiniMessage.get().parse("<red>NO!"));

            String value = plugin.getSqlDatabase().querySingleResultString(
                    String.format("SELECT vote FROM votes WHERE uuid='%s'", player.getUniqueId()));

            if (value != null) {
                if (value.equals("yes")) {
                    yesVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                    yesVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else if (value.equals("no")) {
                    noVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                    noVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            yesVote.setItemMeta(yesVoteMeta);
            noVote.setItemMeta(noVoteMeta);

            staticPane.addItem(new GuiItem(yesVote, event -> {
                if (event.getWhoClicked() instanceof Player playerClicked) {
                    if (value != null) {
                        if (value.equals("no")) {
                            addRemoveEnchant(yesVote, yesVoteMeta, noVote, noVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, true, "yes");
                            playerClicked.closeInventory();
                        }
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            yesVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                            yesVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            yesVote.setItemMeta(yesVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, false, "yes");
                            playerClicked.closeInventory();
                        }
                    }
                }
            }), 2, 1);


            noVoteMeta.displayName(MiniMessage.get().parse("<red>NO!"));
            noVote.setItemMeta(noVoteMeta);
            staticPane.addItem(new GuiItem(noVote, event -> {
                if (event.getWhoClicked() instanceof Player playerClicked) {
                    if (value != null) {
                        if (value.equals("yes")) {
                            addRemoveEnchant(noVote, noVoteMeta, yesVote, yesVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, true, "no");
                            playerClicked.closeInventory();
                        }
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            noVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                            noVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            noVote.setItemMeta(noVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, false, "no");
                            playerClicked.closeInventory();
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

    public void addRemoveEnchant(ItemStack itemAdd, ItemMeta itemMetaAdd, ItemStack itemRemove, ItemMeta itemMetaRemove) {
        itemMetaRemove.removeEnchant(Enchantment.LUCK);
        itemMetaRemove.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemRemove.setItemMeta(itemMetaRemove);
        itemMetaAdd.addEnchant(Enchantment.LUCK, 1, true);
        itemMetaAdd.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemAdd.setItemMeta(itemMetaAdd);
    }

    public void updateVote(Player playerClicked, boolean changedVote, String vote) {
        DiscordWebhook.EmbedObject embedObject = plugin.getEmbedObject();
        if (changedVote) {
            embedObject.setTitle(playerClicked.getName() + " changed their vote to " + vote);
        } else {
            embedObject.setTitle(playerClicked.getName() + " voted " + vote);
        }

        String title = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='title';");
        String description = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='description';");

        embedObject.setDescription("Title: " + title + "\\n" +
                "Description: " + description);

        if (changedVote) {
            plugin.getSqlDatabase().execute(String.format("UPDATE votes SET vote='%s' WHERE uuid='%s';", vote, playerClicked.getUniqueId()));
        } else {
            plugin.getSqlDatabase().execute(String.format("INSERT INTO votes (uuid, vote) VALUES ('%s', '%s');", playerClicked.getUniqueId(), vote));
        }
        plugin.executeWebhook();
    }
}
