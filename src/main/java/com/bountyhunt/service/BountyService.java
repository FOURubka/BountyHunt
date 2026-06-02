package com.bountyhunt.service;

import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BountyService {
    CompletableFuture<Boolean> placeBounty(Player issuer, Player target, double amount);
    void claimBounty(Player hunter, Player target);
    double getActiveBounty(UUID uuid);
    void loadBountiesToCache();
    java.util.Map<UUID, Double> getBountyCache();
}
