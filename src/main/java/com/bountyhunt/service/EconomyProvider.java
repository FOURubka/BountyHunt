package com.bountyhunt.service;

import org.bukkit.entity.Player;

public interface EconomyProvider {
    boolean has(Player player, double amount);
    void withdraw(Player player, double amount);
    void deposit(Player player, double amount);
    String getName();
}
