package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.VotingMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VotingArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only for players.");
            return;
        }

        Player player = (Player) sender;

        if (!playerKits.getVotingManager().isVotingEnabled()) {
            player.sendMessage(playerKits.getLang().getString("messages.votingDisabled"));
            return;
        }

        playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
            playerKits.getStellarTaskHook(() -> new VotingMenu(player, playerKitData).open()).runTask(player.getLocation());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

}