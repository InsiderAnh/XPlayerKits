package io.github.InsiderAnh.xPlayerKits.menus;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.InventorySerializable;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.NBTEditor;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.function.Consumer;

public class KitsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final HashMap<Integer, ItemStack> kitsMenu = new HashMap<>();
    private final HashMap<String, Integer> slotsNumbers = new HashMap<>();
    private final PlayerKitData playerKitData;
    private int page;

    public KitsMenu(Player player, PlayerKitData playerKitData, int page) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.kitsMenu.title"));
        this.playerKitData = playerKitData;
        this.page = page;
        InsiderConfig inventories = playerKits.getInventories();
        for (int slot = 0; slot < 54; slot++) {
            if (!inventories.isSet("inventories.kits." + slot)) continue;
            ItemStack itemStack = inventories.getConfig().getItemStack("inventories.kits." + slot);
            kitsMenu.put(slot, itemStack);
            if (itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
                slotsNumbers.put(itemStack.getItemMeta().getDisplayName(), slot);
            }
        }
        onUpdate(getInventory());
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTEditor nbtItem = playerKits.getNbtEditor();
        if (nbtItem.hasTag(currentItem, "action")) {
            String action = nbtItem.getString(currentItem, "action");
            if (action.equals("close")) {
                close();
            }
            if (action.equals("last")) {
                page = page + 1;
                onUpdate(getInventory());
            }
            if (action.equals("next")) {
                page = page - 1;
                onUpdate(getInventory());
            }
        }
        if (nbtItem.hasTag(currentItem, "kit")) {
            Kit kit = playerKits.getKitManager().getKits().get(nbtItem.getString(currentItem, "kit"));
            if (kit == null) return;

            if (kit.isNoHasRequirements(player)) {
                player.sendMessage(playerKits.getLang().getString("messages.noRequirements"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                return;
            }

            if (!kit.getPermission().equals("none") && player.hasPermission(kit.getPermission())) {
                XPKUtils.executeActions(player, kit.getActionsOnDeny());
                player.sendMessage(playerKits.getLang().getString("messages.noPermissionKit"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                return;
            }

            KitData kitData = playerKitData.getKitsData().get(kit.getName());
            if (kit.isOneTime()) {
                if (kitData != null && kitData.isOneTime()) {
                    XPKUtils.executeActions(player, kit.getActionsOnDeny());
                    player.sendMessage(playerKits.getLang().getString("messages.alreadyOneTime"));
                    player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                    return;
                }
            }

            if (kitData != null && kitData.getCountdown() > System.currentTimeMillis() && !player.hasPermission("xkits.countdown.bypass")) {
                XPKUtils.executeActions(player, kit.getActionsOnDeny());
                player.sendMessage(playerKits.getLang().getString("messages.waitCountdown").replace("<time>", XPKUtils.millisToLongDHMS(kitData.getCountdown() - System.currentTimeMillis())));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                return;
            }

            close();

            playerKitData.getKitsData().put(kit.getName(), new KitData(kit.getName(), System.currentTimeMillis() + (kit.getCountdown() * 1000L), kit.isOneTime(), false));
            playerKits.getExecutor().execute(() -> {
                playerKits.getDatabase().updatePlayerData(player.getUniqueId());
                Bukkit.getScheduler().runTask(playerKits, () -> kit.giveKit(player));
            });
        }
    }

    @Override
    protected void onDrag(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
    }

    @Override
    protected void onAllClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
    }

    @Override
    protected void onBottom(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        inventory.clear();
        Player player = getPlayer();
        int slotClose = slotsNumbers.getOrDefault("{CLOSE_SLOT}", -1);
        if (slotClose > 0) {
            ItemStack close = new ItemUtils(kitsMenu.get(slotClose)).displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem")).build();
            inventory.setItem(slotClose, XPKUtils.applySimpleTag(close, "action", "close"));
        }
        for (int slot : kitsMenu.keySet()) {
            if (slotsNumbers.containsValue(slot)) continue;
            inventory.setItem(slot, kitsMenu.get(slot));
        }
        for (Kit kit : playerKits.getKitManager().getKits().values()) {
            if (kit.getPage() != page) continue;
            inventory.setItem(kit.getSlot(), XPKUtils.applySimpleTag(kit.getIcons().get(getIcon(player, kit, playerKitData)), "kit", kit.getName()));
        }
        if (page > 1) {
            int slotLast = slotsNumbers.getOrDefault("{LAST_SLOT}", -1);
            if (slotLast > 0) {
                ItemStack last = new ItemUtils(kitsMenu.get(slotLast)).displayName(playerKits.getLang().getString("menus.kitsMenu.last.nameItem")).build();
                inventory.setItem(slotLast, XPKUtils.applySimpleTag(last, "action", "last"));
            }
        }
        if (page < playerKits.getKitManager().getLastPage()) {
            int slotNext = slotsNumbers.getOrDefault("{NEXT_SLOT}", -1);
            if (slotNext > 0) {
                ItemStack next = new ItemUtils(kitsMenu.get(slotNext)).displayName(playerKits.getLang().getString("menus.kitsMenu.next.nameItem")).build();
                inventory.setItem(slotNext, XPKUtils.applySimpleTag(next, "action", "next"));
            }
        }
    }

    private String getIcon(Player player, Kit kit, PlayerKitData playerKitData) {
        if (kit.isNoHasRequirements(player)) {
            return "CANT_CLAIM";
        }
        if (!kit.getPermission().equals("none") && player.hasPermission(kit.getPermission())) {
            return "NO_PERMISSION";
        }
        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        if (kit.isOneTime()) {
            if (kitData != null && kitData.isOneTime()) {
                return "ONE_TIME_CLAIMED";
            }
            return "ONE_TIME_REQUIREMENT";
        }
        if (kitData != null && kitData.getCountdown() > System.currentTimeMillis() && !player.hasPermission("xkits.countdown.bypass")) {
            return "COUNTDOWN";
        }
        return "CAN_CLAIM";
    }

}