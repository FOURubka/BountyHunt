package com.bountyhunt.manager;

import com.bountyhunt.BountyHunt;
import org.bukkit.scheduler.BukkitRunnable;

public class EscalationManager extends BukkitRunnable {

    private final BountyHunt plugin;
    private final BountyManager bountyManager;

    public EscalationManager(BountyHunt plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("settings.escalation.enabled")) return;

        double multiplier = plugin.getConfig().getDouble("settings.escalation.multiplier");
        double max = plugin.getConfig().getDouble("settings.escalation.max-multiplier");

        bountyManager.getBountyCache().forEach((uuid, amount) -> {
            if (amount < max) {
                double newAmount = Math.min(amount * multiplier, max);
                bountyManager.setBountyDirectly(uuid, newAmount);
            }
        });
    }

    public void start() {
        long interval = plugin.getConfig().getLong("settings.escalation.interval-minutes") * 20 * 60;
        this.runTaskTimer(plugin, interval, interval);
    }
}
