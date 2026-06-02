package com.bountyhunt.command;

import com.bountyhunt.BountyHunt;
import com.bountyhunt.gui.GuiManager;
import com.bountyhunt.service.BountyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BountyCommand implements CommandExecutor, TabCompleter {

    private final BountyHunt plugin;
    private final BountyService bountyService;
    private final GuiManager guiManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BountyCommand(BountyHunt plugin, BountyService bountyService, GuiManager guiManager) {
        this.plugin = plugin;
        this.bountyService = bountyService;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(plugin.getConfigManager().getMessages().getString("general.player-only")));
            return true;
        }

        if (args.length == 0) {
            guiManager.openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "set" -> handleSet(player, args);
            case "add" -> handleAdd(player, args);
            case "info" -> handleInfo(player, args);
            case "top" -> handleTop(player);
            case "track" -> player.sendMessage(Component.text("Tracking feature coming soon..."));
            case "reload" -> {
                if (player.hasPermission("bountyhunt.admin")) {
                    plugin.getConfigManager().reloadConfigs();
                    player.sendMessage(Component.text("Config reloaded!"));
                }
            }
            default -> player.sendMessage(Component.text("Usage: /bounty [set|add|info|top|track|reload]"));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("set", "add", "info", "top", "track", "reload");
        }
        return null;
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(mm.deserialize("<red>Usage: /bounty set <player> <amount>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize("<red>Player not found."));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(mm.deserialize("<red>You cannot set a bounty on yourself!"));
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Invalid amount!"));
            return;
        }

        double minAmount = plugin.getConfig().getDouble("settings.bounty.min-amount", 100.0);
        if (amount < minAmount) {
            player.sendMessage(mm.deserialize("<red>Minimum bounty is <gold>$" + minAmount));
            return;
        }

        if (!plugin.getEconomyProvider().has(player, amount)) {
            player.sendMessage(mm.deserialize("<red>Insufficient funds!"));
            return;
        }

        bountyService.placeBounty(player, target, amount).thenAccept(success -> {
            player.sendMessage(mm.deserialize(success ? "<green>Bounty set successfully on <red>" + target.getName() : "<red>Failed to set bounty."));
        });
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(mm.deserialize("<red>Usage: /bounty add <player> <amount>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize("<red>Player not found."));
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Invalid amount!"));
            return;
        }

        bountyService.placeBounty(player, target, amount).thenAccept(success -> {
            player.sendMessage(mm.deserialize(success ? "<green>Bounty added successfully!" : "<red>Failed to add bounty."));
        });
    }

    private void handleInfo(Player player, String[] args) {
        String targetName = args.length > 1 ? args[1] : player.getName();
        player.sendMessage(mm.deserialize("<gray>Bounty on <red>" + targetName + "<gray>: <gold>$" + bountyService.getActiveBounty(Bukkit.getOfflinePlayer(targetName).getUniqueId())));
    }

    private void handleTop(Player player) {
        player.sendMessage(mm.deserialize("<gradient:#ffaa00:#ff5500><bold>Top Bounties:</bold></gradient>"));
        bountyService.getBountyCache().entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .forEach(e -> player.sendMessage(mm.deserialize("<gray>- <red>" + Bukkit.getOfflinePlayer(e.getKey()).getName() + "<gray>: <gold>$" + e.getValue())));
    }
}
