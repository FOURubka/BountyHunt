package com.bountyhunt.api;

import com.bountyhunt.BountyHunt;
import java.util.UUID;

public class BountyHuntAPI {
    
    public static double getBounty(UUID uuid) {
        return BountyHunt.getInstance().getBountyService().getActiveBounty(uuid);
    }

    public static void setBounty(UUID uuid, double amount) {
        BountyHunt.getInstance().getBountyManager().setBountyDirectly(uuid, amount);
    }
}
