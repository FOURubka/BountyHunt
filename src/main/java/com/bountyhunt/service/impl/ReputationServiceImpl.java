package com.bountyhunt.service.impl;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.manager.ReputationManager;
import com.bountyhunt.service.ReputationService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReputationServiceImpl implements ReputationService {

    private final BountyHunt plugin;
    private final ReputationManager reputationManager;

    public ReputationServiceImpl(BountyHunt plugin, ReputationManager reputationManager) {
        this.plugin = plugin;
        this.reputationManager = reputationManager;
    }

    @Override
    public CompletableFuture<String> getRankDisplay(UUID uuid) {
        return reputationManager.getRankDisplay(uuid);
    }

    @Override
    public CompletableFuture<Integer> getScore(UUID uuid) {
        return reputationManager.getScore(uuid);
    }

    @Override
    public void addReputation(UUID uuid, int amount) {
        plugin.getStorage().addReputation(uuid, amount);
    }
}
