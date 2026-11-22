package com.bountyhunt;

import lombok.Getter;
import com.bountyhunt.command.BountyCommand;
import com.bountyhunt.config.ConfigManager;
import com.bountyhunt.gui.GuiManager;
import com.bountyhunt.listener.BountyListener;
import com.bountyhunt.manager.BountyManager;
import com.bountyhunt.manager.ReputationManager;
import com.bountyhunt.manager.WantedManager;
import com.bountyhunt.service.BountyService;
import com.bountyhunt.service.EconomyProvider;
import com.bountyhunt.service.InsuranceService;
import com.bountyhunt.service.ReputationService;
import com.bountyhunt.service.impl.BountyServiceImpl;
import com.bountyhunt.service.impl.InsuranceServiceImpl;
import com.bountyhunt.service.impl.ReputationServiceImpl;
import com.bountyhunt.service.impl.VaultEconomyProvider;
import com.bountyhunt.storage.Storage;
import com.bountyhunt.storage.impl.SQLiteStorage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@Getter
public final class BountyHunt extends JavaPlugin {

    private static BountyHunt instance;
    private ConfigManager configManager;
    private Storage storage;
    private EconomyProvider economyProvider;
    
    private BountyManager bountyManager;
    private WantedManager wantedManager;
    private GuiManager guiManager;
    private ReputationManager reputationManager;
    
    private BountyService bountyService;
    private ReputationService reputationService;
    private InsuranceService insuranceService;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling BountyHunt...");

        try {
            // 1. Setup Economy
            if (!setupEconomy()) return;

            // 2. Load Configs
            this.configManager = new ConfigManager(this);
            this.configManager.reloadConfigs();

            // 3. Setup Storage
            this.storage = new SQLiteStorage(this);
            this.storage.init();

            // 4. Initialize Managers
            this.bountyManager = new BountyManager(this);
            this.bountyManager.loadBounties();
            this.wantedManager = new WantedManager(this, bountyManager);
            this.guiManager = new GuiManager(this);
            this.reputationManager = new ReputationManager(this);

            // 5. Initialize Services
            this.bountyService = new BountyServiceImpl(this, bountyManager);
            this.reputationService = new ReputationServiceImpl(this, reputationManager);
            this.insuranceService = new InsuranceServiceImpl(this);

            // 6. Register Listeners
            getServer().getPluginManager().registerEvents(new BountyListener(this, bountyService), this);
            getServer().getPluginManager().registerEvents(new com.bountyhunt.listener.GuiClickListener(this), this);

            // 7. Register Commands
            BountyCommand bountyCommand = new BountyCommand(this, bountyService, guiManager);
            getCommand("bounty").setExecutor(bountyCommand);
            getCommand("bounty").setTabCompleter(bountyCommand);

            // 8. Integration
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new com.bountyhunt.integration.BountyPlaceholderExpansion(this).register();
            }

            getLogger().info("BountyHunt has been successfully enabled!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "CRITICAL ERROR during enable!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        getLogger().info("BountyHunt has been disabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economyProvider = new VaultEconomyProvider(rsp.getProvider());
                getLogger().info("Hooked into Vault economy.");
                return true;
            }
        }
        getLogger().severe("No economy provider found! Disabling...");
        getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    public static BountyHunt getInstance() {
        return instance;
    }
}
