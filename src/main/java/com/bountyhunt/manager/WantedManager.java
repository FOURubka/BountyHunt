package com.bountyhunt.manager;

import com.bountyhunt.BountyHunt;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WantedManager {

    private final BountyHunt plugin;
    private final BountyManager bountyManager;
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WantedManager(BountyHunt plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
        startUpdateTask();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("settings.wanted-status.enabled")) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    double bounty = bountyManager.getBounty(player.getUniqueId());
                    
                    if (bounty > 0) {
                        updateBossBar(player, bounty);
                        showParticles(player);
                    } else {
                        removeBossBar(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Every second
    }

    private void updateBossBar(Player player, double bounty) {
        if (!plugin.getConfig().getBoolean("settings.wanted-status.bossbar.enabled")) return;

        String text = plugin.getConfig().getString("settings.wanted-status.bossbar.text");
        String colorStr = plugin.getConfig().getString("settings.wanted-status.bossbar.color", "RED");
        String styleStr = plugin.getConfig().getString("settings.wanted-status.bossbar.style", "SOLID");

        Component title = mm.deserialize(text,
                Placeholder.parsed("player", player.getName()),
                Placeholder.parsed("amount", String.valueOf(bounty))
        );

        BossBar bossBar = activeBossBars.computeIfAbsent(player.getUniqueId(), k -> {
            BossBar bar = BossBar.bossBar(title, 1.0f, BossBar.Color.valueOf(colorStr), BossBar.Overlay.valueOf(styleStr));
            return bar;
        });

        bossBar.name(title);
        
        // Add to players in range
        double range = plugin.getConfig().getDouble("settings.wanted-status.visibility-range");
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getWorld().equals(player.getWorld()) && online.getLocation().distanceSquared(player.getLocation()) <= range * range) {
                online.showBossBar(bossBar);
            } else {
                online.hideBossBar(bossBar);
            }
        }
    }

    private void showParticles(Player player) {
        if (!plugin.getConfig().getBoolean("settings.wanted-status.particles.enabled")) return;
        
        double time = System.currentTimeMillis() / 150.0;
        // Spiral effect: Red dust (Heat) + Flame (Danger) + Portal (Mystery)
        for (int i = 0; i < 4; i++) {
            double angle = time + i * (Math.PI / 2);
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;
            
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(x, 2.5, z), 0, 0, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.RED, 1.2f));
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(x, 0.2, z), 1, 0, 0, 0, 0.02);
            if (i == 0) player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1.2, 0), 10, 0.3, 0.3, 0.3, 0.1);
        }
    }

    private void removeBossBar(Player player) {
        BossBar bar = activeBossBars.remove(player.getUniqueId());
        if (bar != null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.hideBossBar(bar);
            }
        }
    }
}
