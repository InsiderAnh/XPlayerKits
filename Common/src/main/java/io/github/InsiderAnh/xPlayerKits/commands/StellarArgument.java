package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class StellarArgument {

    protected final PlayerKits playerKits = PlayerKits.getInstance();

    public abstract void onCommand(@NotNull CommandSender sender, String[] arguments);

    protected void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
        sender.sendMessage(ChatColor.DARK_GRAY + "[!] " + ChatColor.RED + "XPlayerKits " + ChatColor.DARK_GRAY + "[!]");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
        sender.sendMessage("§e/xkits editor §7- §fOpens the kit editor.");
        sender.sendMessage("§e/xkits kits §7- §fOpens the kit menu.");
        sender.sendMessage("§e/xkits give <kitName> <player> §7- §fDirectly give kits to players without verifications..");
        sender.sendMessage("§e/xkits claim <kitName> <player> §7- §fGive kits to players with verifications.");
        sender.sendMessage("§e/xkits delete <kitName> §7- §fDelete a kit.");
        sender.sendMessage("§e/xkits preview <kitName> §7- §fPreview a kit.");
        sender.sendMessage("§e/xkits reset <kitName> <player> §7- §fReset a certain kit data.");
        sender.sendMessage("§e/xkits resetall <player> §7- §fReset all kit data.");
        sender.sendMessage("§e/xkits migrate playerkits2_yml/playerkits2_mysql §7- §fMigrate data from playerkits2 plugin.");
        sender.sendMessage("§e/xkits migratekits playerkits2 §7- §fMigrate kit from playerkits2 plugin.");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
    }

}