package com.bountyhunt.service.impl;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.api.event.BountyClaimEvent;
import com.bountyhunt.api.event.BountyPlaceEvent;
import com.bountyhunt.manager.BountyManager;
import com.bountyhunt.service.BountyService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BountyServiceImpl implements BountyService {

    private final BountyHunt plugin;
    private final BountyManager bountyManager;

    public BountyServiceImpl(BountyHunt plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
    }

    @Override
    public CompletableFuture<Boolean> placeBounty(Player issuer, Player target, double amount) {
        BountyPlaceEvent event = new BountyPlaceEvent(issuer, target, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompletableFuture.completedFuture(false);
        }

        return bountyManager.setBounty(issuer, target, event.getAmount());
    }

    @Override
    public void claimBounty(Player hunter, Player target) {
        double amount = bountyManager.getBounty(target.getUniqueId());
        if (amount <= 0) return;

        BountyClaimEvent event = new BountyClaimEvent(hunter, target, amount);
        Bukkit.getPluginManager().callEvent(event);

        bountyManager.claimBounty(hunter, target);
        plugin.getStorage().logKill(hunter.getUniqueId(), target.getUniqueId(), amount);
    }

    @Override
    public double getActiveBounty(UUID uuid) {
        return bountyManager.getBounty(uuid);
    }

    @Override
    public void loadBountiesToCache() {
        bountyManager.loadBounties();
    }

    @Override
    public java.util.Map<UUID, Double> getBountyCache() {
        return bountyManager.getBountyCache();
    }
}
