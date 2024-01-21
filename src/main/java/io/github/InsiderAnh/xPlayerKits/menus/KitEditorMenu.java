package io.github.InsiderAnh.xPlayerKits.menus;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.NBTEditor;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
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
        NBTEditor nbtItem = playerKits.getNbtEditor();
        if (nbtItem.hasTag(currentItem, "icon")) {
            String icon = nbtItem.getString(currentItem, "icon");
            ItemStack itemStack = player.getItemInHand();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                player.sendMessage(playerKits.getLang().getString("messages.noItemHand"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                return;
            }
            kit.getIcons().put(icon, itemStack);
            player.sendMessage(playerKits.getLang().getString("messages.setIcon"));
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
            onUpdate(getInventory());
            return;
        }
        if (nbtItem.hasTag(currentItem, "action")) {
            String action = nbtItem.getString(currentItem, "action");
            if (action.equals("save")) {
                kit.save();
                player.sendMessage(playerKits.getLang().getString("messages.savedKit"));
                player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.parseSound(), 1.0f, 1.0f);
                close();
                return;
            }
            if (action.equals("armor")) {
                kit.setArmor(player.getInventory().getArmorContents());
                player.sendMessage(playerKits.getLang().getString("messages.setArmor"));
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                return;
            }
            if (action.equals("inv")) {
                kit.setInventory(player.getInventory().getContents());
                player.sendMessage(playerKits.getLang().getString("messages.setInventory"));
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                return;
            }
            if (action.equals("oneTime")) {
                kit.setOneTime(!kit.isOneTime());
                player.sendMessage(playerKits.getLang().getString("messages.setOneTime").replace("<state>", XPKUtils.getStatus(kit.isOneTime())));
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                onUpdate(getInventory());
                return;
            }
            if (action.equals("autoArmor")) {
                kit.setAutoArmor(!kit.isAutoArmor());
                player.sendMessage(playerKits.getLang().getString("messages.setAutoArmor").replace("<state>", XPKUtils.getStatus(kit.isAutoArmor())));
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                onUpdate(getInventory());
                return;
            }
            if (action.equals("name")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        if (type.getText().length() > 36) {
                            player.sendMessage(playerKits.getLang().getString("messages.longName"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cLong name"));
                        }
                        playerKits.getKitManager().getKits().remove(kit.getName());
                        kit.setName(type.getText());
                        playerKits.getKitManager().addKit(kit);
                        player.sendMessage(playerKits.getLang().getString("messages.setName").replace("<name>", kit.getName()));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a name")
                    .title("Write a kit name")
                    .open(player);
                return;
            }
            if (action.equals("countdown")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        long seconds;
                        try {
                            seconds = Long.parseLong(type.getText());
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cWrite a valid number"));
                        }
                        kit.setCountdown(seconds);
                        player.sendMessage(playerKits.getLang().getString("messages.setCountdown").replace("<countdown>", String.valueOf(kit.getCountdown())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a countdown")
                    .title("Write a kit countdown")
                    .open(player);
                return;
            }
            if (action.equals("slot")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        int slotI;
                        try {
                            slotI = Integer.parseInt(type.getText());
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cWrite a valid number"));
                        }
                        kit.setSlot(slotI);
                        player.sendMessage(playerKits.getLang().getString("messages.setSlot").replace("<slot>", String.valueOf(kit.getSlot())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a slot")
                    .title("Write a kit slot")
                    .open(player);
                return;
            }
            if (action.equals("page")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        int page;
                        try {
                            page = Integer.parseInt(type.getText());
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cWrite a valid number"));
                        }
                        kit.setPage(page);
                        player.sendMessage(playerKits.getLang().getString("messages.setPage").replace("<page>", String.valueOf(kit.getPage())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a page")
                    .title("Write a kit page")
                    .open(player);
                return;
            }
            if (action.equals("price")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        double amount;
                        try {
                            amount = Double.parseDouble(type.getText());
                        } catch (Exception exception) {
                            player.sendMessage(playerKits.getLang().getString("messages.noNumber"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cWrite a valid number"));
                        }
                        kit.setPrice(amount);
                        player.sendMessage(playerKits.getLang().getString("messages.setPrice").replace("<price>", String.valueOf(kit.getPrice())));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a price")
                    .title("Write a kit price")
                    .open(player);
                return;
            }
            if (action.equals("permission")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        if (type.getText().contains(" ")) {
                            player.sendMessage(playerKits.getLang().getString("messages.permissionNoSpace"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cRemove spaces"));
                        }
                        kit.setName(type.getText());
                        player.sendMessage(playerKits.getLang().getString("messages.setName").replace("<name>", kit.getName()));
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1.0f, 1.0f);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a permission")
                    .title("Write a kit permission")
                    .open(player);
            }
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        ItemStack name = new ItemUtils(XMaterial.PAPER.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.name.nameItem")).lore(playerKits.getLang().getString("menus.newKit.name.loreItem").replace("<name>", kit.getName())).build();
        ItemStack countdown = new ItemUtils(XMaterial.CLOCK.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.countdown.nameItem")).lore(playerKits.getLang().getString("menus.newKit.countdown.loreItem").replace("<countdown>", String.valueOf(kit.getCountdown()))).build();
        ItemStack oneTime = new ItemUtils(XMaterial.EMERALD.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.oneTime.nameItem")).lore(playerKits.getLang().getString("menus.newKit.oneTime.loreItem").replace("<state>", XPKUtils.getStatus(kit.isOneTime()))).build();
        ItemStack autoArmor = new ItemUtils(XMaterial.DIAMOND_HELMET.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.autoArmor.nameItem")).lore(playerKits.getLang().getString("menus.newKit.autoArmor.loreItem").replace("<state>", XPKUtils.getStatus(kit.isAutoArmor()))).build();
        ItemStack permission = new ItemUtils(XMaterial.BARRIER.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.permission.nameItem")).lore(playerKits.getLang().getString("menus.newKit.permission.loreItem").replace("<permission>", kit.getPermission())).build();
        ItemStack armor = new ItemUtils(XMaterial.IRON_CHESTPLATE.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.armor.nameItem")).lore(playerKits.getLang().getString("menus.newKit.armor.loreItem")).build();
        ItemStack inv = new ItemUtils(XMaterial.CHEST.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.inv.nameItem")).lore(playerKits.getLang().getString("menus.newKit.inv.loreItem")).build();
        ItemStack slot = new ItemUtils(XMaterial.GHAST_TEAR.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.slot.nameItem")).lore(playerKits.getLang().getString("menus.newKit.slot.loreItem").replace("<slot>", String.valueOf(kit.getSlot()))).build();
        ItemStack page = new ItemUtils(XMaterial.MAP.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.page.nameItem")).lore(playerKits.getLang().getString("menus.newKit.page.loreItem").replace("<page>", String.valueOf(kit.getPage()))).build();
        ItemStack price = new ItemUtils(XMaterial.GOLD_NUGGET.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.price.nameItem")).lore(playerKits.getLang().getString("menus.newKit.price.loreItem").replace("<price>", String.valueOf(kit.getPrice()))).build();
        ItemStack requirements = new ItemUtils(XMaterial.BOOK.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.requirements.nameItem")).lore(playerKits.getLang().getString("menus.newKit.requirements.loreItem").replace("<requirements>", kit.getRequirementsString())).build();
        ItemStack claimCommands = new ItemUtils(XMaterial.GOLD_INGOT.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.claimCommands.nameItem")).lore(playerKits.getLang().getString("menus.newKit.claimCommands.loreItem").replace("<claimCommands>", kit.getActionsOnClaimString())).build();
        ItemStack denyCommands = new ItemUtils(XMaterial.REDSTONE.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.denyCommands.nameItem")).lore(playerKits.getLang().getString("menus.newKit.denyCommands.loreItem").replace("<denyCommands>", kit.getActionsOnDenyString())).build();
        ItemStack save = new ItemUtils(XMaterial.NETHER_STAR.parseMaterial()).displayName(playerKits.getLang().getString("menus.newKit.save.nameItem")).lore(playerKits.getLang().getString("menus.newKit.save.loreItem")).build();
        inventory.setItem(10, XPKUtils.applySimpleTag(name, "action", "name"));
        inventory.setItem(11, XPKUtils.applySimpleTag(countdown, "action", "countdown"));
        inventory.setItem(12, XPKUtils.applySimpleTag(oneTime, "action", "oneTime"));
        inventory.setItem(13, XPKUtils.applySimpleTag(autoArmor, "action", "autoArmor"));
        inventory.setItem(14, XPKUtils.applySimpleTag(permission, "action", "permission"));
        inventory.setItem(15, XPKUtils.applySimpleTag(armor, "action", "armor"));
        inventory.setItem(16, XPKUtils.applySimpleTag(inv, "action", "inv"));
        inventory.setItem(19, XPKUtils.applySimpleTag(slot, "action", "slot"));
        inventory.setItem(20, XPKUtils.applySimpleTag(page, "action", "page"));
        inventory.setItem(21, XPKUtils.applySimpleTag(price, "action", "price"));
        inventory.setItem(22, XPKUtils.applySimpleTag(requirements, "action", "requirements"));
        inventory.setItem(23, XPKUtils.applySimpleTag(claimCommands, "action", "claimCommands"));
        inventory.setItem(24, XPKUtils.applySimpleTag(denyCommands, "action", "denyCommands"));
        ItemStack icons = new ItemUtils(XMaterial.GREEN_STAINED_GLASS_PANE.parseItem()).displayName(playerKits.getLang().getString("menus.newKit.icons.nameItem")).lore(playerKits.getLang().getString("menus.newKit.icons.loreItem").replace("<denyCommands>", kit.getActionsOnDenyString())).build();
        for (int i = 28; i <= 34; i++) {
            inventory.setItem(i, icons);
        }
        AtomicInteger indexIcons = new AtomicInteger();
        for (String key : kit.getIcons().keySet()) {
            ItemStack icon = new ItemUtils(kit.getIcons().get(key)).displayName(playerKits.getLang().getString("menus.newKit." + key + ".nameItem")).lore(playerKits.getLang().getString("menus.newKit." + key + ".loreItem")).build();
            inventory.setItem(37 + indexIcons.getAndIncrement(), XPKUtils.applySimpleTag(icon, "icon", key));
        }
        inventory.setItem(53, XPKUtils.applySimpleTag(save, "action", "save"));
    }

}