package net.theplonk.votingsystem.tasks;

import net.theplonk.votingsystem.VotingSystem;
import net.theplonk.votingsystem.managers.SettingsManager;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceVote {

    public void startTask() {
        VotingSystem plugin = VotingSystem.getInstance();
        SettingsManager config = plugin.getConfig();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Do Nothing
            }
        }.runTaskTimer(plugin, 0L, 20L * config.announceInterval);
    }

}
