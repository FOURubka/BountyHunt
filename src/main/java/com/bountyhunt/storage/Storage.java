package com.bountyhunt.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {
    void init() throws Exception;
    void close();

    // Bounty operations
    CompletableFuture<Double> getBounty(UUID uuid);
    CompletableFuture<Void> setBounty(UUID uuid, double amount);
    
    // Reputation operations
    CompletableFuture<Integer> getReputation(UUID uuid);
    CompletableFuture<Void> addReputation(UUID uuid, int amount);

    // History operations
    CompletableFuture<Void> logKill(UUID hunter, UUID target, double amount);
}
