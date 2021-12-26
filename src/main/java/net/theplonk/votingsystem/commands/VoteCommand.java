package net.theplonk.votingsystem.commands;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import litebans.api.Database;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VoteCommand implements CommandExecutor {

    VotingSystem plugin = VotingSystem.getInstance();
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

            if (!VoteManager.isVoteRunning()) {
                playerAudience.sendMessage(config.getMessageComponentPlain("vote not running player"));
                return true;
            }

            Sound openMenuSound = Sound.sound(Key.key("entity.bat.takeoff"), Sound.Source.NEUTRAL, 1f, 1.5f);
            Sound acceptableSound = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.BLOCK, 1f, 1.5f);
            Sound denySound = Sound.sound(Key.key("entity.villager.no"), Sound.Source.NEUTRAL, 1f, 1f);

            String title = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='title';");
            String description = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='description';");
            playerAudience.playSound(openMenuSound, Sound.Emitter.self());

            ChestGui gui = new ChestGui(4, "The Plonk SMP | Voting");
            gui.setOnGlobalClick(e -> e.setCancelled(true));
            StaticPane staticPane = new StaticPane(9, 4);

            ItemStack signInfo = new ItemStack(Material.OAK_SIGN);
            ItemMeta signInfoMeta = signInfo.getItemMeta();
            signInfoMeta.displayName(config.getMessageComponentPlain("sign title").decoration(TextDecoration.ITALIC, false));
            List<Template> templates = List.of(Template.of("title", title), Template.of("description", description));
            signInfoMeta.lore(config.getMessageComponentListPlain("sign lore", templates));
            signInfo.setItemMeta(signInfoMeta);
            staticPane.addItem(new GuiItem(signInfo), 4, 3);

            ItemStack yesVote = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta yesVoteMeta = yesVote.getItemMeta();
            yesVoteMeta.displayName(MiniMessage.get().parse("<green><bold>YES").decoration(TextDecoration.ITALIC, false));

            ItemStack noVote = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta noVoteMeta = noVote.getItemMeta();
            noVoteMeta.displayName(MiniMessage.get().parse("<red><bold>NO").decoration(TextDecoration.ITALIC, false));

            String value = plugin.getSqlDatabase().querySingleResultString(
                    String.format("SELECT vote FROM votes WHERE uuid='%s'", player.getUniqueId()));

            if (value != null) {
                if (value.equals("yes")) {
                    yesVoteMeta.displayName(MiniMessage.get().parse("<green><bold>YES <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
                    yesVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                    yesVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else if (value.equals("no")) {
                    noVoteMeta.displayName(MiniMessage.get().parse("<red><bold>NO <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
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
                            yesVoteMeta.displayName(MiniMessage.get().parse("<green><bold>YES <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
                            addRemoveEnchant(yesVote, yesVoteMeta, noVote, noVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, true, "yes");
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            playerClicked.closeInventory();
                            playerAudience.sendMessage(config.getMessageComponentPlain("updated yes"));
                            playerAudience.playSound(acceptableSound, Sound.Emitter.self());
                        } else {
                            playerAudience.playSound(denySound, Sound.Emitter.self());
                        }
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            yesVoteMeta.displayName(MiniMessage.get().parse("<green><bold>YES <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
                            yesVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                            yesVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            yesVote.setItemMeta(yesVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, false, "yes");
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            playerClicked.closeInventory();
                            playerAudience.playSound(acceptableSound, Sound.Emitter.self());
                            playerAudience.sendMessage(config.getMessageComponentPlain("set yes"));
                        }
                    }
                }
            }), 2, 1);

            noVote.setItemMeta(noVoteMeta);
            staticPane.addItem(new GuiItem(noVote, event -> {
                if (event.getWhoClicked() instanceof Player playerClicked) {
                    if (value != null) {
                        if (value.equals("yes")) {
                            noVoteMeta.displayName(MiniMessage.get().parse("<red><bold>NO <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
                            addRemoveEnchant(noVote, noVoteMeta, yesVote, yesVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, true, "no");
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            playerClicked.closeInventory();
                            playerAudience.playSound(acceptableSound, Sound.Emitter.self());
                            playerAudience.sendMessage(config.getMessageComponentPlain("updated no"));
                        } else {
                            playerAudience.playSound(denySound, Sound.Emitter.self());
                        }
                    } else {
                        if (!duplicateIPAddress(playerClicked).join() && playedMoreThanTwoHours(playerClicked)) {
                            noVoteMeta.displayName(MiniMessage.get().parse("<red><bold>NO <reset><gray>(Selected)").decoration(TextDecoration.ITALIC, false));
                            noVoteMeta.addEnchant(Enchantment.LUCK, 1, true);
                            noVoteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            noVote.setItemMeta(noVoteMeta);
                            gui.update();
                            this.updateVote(playerClicked, false, "no");
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            playerClicked.closeInventory();
                            playerAudience.playSound(acceptableSound, Sound.Emitter.self());
                            playerAudience.sendMessage(config.getMessageComponentPlain("set no"));
                        }
                    }
                }
            }), 6, 1);


            ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta fillerItemMeta = fillerItem.getItemMeta();
            fillerItemMeta.displayName(Component.text(" "));
            fillerItem.setItemMeta(fillerItemMeta);
            staticPane.fillWith(fillerItem);
            gui.addPane(staticPane);
            gui.show(player);
        } else {
            adventure.sender(sender).sendMessage(config.getMessageComponentPlain("only players"));
        }

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

        if (vote.equals("no")) {
            embedObject.setColor(Color.decode("#990c0c"));
        } else {
            embedObject.setColor(Color.decode("#5af024"));
        }

        if (changedVote) {
            plugin.getSqlDatabase().execute(String.format("UPDATE votes SET vote='%s' WHERE uuid='%s';", vote, playerClicked.getUniqueId()));
        } else {
            plugin.getSqlDatabase().execute(String.format("INSERT INTO votes (uuid, vote) VALUES ('%s', '%s');", playerClicked.getUniqueId(), vote));
        }
        plugin.executeWebhook();
    }
}
