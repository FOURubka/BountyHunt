package com.bountyhunt.listener;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.service.BountyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyListener implements Listener {

    private final BountyHunt plugin;
    private final BountyService bountyService;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BountyListener(BountyHunt plugin, BountyService bountyService) {
        this.plugin = plugin;
        this.bountyService = bountyService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) return;

        double bounty = bountyService.getActiveBounty(victim.getUniqueId());
        if (bounty > 0) {
            bountyService.claimBounty(killer, victim);
            
            String prefix = plugin.getConfigManager().getMessages().getString("general.prefix");
            String claimMsg = plugin.getConfigManager().getMessages().getString("bounty.broadcast-claimed");
            
            Component broadcast = mm.deserialize(prefix + claimMsg
                    .replace("%hunter%", killer.getName())
                    .replace("%target%", victim.getName())
                    .replace("%amount%", String.valueOf(bounty)));
            
            Bukkit.broadcast(broadcast);
        }
    }
}
