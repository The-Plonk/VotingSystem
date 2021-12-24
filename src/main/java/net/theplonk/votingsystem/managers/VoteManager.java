package net.theplonk.votingsystem.managers;

import lombok.Getter;
import lombok.Setter;
import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.objects.Question;
import redempt.redlib.sql.SQLHelper;

public class VoteManager {

    @Getter @Setter
    private static boolean voteRunning = false;
    private static final VotingSystem plugin = VotingSystem.getInstance();

    public static void setQuestion(Question question) {
        if (!voteRunning) {
            SQLHelper sqlHelper = plugin.getSqlDatabase();
            sqlHelper.execute("DELETE FROM votes;");
            sqlHelper.execute("DELETE FROM vote_data;");
            sqlHelper.close();

            voteRunning = true;

            plugin.getSqlSettingsCache().update(question.title(), "title");
            plugin.getSqlSettingsCache().update(question.description(), "description");
            plugin.getSqlSettingsCache().update(Boolean.toString(voteRunning), "vote_running");
        }
    }


}
