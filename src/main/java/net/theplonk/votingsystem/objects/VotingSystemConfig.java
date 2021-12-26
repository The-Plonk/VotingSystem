package net.theplonk.votingsystem.objects;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redempt.redlib.config.annotations.ConfigMappable;
import redempt.redlib.config.annotations.ConfigName;

import java.util.*;

@ConfigMappable
public class VotingSystemConfig {

    @Getter private final Map<String, Question> questions = new HashMap<>();
    @Getter @ConfigName("discord webhook url") private final String discord_url;
    private final Map<String, Object> messages = new HashMap<>();

    public VotingSystemConfig() {
        discord_url = "https://discord.com/api/webhooks/924470586955149352/gPWGEQ17MJLdazb5eLtMS_BtD4H4qCrYHEG2BohJsxWVYJra6gN3d1Ux28Uvb3zUWrnC";
        Question presetQuestion = new Question("mcMMO", "Should we add mcMMO?", "mcMMO is a very useful plugin!");
        questions.put(presetQuestion.name(), presetQuestion);
        this.setMessages();
    }

    public void setMessages() {
        messages.put("no permission", "<red>You don't have permission to run that command!");
        messages.put("vote running", "<yellow>A vote is already in session. Please use /question unpublish to unpubish the vote!");
        messages.put("only players", "<red>Only players can run this command!");
        messages.put("sign lore", new ArrayList<>(Arrays.asList("hi", "bye", "ok")));
        messages.put("help message", new ArrayList<>(Arrays.asList("Help Menu", "yes")));
        messages.put("successfully reloaded", "<yellow>Successfully reloaded the configuration!");
        messages.put("success vote running", "<green>The vote is now running!");
        messages.put("success unpublish vote", "<green>You successfully unpublished the current question!");
        messages.put("invalid unpublish vote", "<red>You did not give a valid option. Please type in yes or no as the argument for getting results.");
        messages.put("vote not running", "<red>No vote is currently running! Use /question publish to publish a vote!");
        messages.put("vote not running player", "<red>No vote is currently running!");
        messages.put("updated yes", "<green>You successfully updated your vote to YES!");
        messages.put("set yes", "<green>You successfully set your vote to YES!");
        messages.put("updated no", "<green>You successfully updated your vote to NO!");
        messages.put("set no", "<green>You successfully set your vote to NO!");
    }

    public Component getMessageComponentPlain(String key) {
        if (messages.containsKey(key)) {
            return MiniMessage.get().parse((String) messages.get(key));
        }

        return null;
    }

    public List<Component> getMessageComponentListPlain(String key) {
        if (messages.containsKey(key)) {
            List<Component> lines = new ArrayList<>();
            for (Object line : (List<?>) messages.get(key)) {
                if (line instanceof String) {
                    lines.add(MiniMessage.get().parse((String) line));
                }
            }

            return lines;
        }

        return null;
    }


}
