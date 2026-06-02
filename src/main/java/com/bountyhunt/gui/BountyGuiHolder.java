package com.bountyhunt.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BountyGuiHolder implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        return null; // Holder itself doesn't need to return the inventory
    }
}
