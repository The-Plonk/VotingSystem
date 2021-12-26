package net.theplonk.votingsystem.objects;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import redempt.redlib.config.annotations.ConfigMappable;
import redempt.redlib.config.annotations.ConfigName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigMappable
public class VotingSystemConfig {

    @Getter private final Map<String, Question> questions = new HashMap<>();
    @Getter @ConfigName("discord webhook url") private final String discord_url;
    @Getter private final Map<String, Object> messages = new HashMap<>();

    public VotingSystemConfig() {
        discord_url = "https://discord.com/api/webhooks/924470586955149352/gPWGEQ17MJLdazb5eLtMS_BtD4H4qCrYHEG2BohJsxWVYJra6gN3d1Ux28Uvb3zUWrnC";
        Question presetQuestion = new Question("mcMMO", "Should we add mcMMO?", "mcMMO is a very useful plugin!");
        questions.putIfAbsent(presetQuestion.name(), presetQuestion);
    }

    public void setMessages() {
        messages.putIfAbsent("no permission", "<red>You don't have permission to run that command!");
        messages.putIfAbsent("vote running", "<yellow>A vote is already in session. Please use /question unpublish to unpubish the vote!");
        messages.putIfAbsent("only players", "<red>Only players can run this command!");
        messages.putIfAbsent("sign title", "<yellow>Vote Information");
        messages.putIfAbsent("sign lore", List.of("<yellow><bold>TITLE<reset><yellow>: <title>", "<yellow><bold>DESCRIPTION<reset><yellow>: <description>"));
        messages.putIfAbsent("help message", List.of(
                "<gold><bold>QUESTION HELP MENU",
                "",
                "<gold>/question help <dark_gray>- <yellow>Shows this help menu!",
                "<gold>/question active <dark_gray>- <yellow>Shows the active question (if there is one)",
                "<gold>/question publish <id> <dark_gray>- <yellow>Publish a question for voting",
                "<gold>/question unpublish <yes|no> <dark_gray>- <yellow>Unpublish a question. Yes value means show results, no means no results on discord.",
                "<gold>/question reload <dark_gray>- <yellow>Reloads the config"
        ));
        messages.putIfAbsent("successfully reloaded", "<yellow>Successfully reloaded the configuration!");
        messages.putIfAbsent("success vote running", "<green>The vote is now running!");
        messages.putIfAbsent("success unpublish vote", "<green>You successfully unpublished the current question! <gray>(Output on discord)");
        messages.putIfAbsent("invalid unpublish vote", "<red>You did not give a valid option. Please type in yes or no as the argument for getting results.");
        messages.putIfAbsent("vote not running", "<red>No vote is currently running! Use /question publish to publish a vote!");
        messages.putIfAbsent("vote not running player", "<red>No vote is currently running!");
        messages.putIfAbsent("updated yes", "<green>You successfully updated your vote to YES!");
        messages.putIfAbsent("set yes", "<green>You successfully set your vote to YES!");
        messages.putIfAbsent("updated no", "<green>You successfully updated your vote to NO!");
        messages.putIfAbsent("set no", "<green>You successfully set your vote to NO!");
        messages.putIfAbsent("id does not exist", "<red>You did not provide a valid ID. IDs: <valid>");
        messages.putIfAbsent("provide publish arg", "<red>You didn't include an ID. IDs: <valid>");
        messages.putIfAbsent("both conditions not met", "<red>Your IP already voted and you don't have 2 hours of playtime!");
        messages.putIfAbsent("ip already voted", "<red>Your IP address has already voted!");
        messages.putIfAbsent("not enough playtime", "<red>You don't meet the playtime requirement of 2 hours!");
        messages.putIfAbsent("secret requirements not met", "<red>You don't meet the requirements to vote!");
        messages.putIfAbsent("active message", List.of(
                "",
                "<gold>Title: <yellow><title>",
                "<gold>Description: <yellow><description>",
                "<gold>Yes Count: <yellow><yes-count>",
                "<gold>No Count: <yellow><no-count>",
                ""
        ));
    }

    public Component getMessageComponentPlain(String key, List<Template> placeholders) {
        if (messages.containsKey(key)) {
            if (placeholders == null) {
                return MiniMessage.get().parse((String) messages.get(key));
            } else {
                return MiniMessage.get().parse((String) messages.get(key), placeholders);
            }
        }

        return null;
    }

    public Component getMessageComponentPlain(String key) {
        return this.getMessageComponentPlain(key, null);
    }

    public List<Component> getMessageComponentListPlain(String key) {
        return this.getMessageComponentListPlain(key, null);
    }

    public List<Component> getMessageComponentListPlain(String key, List<Template> placeholders) {
        if (messages.containsKey(key)) {
            List<Component> lines = new ArrayList<>();
            for (Object line : (List<?>) messages.get(key)) {
                if (line instanceof String) {
                    if (placeholders == null) {
                        lines.add(MiniMessage.get().parse((String) line).decoration(TextDecoration.ITALIC, false));
                    } else {
                        lines.add(MiniMessage.get().parse((String) line, placeholders).decoration(TextDecoration.ITALIC, false));
                    }
                }
            }

            return lines;
        }

        return null;
    }


}
