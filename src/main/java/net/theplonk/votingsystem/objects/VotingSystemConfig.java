package net.theplonk.votingsystem.objects;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import redempt.redlib.config.annotations.ConfigMappable;

import java.util.*;

@ConfigMappable
public class VotingSystemConfig {

    @Getter private final Map<String, Question> questions;
    @Getter private final String discord_token;
    private final Map<String, Object> messages;

    public VotingSystemConfig() {
        questions = new HashMap<>();
        discord_token = "000000000000000000000000000000";
        Question presetQuestion = new Question("mcMMO", "Should we add mcMMO?", "mcMMO is a very useful plugin!");
        questions.put(presetQuestion.name(), presetQuestion);
        messages = new HashMap<>();

        this.setMessages();
    }

    public void setMessages() {
        messages.put("no-permission", "<red>You don't have permission to run that command!");
        messages.put("vote-running", "<yellow>A vote is already in session. Please use /question unpublish to unpubish the vote!");
        messages.put("only-players", "<red>Only players can run this command!");
        messages.put("sign-lore", new ArrayList<>(Arrays.asList("hi", "bye", "ok")));
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
            System.out.println("test1");
            for (Object line : (List<?>) messages.get(key)) {
                if (line instanceof String) {
                    lines.add(MiniMessage.get().parse((String) line));
                }
            }
        }

        return null;
    }


}
