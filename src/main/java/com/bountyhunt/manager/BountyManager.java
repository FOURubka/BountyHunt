package com.bountyhunt.manager;

import lombok.Getter;
import com.bountyhunt.BountyHunt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BountyManager {

    private final BountyHunt plugin;
    @Getter
    private final Map<UUID, Double> bountyCache = new ConcurrentHashMap<>();

    public BountyManager(BountyHunt plugin) {
        this.plugin = plugin;
    }

    public void loadBounties() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Attempting to load bounties from database...");
            try (java.sql.Connection conn = ((com.bountyhunt.storage.impl.SQLiteStorage) plugin.getStorage()).getDataSource().getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT uuid, amount FROM bh_bounties WHERE amount > 0");
                 java.sql.ResultSet rs = stmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    bountyCache.put(UUID.fromString(rs.getString("uuid")), rs.getDouble("amount"));
                    count++;
                }
                plugin.getLogger().info("Successfully loaded " + count + " active bounties into cache.");
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Error loading bounties: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void setBountyDirectly(UUID uuid, double amount) {
        plugin.getStorage().setBounty(uuid, amount).thenRun(() -> {
            bountyCache.put(uuid, amount);
        });
    }

    public CompletableFuture<Boolean> setBounty(Player issuer, Player target, double amount) {
        if (amount < plugin.getConfig().getDouble("settings.bounty.min-amount")) {
            return CompletableFuture.completedFuture(false);
        }

        if (!plugin.getEconomyProvider().has(issuer, amount)) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            plugin.getEconomyProvider().withdraw(issuer, amount);
            
            double currentBounty = bountyCache.getOrDefault(target.getUniqueId(), 0.0);
            double newTotal = currentBounty + amount;
            
            plugin.getStorage().setBounty(target.getUniqueId(), newTotal).join();
            bountyCache.put(target.getUniqueId(), newTotal);
            
            return true;
        });
    }

    public void claimBounty(Player hunter, Player target) {
        Double amount = bountyCache.remove(target.getUniqueId());
        if (amount == null || amount <= 0) return;

        plugin.getStorage().setBounty(target.getUniqueId(), 0).thenRun(() -> {
            plugin.getEconomyProvider().deposit(hunter, amount);
            plugin.getStorage().addReputation(hunter.getUniqueId(), 1).join();
            
            // Broadcast and notifications would go here
        });
    }
    
    public double getBounty(UUID uuid) {
        return bountyCache.getOrDefault(uuid, 0.0);
    }
}
