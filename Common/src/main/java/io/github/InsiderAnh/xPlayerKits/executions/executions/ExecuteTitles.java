package io.github.InsiderAnh.xPlayerKits.executions.executions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.api.ColorUtils;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class ExecuteTitles extends Execution {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ExecuteTitles(String action, String data) {
        super(action);
        String[] sep = data.split(";");
        title = sep[0];
        subtitle = sep.length >= 2 ? sep[1] : "";
        fadeIn = sep.length >= 3 ? Integer.parseInt(sep[2]) : 10;
        stay = sep.length >= 4 ? Integer.parseInt(sep[3]) : 20;
        fadeOut = sep.length >= 5 ? Integer.parseInt(sep[4]) : 10;
    }

    @Override
    public void execute(Player player, Placeholder... placeholders) {
        PlayerKitsNMS nms = PlayerKits.getInstance().getPlayerKitsNMS();
        ColorUtils colorUtils = PlayerKits.getInstance().getColorUtils();

        StringBuilder titleBuilder = new StringBuilder(this.title);
        StringBuilder subtitleBuilder = new StringBuilder(this.subtitle);

        for (Placeholder placeholder : placeholders) {
            String placeholderStr = "<" + placeholder.getPlaceholder() + ">";
            String value = placeholder.getValue();

            int index;
            while ((index = titleBuilder.indexOf(placeholderStr)) != -1) {
                titleBuilder.replace(index, index + placeholderStr.length(), value);
            }
            while ((index = subtitleBuilder.indexOf(placeholderStr)) != -1) {
                subtitleBuilder.replace(index, index + placeholderStr.length(), value);
            }
        }

        String replacedTitle = colorUtils.color(PlaceholderAPI.setPlaceholders(player, titleBuilder.toString()));
        String replacedSubTitle = colorUtils.color(PlaceholderAPI.setPlaceholders(player, subtitleBuilder.toString()));

        nms.sendTitle(player, replacedTitle, replacedSubTitle, fadeIn, stay, fadeOut);
    }

    public String getActionType() {
        return "titles";
    }

}