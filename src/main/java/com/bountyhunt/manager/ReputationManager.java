package com.bountyhunt.manager;

import com.bountyhunt.BountyHunt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReputationManager {

    private final BountyHunt plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ReputationManager(BountyHunt plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<String> getRankDisplay(UUID uuid) {
        return plugin.getStorage().getReputation(uuid).thenApply(score -> {
            ConfigurationSection ranks = plugin.getConfig().getConfigurationSection("settings.reputation.ranks");
            if (ranks == null) return "Unknown";

            String bestRank = "ROOKIE";
            int maxThreshold = -1;

            for (String rankKey : ranks.getKeys(false)) {
                int threshold = ranks.getInt(rankKey + ".threshold");
                if (score >= threshold && threshold > maxThreshold) {
                    maxThreshold = threshold;
                    bestRank = rankKey;
                }
            }

            return ranks.getString(bestRank + ".display", "Unknown");
        });
    }

    public CompletableFuture<Integer> getScore(UUID uuid) {
        return plugin.getStorage().getReputation(uuid);
    }
}
