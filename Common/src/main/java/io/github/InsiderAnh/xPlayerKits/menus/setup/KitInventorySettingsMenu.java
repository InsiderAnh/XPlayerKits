package io.github.InsiderAnh.xPlayerKits.menus.setup;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
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

public class KitInventorySettingsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;

    public KitInventorySettingsMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X4, PlayerKits.getInstance().getLang().getString("menus.inventorySettings.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            switch (action) {
                case "slot":
                    handleAnvilGUI(player, "Write a kit slot", "Write a slot", string -> {
                        int slotI;
                        try {
                            slotI = Integer.parseInt(string);
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                            return;
                        }
                        kit.setSlot(slotI);
                        player.sendMessage(playerKits.getLang().getString("messages.setSlot").replace("<slot>", String.valueOf(kit.getSlot())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                    });
                    return;
                case "page":
                    handleAnvilGUI(player, "Write a kit page", "Write a page", string -> {
                        int page;
                        try {
                            page = Integer.parseInt(string);
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                            return;
                        }
                        kit.setPage(page);
                        player.sendMessage(playerKits.getLang().getString("messages.setPage").replace("<page>", String.valueOf(kit.getPage())));
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
        ItemStack slot = new ItemUtils(XMaterial.GHAST_TEAR.get()).displayName(playerKits.getLang().getString("menus.newKit.slot.nameItem")).lore(playerKits.getLang().getString("menus.newKit.slot.loreItem").replace("<slot>", String.valueOf(kit.getSlot()))).build();
        ItemStack page = new ItemUtils(XMaterial.MAP.get()).displayName(playerKits.getLang().getString("menus.newKit.page.nameItem")).lore(playerKits.getLang().getString("menus.newKit.page.loreItem").replace("<page>", String.valueOf(kit.getPage()))).build();

        ItemStack back = new ItemUtils(XMaterial.ARROW.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem")).build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem")).build();

        inventory.setItem(11, XPKUtils.applySimpleTag(slot, "action", "slot"));
        inventory.setItem(15, XPKUtils.applySimpleTag(page, "action", "page"));

        inventory.setItem(27, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(31, XPKUtils.applySimpleTag(close, "action", "close"));
    }

}