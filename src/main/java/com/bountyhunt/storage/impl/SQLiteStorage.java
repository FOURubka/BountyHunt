package com.bountyhunt.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.bountyhunt.BountyHunt;
import com.bountyhunt.storage.Storage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteStorage implements Storage {

    private final BountyHunt plugin;
    private HikariDataSource dataSource;

    public SQLiteStorage(BountyHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() throws Exception {
        HikariConfig config = new HikariConfig();
        File dbFile = new File(plugin.getDataFolder(), "database.db");
        
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("BountyHuntPool");
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection()) {
            String createBountiesTable = "CREATE TABLE IF NOT EXISTS bh_bounties (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "amount DOUBLE DEFAULT 0" +
                    ");";
            String createReputationTable = "CREATE TABLE IF NOT EXISTS bh_reputation (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "score INTEGER DEFAULT 0" +
                    ");";
            String createHistoryTable = "CREATE TABLE IF NOT EXISTS bh_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "hunter_uuid VARCHAR(36)," +
                    "target_uuid VARCHAR(36)," +
                    "amount DOUBLE," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            
            try (PreparedStatement stmt = conn.prepareStatement(createBountiesTable)) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(createReputationTable)) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(createHistoryTable)) {
                stmt.execute();
            }
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public CompletableFuture<Void> logKill(UUID hunter, UUID target, double amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO bh_history (hunter_uuid, target_uuid, amount) VALUES (?, ?, ?)")) {
                stmt.setString(1, hunter.toString());
                stmt.setString(2, target.toString());
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error logging kill history: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Double> getBounty(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT amount FROM bh_bounties WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("amount");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting bounty: " + e.getMessage());
            }
            return 0.0;
        });
    }

    @Override
    public CompletableFuture<Void> setBounty(UUID uuid, double amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO bh_bounties (uuid, amount) VALUES (?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setDouble(2, amount);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error setting bounty: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getReputation(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT score FROM bh_reputation WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("score");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting reputation: " + e.getMessage());
            }
            return 0;
        });
    }

    @Override
    public CompletableFuture<Void> addReputation(UUID uuid, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO bh_reputation (uuid, score) VALUES (?, ?) " +
                         "ON CONFLICT(uuid) DO UPDATE SET score = score + ?")) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, amount);
                stmt.setInt(3, amount);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error adding reputation: " + e.getMessage());
            }
        });
    }
}
