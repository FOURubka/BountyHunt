package com.bountyhunt.listener;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.gui.BountyGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GuiClickListener implements Listener {

    private final BountyHunt plugin;

    public GuiClickListener(BountyHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BountyGuiHolder)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();

        // Check for menu actions
        if (clicked.getItemMeta().getPersistentDataContainer().has(plugin.getGuiManager().getMenuActionKey(), PersistentDataType.STRING)) {
            String action = clicked.getItemMeta().getPersistentDataContainer().get(plugin.getGuiManager().getMenuActionKey(), PersistentDataType.STRING);
            if ("open_list".equals(action)) {
                plugin.getGuiManager().openBountyList(player);
            } else if ("open_top".equals(action)) {
                player.sendMessage("Top hunters coming soon...");
            }
        }
        
        // Check for bounty target tracking
        else if (clicked.getItemMeta().getPersistentDataContainer().has(plugin.getGuiManager().getTargetKey(), PersistentDataType.STRING)) {
            String uuidStr = clicked.getItemMeta().getPersistentDataContainer().get(plugin.getGuiManager().getTargetKey(), PersistentDataType.STRING);
            UUID targetUuid = UUID.fromString(uuidStr);
            
            player.closeInventory();
            player.sendMessage("Tracking " + Bukkit.getOfflinePlayer(targetUuid).getName() + "...");
        }
    }
}
