package net.theplonk.votingsystem.managers;

import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.objects.Question;
import net.theplonk.votingsystem.objects.VotingSystemConfig;
import net.theplonk.votingsystem.util.DiscordWebhook;
import redempt.redlib.sql.SQLHelper;

public class VoteManager {

    private static final VotingSystem plugin = VotingSystem.getInstance();

    public static boolean question(Question question) {
        SQLHelper sqlHelper = plugin.getSqlDatabase();
        sqlHelper.execute("DELETE FROM votes;");
        sqlHelper.execute("DELETE FROM vote_data;");

        sqlHelper.execute(String.format("INSERT INTO vote_data (setting, value) VALUES ('%s', '%s');", "title", question.title()));
        sqlHelper.execute(String.format("INSERT INTO vote_data (setting, value) VALUES ('%s', '%s');", "description", question.description()));
        sqlHelper.execute(String.format("INSERT INTO vote_data (setting, value) VALUES ('%s', %s);", "vote_running", true));
        return true;
    }

    public static boolean unpublish(boolean report) {
        VotingSystemConfig config = plugin.getVotingConfig();
        SQLHelper sqlHelper = plugin.getSqlDatabase();
        DiscordWebhook.EmbedObject embedObject = plugin.getEmbedObject();

        String title = plugin.getSqlDatabase().querySingleResultString("SELECT setting FROM vote_data WHERE setting='title';");
        String description = plugin.getSqlDatabase().querySingleResultString("SELECT setting FROM vote_data WHERE setting='description';");

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


            embedObject.setDescription("Title: " + title + "\\n" +
                    "Description: " + description + "\\n\\n" +
                    "Results: " + resultWord + " wins!" + "\\n" +
                    "  Yes Votes: " + resultYes + " votes\\n" +
                    "  No Votes: " + resultNo + " votes");
        } else {
            embedObject.setTitle("Vote Canceled");
            embedObject.setDescription("Title: " + title + "\\n" +
                    "Description: " + description);
        }

        plugin.executeWebhook();
        sqlHelper.execute("DELETE FROM votes;");
        sqlHelper.execute("DELETE FROM vote_data;");
        return true;
    }

    public static boolean isVoteRunning() {
        String result = plugin.getSqlDatabase().querySingleResultString("SELECT value FROM vote_data WHERE setting='vote_running';");
        if (result == null) return false;
        return result.equals("1");
    }



}
