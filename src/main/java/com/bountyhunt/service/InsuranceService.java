package com.bountyhunt.service;

import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface InsuranceService {
    CompletableFuture<Boolean> purchaseProtection(Player player);
    boolean isProtected(UUID uuid);
}
