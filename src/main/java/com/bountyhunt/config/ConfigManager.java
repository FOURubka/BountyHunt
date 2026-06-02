package com.bountyhunt.config;

import com.bountyhunt.BountyHunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final BountyHunt plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(BountyHunt plugin) {
        this.plugin = plugin;
    }

    public void reloadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        saveDefaultMessages();
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void saveDefaultMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public FileConfiguration getMessages() {
        return messagesConfig;
    }

    public void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml!");
        }
    }
}
