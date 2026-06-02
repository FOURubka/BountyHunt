package com.bountyhunt.service.impl;

import com.bountyhunt.service.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultEconomyProvider implements EconomyProvider {
    private final Economy economy;

    public VaultEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    @Override
    public void withdraw(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void deposit(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public String getName() {
        return "Vault";
    }
}
