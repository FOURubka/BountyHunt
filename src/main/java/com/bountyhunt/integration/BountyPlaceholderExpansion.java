package com.bountyhunt.integration;

import com.bountyhunt.BountyHunt;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BountyPlaceholderExpansion extends PlaceholderExpansion {

    private final BountyHunt plugin;

    public BountyPlaceholderExpansion(BountyHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bountyhunt";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GeminiCLI";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("bounty")) {
            return String.valueOf(plugin.getBountyManager().getBounty(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("reputation")) {
            return String.valueOf(plugin.getStorage().getReputation(player.getUniqueId()).join());
        }

        if (params.equalsIgnoreCase("rank")) {
            return plugin.getReputationManager().getRankDisplay(player.getUniqueId()).join();
        }

        return null;
    }
}
