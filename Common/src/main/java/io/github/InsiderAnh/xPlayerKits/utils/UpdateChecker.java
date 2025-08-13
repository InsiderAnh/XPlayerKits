package io.github.InsiderAnh.xPlayerKits.utils;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

@Getter
public class UpdateChecker {

    private String version = "none";

    public UpdateChecker() {
        PlayerKits.getInstance().getStellarTaskHook(() -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=114632/~").openStream();
                 Scanner scanner = new Scanner(is)) {
                if (scanner.hasNext()) {
                    version = scanner.next();
                }
            } catch (IOException e) {
                PlayerKits.getInstance().getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        }).runTask();
    }

    public void sendUpdateMessage(Player player) {
        if (version.equals("none") || version.equals(PlayerKits.getInstance().getDescription().getVersion()))
            return;

        TextComponent textComponent = new TextComponent("§3§lXPlayerKits §8| §fNew version available: §a" + version + " §fYour: §c" + PlayerKits.getInstance().getDescription().getVersion() + " ");
        TextComponent download = new TextComponent("§6[Download]");
        download.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§fClick to download")));
        download.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/stellarprotect"));
        TextComponent changelog = new TextComponent(" §6[Changelog]");
        changelog.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§fClick to see changelog")));
        changelog.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/plugin/stellarprotect/changelog"));
        textComponent.addExtra(download);
        textComponent.addExtra(changelog);

        player.spigot().sendMessage(textComponent);
    }

}