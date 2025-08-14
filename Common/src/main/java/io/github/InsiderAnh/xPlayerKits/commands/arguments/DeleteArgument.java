package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import com.google.common.base.Joiner;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DeleteArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (arguments.length < 1) {
            sendHelp(sender);
            return;
        }
        if (!sender.hasPermission("xkits.admin")) {
            sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        Kit kit = playerKits.getKitManager().removeKit(Joiner.on(" ").join(arguments));
        if (kit != null) {
            File fileKit = new File(playerKits.getDataFolder(), kit.getName() + ".yml");
            if (fileKit.exists()) {
                fileKit.delete();
            }
            sender.sendMessage(playerKits.getLang().getString("messages.deletedKit"));
        } else {
            sender.sendMessage(playerKits.getLang().getString("messages.noExistsKit"));
        }
    }

}