package com.bountyhunt.manager;

import com.bountyhunt.BountyHunt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class BountyTracker {

    private BountyTracker() {
    }

    /** Points the compass at the target's current position and reports the distance. */
    public static void track(BountyHunt plugin, Player hunter, Player target) {
        if (plugin.getBountyManager().getBounty(target.getUniqueId()) <= 0) {
            hunter.sendMessage(Component.text(target.getName() + " has no bounty.", NamedTextColor.RED));
            return;
        }
        if (!hunter.getWorld().equals(target.getWorld())) {
            hunter.sendMessage(Component.text(target.getName() + " is in another world.", NamedTextColor.RED));
            return;
        }
        hunter.setCompassTarget(target.getLocation());
        int distance = (int) hunter.getLocation().distance(target.getLocation());
        hunter.sendMessage(Component.text("Compass set to " + target.getName() + ", " + distance + " blocks away.", NamedTextColor.GOLD));
    }
}
