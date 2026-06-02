package com.bountyhunt.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class BountyClaimEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Player hunter;
    private final Player target;
    private final double amount;

    public BountyClaimEvent(Player hunter, Player target, double amount) {
        this.hunter = hunter;
        this.target = target;
        this.amount = amount;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
