package com.bountyhunt.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ReputationService {
    CompletableFuture<String> getRankDisplay(UUID uuid);
    CompletableFuture<Integer> getScore(UUID uuid);
    void addReputation(UUID uuid, int amount);
}
