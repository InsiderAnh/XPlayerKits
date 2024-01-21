package io.github.InsiderAnh.xPlayerKits.menus;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.utils.InventorySerializable;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class KitSlotEditorMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    public KitSlotEditorMenu(Player player) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.kitSlot.title"));
        player.sendMessage(playerKits.getLang().getString("messages.openEditor"));
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
    }

    @Override
    protected void onClose() {
        super.onClose();
        Player player = getPlayer();
        InsiderConfig inventories = playerKits.getInventories();
        for (int slot = 0; slot < 54; slot++) {
            inventories.set("inventories.kits." + slot, getInventory().getItem(slot));
        }
        inventories.save();
        player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0f, 1.0f);
        player.sendMessage(playerKits.getLang().getString("messages.savedInventory"));
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        InsiderConfig inventories = playerKits.getInventories();
        if (!inventories.isSet("inventories.kits")) {
            ItemStack yellow = new ItemUtils(XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem()).displayName("§7").build();
            ItemStack last = new ItemUtils(XMaterial.ARROW.parseItem()).displayName("{LAST_SLOT}").build();
            ItemStack next = new ItemUtils(XMaterial.ARROW.parseItem()).displayName("{NEXT_SLOT}").build();
            ItemStack close = new ItemUtils(XMaterial.BOOK.parseItem()).displayName("{CLOSE_SLOT}").build();
            inventory.setItem(0, yellow);
            inventory.setItem(1, yellow);
            inventory.setItem(2, yellow);
            inventory.setItem(6, yellow);
            inventory.setItem(7, yellow);
            inventory.setItem(8, yellow);
            inventory.setItem(9, yellow);
            inventory.setItem(10, yellow);
            inventory.setItem(16, yellow);
            inventory.setItem(17, yellow);
            inventory.setItem(36, yellow);
            inventory.setItem(37, yellow);
            inventory.setItem(38, last);
            inventory.setItem(42, next);
            inventory.setItem(43, yellow);
            inventory.setItem(44, yellow);
            inventory.setItem(45, yellow);
            inventory.setItem(46, yellow);
            inventory.setItem(47, yellow);
            inventory.setItem(49, close);
            inventory.setItem(51, yellow);
            inventory.setItem(52, yellow);
            inventory.setItem(53, yellow);
        }
        for (int slot = 0; slot < 54; slot++) {
            if (!inventories.isSet("inventories.kits." + slot)) continue;
            ItemStack itemStack = inventories.getConfig().getItemStack("inventories.kits." + slot);
            inventory.setItem(slot, itemStack);
        }
    }

}