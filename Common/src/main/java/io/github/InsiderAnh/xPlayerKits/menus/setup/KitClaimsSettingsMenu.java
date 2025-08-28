package io.github.InsiderAnh.xPlayerKits.menus.setup;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyInventory;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyTiming;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

public class KitClaimsSettingsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;

    public KitClaimsSettingsMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X4, PlayerKits.getInstance().getLang().getString("menus.kitClaims.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();

        PropertyTiming propertyTiming = kit.getPropertyTiming();
        PropertyInventory propertyInventory = kit.getPropertyInventory();

        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            switch (action) {
                case "oneTime":
                    propertyTiming.setOneTime(!propertyTiming.isOneTime());
                    player.sendMessage(playerKits.getLang().getString("messages.setOneTime").replace("<state>", XPKUtils.getStatus(propertyTiming.isOneTime())));
                    player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
                    return;
                case "autoArmor":
                    propertyInventory.setAutoArmor(!propertyInventory.isAutoArmor());
                    player.sendMessage(playerKits.getLang().getString("messages.setAutoArmor").replace("<state>", XPKUtils.getStatus(propertyInventory.isAutoArmor())));
                    player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
                    return;
                case "countdown":
                    handleAnvilGUI(player, "Write a countdown", "Write a countdown", string -> {
                        long seconds;
                        try {
                            seconds = Long.parseLong(string);
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                            return;
                        }
                        propertyTiming.setCountdown(seconds);
                        player.sendMessage(playerKits.getLang().getString("messages.setCountdown").replace("<countdown>", String.valueOf(propertyTiming.getCountdown())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                    });
                    return;
                case "back":
                    new KitEditorMenu(player, kit).open();
                    return;
                case "close":
                    close();
                    return;
                default:
            }
        }
    }

    private void handleAnvilGUI(Player player, String title, String text, Consumer<String> execute) {
        new AnvilGUI.Builder()
            .plugin(playerKits)
            .itemLeft(new ItemStack(Material.PAPER))
            .title(title)
            .text(text)
            .onClick((slot, type) -> {
                execute.accept(type.getText());
                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            })
            .open(player);
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        PropertyTiming propertyTiming = kit.getPropertyTiming();
        PropertyInventory propertyInventory = kit.getPropertyInventory();

        ItemStack countdown = new ItemUtils(XMaterial.CLOCK.get()).displayName(playerKits.getLang().getString("menus.kitClaims.countdown.nameItem")).lore(playerKits.getLang().getString("menus.kitClaims.countdown.loreItem").replace("<countdown>", String.valueOf(propertyTiming.getCountdown()))).build();
        ItemStack oneTime = new ItemUtils(XMaterial.EMERALD.get()).displayName(playerKits.getLang().getString("menus.kitClaims.oneTime.nameItem")).lore(playerKits.getLang().getString("menus.kitClaims.oneTime.loreItem").replace("<state>", XPKUtils.getStatus(propertyTiming.isOneTime()))).build();
        ItemStack autoArmor = new ItemUtils(XMaterial.DIAMOND_HELMET.get()).displayName(playerKits.getLang().getString("menus.kitClaims.autoArmor.nameItem")).lore(playerKits.getLang().getString("menus.kitClaims.autoArmor.loreItem").replace("<state>", XPKUtils.getStatus(propertyInventory.isAutoArmor()))).build();

        ItemStack back = new ItemUtils(XMaterial.ARROW.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem")).build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem")).build();

        inventory.setItem(11, XPKUtils.applySimpleTag(countdown, "action", "countdown"));
        inventory.setItem(13, XPKUtils.applySimpleTag(oneTime, "action", "oneTime"));
        inventory.setItem(15, XPKUtils.applySimpleTag(autoArmor, "action", "autoArmor"));

        inventory.setItem(27, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(31, XPKUtils.applySimpleTag(close, "action", "close"));
    }

}