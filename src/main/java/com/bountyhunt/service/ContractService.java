package com.bountyhunt.service;

import org.bukkit.entity.Player;
import java.util.UUID;

public interface ContractService {
    void createContract(Player issuer, Player target, String type);
    boolean completeContract(Player hunter, Player target, String type);
}
