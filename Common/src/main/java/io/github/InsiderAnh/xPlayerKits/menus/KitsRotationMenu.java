package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.MenuVarItem;
import io.github.InsiderAnh.xPlayerKits.customize.actions.MenuAction;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class KitsRotationMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Menu menu;
    private final PlayerKitData playerKitData;
    private int page;

    public KitsRotationMenu(Player player, PlayerKitData playerKitData, int page) {
        super(player, PlayerKits.getInstance().getMenuManager().getInventorySizes("rotation_kits", InventorySizes.GENERIC_9X6), PlayerKits.getInstance().getMenuManager().getTitle("rotation_kits", "Kits rotation"));
        this.menu = playerKits.getMenuManager().getMenu("rotation_kits");
        this.playerKitData = playerKitData;
        this.page = page;
        onUpdate(getInventory());
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("xpk-menu:item")) {
            String menuItemId = nbtItem.getString("xpk-menu:item");
            MenuItem menuItem = menu.getItems().get(menuItemId);
            if (menuItem == null) return;

            for (MenuAction action : menuItem.getActions()) {
                if (action.getAction().equals("close_menu")) {
                    close();
                }
                if (action.getAction().equals("last_page")) {
                    page = page + 1;
                    onUpdate(getInventory());
                }
                if (action.getAction().equals("next_page")) {
                    page = page - 1;
                    onUpdate(getInventory());
                }
            }

            playerKits.getExecutionManager().execute(player, menuItem.getExecutions(), new Placeholder("<player>", player.getName()));
        }
        if (nbtItem.hasTag("kit")) {
            Kit kit = playerKits.getKitManager().getKit(nbtItem.getString("kit"));
            if (kit == null) return;

            if (click.isRightClick() && kit.isPreview()) {
                new KitPreviewMenu(player, kit).open();
                return;
            }

            close();
            XPKUtils.claimKit(player, kit, playerKitData);
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
        for (MenuItem menuItem : menu.getItems().values()) {
            if (menu.getLastPageItems().contains(menuItem.getItemId()) && page <= 1) continue;
            if (menu.getNextPageItems().contains(menuItem.getItemId()) && page >= playerKits.getKitManager().getLastPage())
                continue;

            MenuSlots menuSlots = menuItem.getSlots();

            ItemStack itemStack = menuItem.buildItem(player);
            for (int slot : menuSlots.getSlots()) {
                inventory.setItem(slot, itemStack);
            }
        }

        // Obtener kits en rotación activa
        List<Kit> rotationKits = playerKits.getRotationManager().getActiveRotationKits();

        if (rotationKits.isEmpty()) {
            // Mostrar mensaje de no hay kits en rotación
            MenuVarItem menuVarItem = menu.getVarItems().get("rotationSlots");
            if (menuVarItem != null) {
                MenuSlots menuSlots = menuVarItem.getSlots();
                List<Integer> slots = menuSlots.getSlots();
                if (!slots.isEmpty()) {
                    // Mostrar ítems de countdown en todos los slots
                    MenuItem countdownItem = menu.getItems().get("countdown");
                    if (countdownItem != null) {
                        for (int slot : slots) {
                            ItemStack itemStack = countdownItem.buildItem(player);
                            inventory.setItem(slot, itemStack);
                        }
                    }
                }
            }
            return;
        }

        MenuVarItem menuVarItem = menu.getVarItems().get("rotationSlots");
        if (menuVarItem != null) {
            MenuSlots menuSlots = menuVarItem.getSlots();
            List<Integer> slots = menuSlots.getSlots();

            for (int i = 0; i < Math.min(rotationKits.size(), slots.size()); i++) {
                Kit kit = rotationKits.get(i);
                if (kit == null) continue;

                buildAndSet(player, slots.get(i), kit);
            }

            // Llenar slots vacíos con ítems de countdown si hay menos kits que slots
            if (rotationKits.size() < slots.size()) {
                MenuItem countdownItem = menu.getItems().get("countdown");
                if (countdownItem != null) {
                    for (int i = rotationKits.size(); i < slots.size(); i++) {
                        ItemStack itemStack = countdownItem.buildItem(player);
                        inventory.setItem(slots.get(i), itemStack);
                    }
                }
            }
        }
    }

    public void buildAndSet(Player player, int slot, Kit kit) {
        String state = getState(player, kit, playerKitData);
        ItemStack icon = new ItemUtils(kit.getIcons().get(state))
                .build();
        getInventory().setItem(slot, XPKUtils.applySimpleTag(icon, "kit", kit.getName()));
    }

    private String getState(Player player, Kit kit, PlayerKitData playerKitData) {
        if (kit.isNoHasRequirements(player)) {
            return "CANT_CLAIM";
        }
        if (!kit.getPermission().equals("none") && !player.hasPermission(kit.getPermission())) {
            return "NO_PERMISSION";
        }
        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        if (kit.getPropertyTiming().isOneTime()) {
            if (kitData != null && kitData.isOneTime() && !player.hasPermission("xkits.onetime.bypass")) {
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