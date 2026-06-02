package com.bountyhunt.gui;

import com.bountyhunt.BountyHunt;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;

public class GuiManager {

    private final BountyHunt plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final NamespacedKey targetKey;
    private final NamespacedKey menuActionKey;

    public GuiManager(BountyHunt plugin) {
        this.plugin = plugin;
        this.targetKey = new NamespacedKey(plugin, "target_uuid");
        this.menuActionKey = new NamespacedKey(plugin, "menu_action");
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(new BountyGuiHolder(), 27, mm.deserialize("<gradient:#ffaa00:#ff5500><bold>BountyHunt</bold></gradient>"));
        
        ItemStack activeBounties = new ItemStack(Material.RED_BANNER);
        ItemMeta meta = activeBounties.getItemMeta();
        meta.displayName(mm.deserialize("<red><bold>Active Bounties"));
        meta.lore(java.util.List.of(mm.deserialize("<gray>Click to view all current bounties.")));
        meta.getPersistentDataContainer().set(menuActionKey, PersistentDataType.STRING, "open_list");
        activeBounties.setItemMeta(meta);
        
        gui.setItem(11, activeBounties);
        
        ItemStack topHunters = new ItemStack(Material.DIAMOND_SWORD);
        meta = topHunters.getItemMeta();
        meta.displayName(mm.deserialize("<gold><bold>Top Hunters"));
        meta.lore(java.util.List.of(mm.deserialize("<gray>View the top bounty hunters.")));
        meta.getPersistentDataContainer().set(menuActionKey, PersistentDataType.STRING, "open_top");
        topHunters.setItemMeta(meta);
        
        gui.setItem(15, topHunters);

        player.openInventory(gui);
    }
    
    public NamespacedKey getMenuActionKey() {
        return menuActionKey;
    }

    public void openBountyList(Player player) {
        Inventory gui = Bukkit.createInventory(new BountyGuiHolder(), 54, mm.deserialize("<gradient:#ffaa00:#ff5500><bold>Active Bounties</bold></gradient>"));

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(net.kyori.adventure.text.Component.empty());
        border.setItemMeta(borderMeta);
        
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = 45; i < 54; i++) gui.setItem(i, border);

        Map<UUID, Double> bounties = plugin.getBountyManager().getBountyCache();
        
        int slot = 9;
        for (Map.Entry<UUID, Double> entry : bounties.entrySet()) {
            if (slot >= 45) break;
            
            final UUID targetUuid = entry.getKey();
            final double amount = entry.getValue();
            final String name = Bukkit.getOfflinePlayer(targetUuid).getName() == null ? "Unknown" : Bukkit.getOfflinePlayer(targetUuid).getName();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(targetUuid));
            meta.displayName(mm.deserialize("<red><bold>" + name));
            meta.lore(java.util.List.of(
                    mm.deserialize("<gray>Reward: <gold>$" + amount),
                    mm.deserialize(""),
                    mm.deserialize("<yellow>Click to track this player!")
            ));
            
            meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, targetUuid.toString());
            head.setItemMeta(meta);

            gui.setItem(slot++, head);
        }

        player.openInventory(gui);
    }
    
    public NamespacedKey getTargetKey() {
        return targetKey;
    }
}
