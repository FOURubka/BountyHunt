package com.bountyhunt.service.impl;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.service.InsuranceService;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InsuranceServiceImpl implements InsuranceService {

    private final BountyHunt plugin;
    private final Map<UUID, Long> protectedPlayers = new ConcurrentHashMap<>();

    public InsuranceServiceImpl(BountyHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Boolean> purchaseProtection(Player player) {
        double cost = plugin.getConfig().getDouble("settings.insurance.cost", 500.0);
        if (!plugin.getEconomyProvider().has(player, cost)) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            plugin.getEconomyProvider().withdraw(player, cost);
            long duration = plugin.getConfig().getLong("settings.insurance.duration-minutes", 30) * 60 * 1000L;
            protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis() + duration);
            return true;
        });
    }

    @Override
    public boolean isProtected(UUID uuid) {
        if (!protectedPlayers.containsKey(uuid)) return false;
        if (System.currentTimeMillis() > protectedPlayers.get(uuid)) {
            protectedPlayers.remove(uuid);
            return false;
        }
        return true;
    }
}
