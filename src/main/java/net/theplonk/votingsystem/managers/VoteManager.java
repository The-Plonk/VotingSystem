package net.theplonk.votingsystem.managers;

import lombok.Getter;
import lombok.Setter;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.objects.Question;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import net.theplonk.votingsystem.util.DiscordWebhook;
import redempt.redlib.sql.SQLHelper;

public class VoteManager {

    @Getter @Setter
    private static boolean voteRunning = false;
    private static final VotingSystem plugin = VotingSystem.getInstance();

    public static boolean question(Question question) {
        SQLHelper sqlHelper = plugin.getSqlDatabase();
        sqlHelper.execute("DELETE FROM votes;");
        sqlHelper.execute("DELETE FROM vote_data;");
        sqlHelper.close();

        plugin.getSqlSettingsCache().update(question.title(), "title");
        plugin.getSqlSettingsCache().update(question.description(), "description");
        plugin.getSqlSettingsCache().update(Boolean.toString(voteRunning), "vote_running");

        return true;
    }

    public static boolean unpublish(boolean report) {
        VotingSystemConfig config = plugin.getVotingConfig();
        SQLHelper sqlHelper = plugin.getSqlDatabase();
        DiscordWebhook.EmbedObject embedObject = plugin.getEmbedObject();

        if (report) {
            embedObject.setTitle("Vote Completed");

            String resultWord;
            int resultYes = sqlHelper.querySingleResult("SELECT COUNT(vote) FROM votes WHERE vote = true;");
            int resultNo = sqlHelper.querySingleResult("SELECT COUNT(vote) FROM votes WHERE vote = false;");
            if (resultYes > resultNo) {
                resultWord = "Yes";
            } else {
                resultWord = "No";
            }


            embedObject.setDescription("Title: " + plugin.getSqlSettingsCache().select("title") + "\n" +
                    "Description: " + plugin.getSqlSettingsCache().select("description") + "\n\n" +
                    "Results: " + resultWord + " wins!" + "\n" +
                    "  Yes Votes: " + resultYes + " votes\n" +
                    "  No Votes: " + resultNo + " votes");

            plugin.executeWebhook();
            sqlHelper.execute("DELETE FROM votes;");
            sqlHelper.execute("DELETE FROM vote_data;");
            sqlHelper.close();
        } else {
            embedObject.setTitle("Vote Canceled");
            embedObject.setDescription("Title: " + plugin.getSqlSettingsCache().select("title") + "\n" +
                    "Description: " + plugin.getSqlSettingsCache().select("description"));

            plugin.executeWebhook();
            sqlHelper.execute("DELETE FROM votes;");
            sqlHelper.execute("DELETE FROM vote_data;");
            sqlHelper.close();
        }

        return true;
    }



}
