package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.enums.ServerVersion;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class KitContentMenu extends AInventory {

    private final PlayerKits plugin = PlayerKits.getInstance();
    private final Kit kit;

    public KitContentMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.kitContent.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        NBTItem nbtItem = new NBTItem(currentItem);
        Player player = getPlayer();
        Inventory inventory = getInventory();
        if (nbtItem.hasTag("itemId")) {
            String itemId = nbtItem.getString("itemId");
            if (itemId.equals("save")) {
                ItemStack[] inv = new ItemStack[36];
                for (int i = 0; i < 36; i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item == null || item.getType().equals(XMaterial.AIR.parseMaterial())) continue;
                    inv[i] = item;
                }
                kit.setInventory(inv);
                ItemStack[] armor = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    ItemStack item = inventory.getItem(i + 45);
                    if (item == null || item.getType().equals(XMaterial.AIR.parseMaterial())) continue;
                    armor[3 - i] = item;
                }
                kit.setArmor(armor);

                if (XPKUtils.SERVER_VERSION.serverVersionGreaterEqualThan(ServerVersion.v1_9)) {
                    ItemStack offhand = inventory.getItem(50);
                    if (offhand != null && !offhand.getType().equals(XMaterial.AIR.parseMaterial())) {
                        kit.setOffhand(offhand);
                    }
                    player.sendMessage(plugin.getLang().getString("messages.savedInventory"));
                    player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0f, 1.0f);
                    new KitEditorMenu(getPlayer(), kit).open();
                }
            }
            canceled.accept(true);
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        ItemStack helmet = new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.helmet.nameItem")).build();
        ItemStack chestplate = new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.chestplate.nameItem")).build();
        ItemStack leggings = new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.leggings.nameItem")).build();
        ItemStack boots = new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.boots.nameItem")).build();
        ItemStack save = new ItemUtils(XMaterial.NETHER_STAR.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.save.nameItem")).lore(plugin.getLang().getString("menus.kitContent.save.loreItem")).build();
        ItemStack glass = XPKUtils.applySimpleTag(new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName("§7").build(), "itemId", "glass");

        inventory.setItem(36, XPKUtils.applySimpleTag(helmet, "itemId", "helmet"));
        inventory.setItem(37, XPKUtils.applySimpleTag(chestplate, "itemId", "chestplate"));
        inventory.setItem(38, XPKUtils.applySimpleTag(leggings, "itemId", "leggings"));
        inventory.setItem(39, XPKUtils.applySimpleTag(boots, "itemId", "boots"));
        inventory.setItem(40, glass);
        inventory.setItem(42, glass);
        inventory.setItem(43, glass);
        inventory.setItem(44, glass);
        inventory.setItem(49, glass);
        inventory.setItem(51, glass);
        inventory.setItem(52, glass);
        inventory.setItem(53, XPKUtils.applySimpleTag(save, "itemId", "save"));

        if (XPKUtils.SERVER_VERSION.serverVersionGreaterEqualThan(ServerVersion.v1_9)) {
            ItemStack offhand = new ItemUtils(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem()).displayName(plugin.getLang().getString("menus.kitContent.offhand.nameItem")).build();
            inventory.setItem(41, XPKUtils.applySimpleTag(offhand, "itemId", "offhand"));
        } else {
            inventory.setItem(41, glass);
            inventory.setItem(50, glass);
        }
    }

}
