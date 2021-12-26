package net.theplonk.votingsystem.objects;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redempt.redlib.config.annotations.ConfigMappable;

import java.util.*;

@ConfigMappable
public class VotingSystemConfig {

    @Getter private final Map<String, Question> questions = new HashMap<>();
    @Getter private final String discord_token;
    private final Map<String, Object> messages = new HashMap<>();

    public VotingSystemConfig() {
        discord_token = "000000000000000000000000000000";
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
