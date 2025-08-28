package io.github.InsiderAnh.xPlayerKits.executions.executions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.api.ColorUtils;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.executions.enums.MessageType;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.CenterMessage;
import io.github.InsiderAnh.xPlayerKits.utils.LanguageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExecuteMessage extends Execution {

    private final MessageType messageType;
    private final String message;

    public ExecuteMessage(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
    }

    @Override
    public void execute(Player player, Placeholder... placeholders) {
        ColorUtils colorUtils = PlayerKits.getInstance().getColorUtils();
        PlayerKitsNMS nms = PlayerKits.getInstance().getPlayerKitsNMS();
        String replacedMessage = colorUtils.color(PlaceholderAPI.setPlaceholders(player, LanguageUtils.replacePlaceholders(this.message, placeholders)));

        if (messageType.equals(MessageType.NORMAL)) {
            player.sendMessage(replacedMessage);
        } else if (messageType.equals(MessageType.CENTERED)) {
            player.sendMessage(CenterMessage.getCenteredMessage(replacedMessage));
        } else if (messageType.equals(MessageType.MINI_MESSAGE)) {
            nms.sendMiniMessage(player, replacedMessage);
        } else if (messageType.equals(MessageType.ACTION_BAR)) {
            nms.sendActionBar(player, replacedMessage);
        } else if (messageType.equals(MessageType.BROADCAST)) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(replacedMessage);
            }
        }
    }

}