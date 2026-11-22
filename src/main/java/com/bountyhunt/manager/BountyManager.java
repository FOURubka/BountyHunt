package com.bountyhunt.manager;

import lombok.Getter;
import com.bountyhunt.BountyHunt;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            }
    }

    public void setBountyDirectly(UUID uuid, double amount) {
        if (amount > 0) {
            bountyCache.put(uuid, amount);
        } else {
            bountyCache.remove(uuid);
        }
        plugin.getStorage().setBounty(uuid, amount);
    }

    // Main thread only: Vault economies aren't thread-safe
    public CompletableFuture<Boolean> setBounty(Player issuer, Player target, double amount) {
        double min = plugin.getConfig().getDouble("settings.bounty.min-amount");
        double max = plugin.getConfig().getDouble("settings.bounty.max-amount", Double.MAX_VALUE);
        if (!Double.isFinite(amount)) return CompletableFuture.completedFuture(false);

        // Store exactly what the economy can withdraw, or the claim pays out more than was paid in
        int digits = plugin.getEconomyProvider().getFractionalDigits();
        amount = BigDecimal.valueOf(amount).setScale(digits < 0 ? 2 : Math.min(digits, 2), RoundingMode.DOWN).doubleValue();
        boolean selfBounty = issuer.getUniqueId().equals(target.getUniqueId());

        if (amount <= 0 || amount < min || amount > max
                || (selfBounty && !plugin.getConfig().getBoolean("settings.bounty.allow-self-bounty"))
                || !plugin.getEconomyProvider().has(issuer, amount)
                || !plugin.getEconomyProvider().withdraw(issuer, amount)) {
            return CompletableFuture.completedFuture(false);
        }

        setBountyDirectly(target.getUniqueId(), getBounty(target.getUniqueId()) + amount);
        return CompletableFuture.completedFuture(true);
    }

    public void claimBounty(Player hunter, Player target) {
        Double amount = bountyCache.remove(target.getUniqueId());
        if (amount == null || amount <= 0) return;

        plugin.getStorage().setBounty(target.getUniqueId(), 0);
        plugin.getEconomyProvider().deposit(hunter, amount);
        plugin.getStorage().addReputation(hunter.getUniqueId(), 1);
    }

    public double getBounty(UUID uuid) {
        return bountyCache.getOrDefault(uuid, 0.0);
    }
}
