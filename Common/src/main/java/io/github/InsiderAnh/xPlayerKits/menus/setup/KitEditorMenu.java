package io.github.InsiderAnh.xPlayerKits.menus.setup;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
import io.github.InsiderAnh.xPlayerKits.menus.setup.actions.KitMainActionsMenu;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitEditorMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;

    public KitEditorMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.newKit.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("icon")) {
            String icon = nbtItem.getString("icon");
            ItemStack itemStack = player.getItemInHand();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                player.sendMessage(playerKits.getLang().getString("messages.noItemHand"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                return;
            }
            kit.setIcon(icon, itemStack);
            player.sendMessage(playerKits.getLang().getString("messages.setIcon"));
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
            onUpdate(getInventory());
            return;
        }
        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            switch (action) {
                case "claimsSettings":
                    new KitClaimsSettingsMenu(player, kit).open();
                    return;
                case "inventorySettings":
                    new KitInventorySettingsMenu(player, kit).open();
                    return;
                case "actionsRequirements":
                    new KitMainActionsMenu(player, kit).open();
                    return;
                case "save":
                    kit.save();
                    playerKits.getKitManager().load();
                    player.sendMessage(playerKits.getLang().getString("messages.savedKit"));
                    player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0f, 1.0f);
                    close();
                    return;
                case "back":
                    new KitMainEditorMenu(player, 1).open();
                    return;
                case "armor":
                case "inv":
                    new KitContentMenu(player, kit).open();
                    return;
                case "name":
                    handleAnvilGUI(player, "Write a name", "Write a name", string -> {
                        if (string.length() > 36) {
                            player.sendMessage(playerKits.getLang().getString("messages.longName"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                            return;
                        }
                        playerKits.getKitManager().removeKit(kit.getName());
                        kit.setName(string);
                        playerKits.getKitManager().addKit(kit);
                        player.sendMessage(playerKits.getLang().getString("messages.setName").replace("<name>", kit.getName()));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                    });
                    return;
                case "price":
                    handleAnvilGUI(player, "Write a price", "Write a price", string -> {
                        double amount;
                        try {
                            amount = Double.parseDouble(string);
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                            return;
                        }
                        kit.setPrice(amount);
                        player.sendMessage(playerKits.getLang().getString("messages.setPrice").replace("<price>", String.valueOf(kit.getPrice())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                    });
                    return;
                case "permission":
                    handleAnvilGUI(player, "Write a kit permission", "Write a permission", string -> {
                        if (string.contains(" ")) {
                            player.sendMessage(playerKits.getLang().getString("messages.permissionNoSpace"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                        } else {
                            kit.setName(string);
                            player.sendMessage(playerKits.getLang().getString("messages.setName").replace("<name>", kit.getName()));
                            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                            new KitEditorMenu(player, kit).open();
                        }
                    });
                    break;
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
        ItemStack name = new ItemUtils(XMaterial.PAPER.get()).displayName(playerKits.getLang().getString("menus.newKit.name.nameItem")).lore(playerKits.getLang().getString("menus.newKit.name.loreItem").replace("<name>", kit.getName())).build();
        ItemStack claimsSettings = new ItemUtils(XMaterial.ANVIL.get()).displayName(playerKits.getLang().getString("menus.newKit.claimsSettings.nameItem")).lore(playerKits.getLang().getString("menus.newKit.claimsSettings.loreItem")).build();
        ItemStack permission = new ItemUtils(XMaterial.BARRIER.get()).displayName(playerKits.getLang().getString("menus.newKit.permission.nameItem")).lore(playerKits.getLang().getString("menus.newKit.permission.loreItem").replace("<permission>", kit.getPermission())).build();
        ItemStack armor = new ItemUtils(XMaterial.IRON_CHESTPLATE.get()).displayName(playerKits.getLang().getString("menus.newKit.armor.nameItem")).lore(playerKits.getLang().getString("menus.newKit.armor.loreItem")).build();
        ItemStack inv = new ItemUtils(XMaterial.CHEST.get()).displayName(playerKits.getLang().getString("menus.newKit.inv.nameItem")).lore(playerKits.getLang().getString("menus.newKit.inv.loreItem")).build();
        ItemStack inventorySettings = new ItemUtils(XMaterial.ENDER_CHEST.get()).displayName(playerKits.getLang().getString("menus.newKit.inventorySettings.nameItem")).lore(playerKits.getLang().getString("menus.newKit.inventorySettings.loreItem")).build();
        ItemStack price = new ItemUtils(XMaterial.GOLD_NUGGET.get()).displayName(playerKits.getLang().getString("menus.newKit.price.nameItem")).lore(playerKits.getLang().getString("menus.newKit.price.loreItem").replace("<price>", String.valueOf(kit.getPrice()))).build();
        ItemStack actionsRequirements = new ItemUtils(XMaterial.BOOK.get()).displayName(playerKits.getLang().getString("menus.newKit.actionsRequirements.nameItem")).lore(playerKits.getLang().getString("menus.newKit.actionsRequirements.loreItem")).build();
        ItemStack back = new ItemUtils(XMaterial.ARROW.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem")).build();
        ItemStack save = new ItemUtils(XMaterial.NETHER_STAR.get()).displayName(playerKits.getLang().getString("menus.newKit.save.nameItem")).lore(playerKits.getLang().getString("menus.newKit.save.loreItem")).build();
        inventory.setItem(10, XPKUtils.applySimpleTag(name, "action", "name"));
        inventory.setItem(11, XPKUtils.applySimpleTag(claimsSettings, "action", "claimsSettings"));
        inventory.setItem(13, XPKUtils.applySimpleTag(permission, "action", "permission"));
        inventory.setItem(15, XPKUtils.applySimpleTag(armor, "action", "armor"));
        inventory.setItem(16, XPKUtils.applySimpleTag(inv, "action", "inv"));
        inventory.setItem(19, XPKUtils.applySimpleTag(inventorySettings, "action", "inventorySettings"));
        inventory.setItem(22, XPKUtils.applySimpleTag(price, "action", "price"));
        inventory.setItem(25, XPKUtils.applySimpleTag(actionsRequirements, "action", "actionsRequirements"));
        ItemStack icons = new ItemUtils(XMaterial.GREEN_STAINED_GLASS_PANE.parseItem()).displayName(playerKits.getLang().getString("menus.newKit.icons.nameItem")).lore(playerKits.getLang().getString("menus.newKit.icons.loreItem").replace("<denyCommands>", kit.getActionsOnDenyString())).build();
        for (int i = 28; i <= 34; i++) {
            inventory.setItem(i, icons);
        }
        AtomicInteger indexIcons = new AtomicInteger();
        for (String key : kit.getIcons().keySet()) {
            ItemStack icon = new ItemUtils(kit.getIcons().get(key)).displayName(playerKits.getLang().getString("menus.newKit." + key + ".nameItem")).lore(playerKits.getLang().getString("menus.newKit." + key + ".loreItem")).build();
            inventory.setItem(37 + indexIcons.getAndIncrement(), XPKUtils.applySimpleTag(icon, "icon", key));
        }
        inventory.setItem(45, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(53, XPKUtils.applySimpleTag(save, "action", "save"));
    }

}