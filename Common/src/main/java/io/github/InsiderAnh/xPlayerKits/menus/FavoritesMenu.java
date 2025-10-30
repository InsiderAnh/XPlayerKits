package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.actions.MenuAction;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class FavoritesMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Menu menu;
    private final PlayerKitData playerKitData;
    private int page;

    public FavoritesMenu(Player player, PlayerKitData playerKitData, int page) {
        super(player, PlayerKits.getInstance().getMenuManager().getInventorySizes("favorites", InventorySizes.GENERIC_9X6),
              PlayerKits.getInstance().getMenuManager().getTitle("favorites", "Favorite Kits"));
        this.menu = playerKits.getMenuManager().getMenu("favorites");
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
                    if (page > 1) {
                        page--;
                        onUpdate(getInventory());
                    }
                }
                if (action.getAction().equals("next_page")) {
                    page++;
                    onUpdate(getInventory());
                }
            }

            playerKits.getExecutionManager().execute(player, menuItem.getExecutions(),
                new Placeholder("<player>", player.getName()));
        }

        if (nbtItem.hasTag("kit")) {
            Kit kit = playerKits.getKitManager().getKit(nbtItem.getString("kit"));
            if (kit == null) return;

            if (click.isShiftClick()) {
                // Remover de favoritos
                playerKits.getFavoriteManager().removeFavorite(playerKitData, kit.getName());
                player.sendMessage(playerKits.getLang().getString("messages.removedFromFavorites")
                    .replace("<kit>", kit.getName()));
                onUpdate(getInventory());
                return;
            }

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

        if (menu != null) {
            for (MenuItem menuItem : menu.getItems().values()) {
                MenuSlots menuSlots = menuItem.getSlots();
                ItemStack itemStack = menuItem.buildItem(player);
                for (int slot : menuSlots.getSlots()) {
                    inventory.setItem(slot, itemStack);
                }
            }
        }

        // Obtener kits favoritos
        List<Kit> favoriteKits = playerKits.getFavoriteManager().getFavoriteKits(playerKitData);

        if (favoriteKits.isEmpty()) {
            // Mostrar mensaje de no hay favoritos
            ItemStack noFavorites = new ItemUtils(Material.BARRIER)
                .displayName("§cNo Favorite Kits")
                .lore("§7You haven't marked any kits as favorite yet.")
                .build();
            inventory.setItem(22, noFavorites);
            return;
        }

        // Mostrar kits favoritos
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int startIndex = (page - 1) * slots.length;
        int index = 0;

        for (int i = startIndex; i < favoriteKits.size() && index < slots.length; i++) {
            Kit kit = favoriteKits.get(i);
            if (kit == null) continue;

            buildAndSet(player, slots[index], kit);
            index++;
        }
    }

    public void buildAndSet(Player player, int slot, Kit kit) {
        String state = getState(player, kit, playerKitData);
        ItemStack icon = new ItemUtils(kit.getIcons().get(state))
            .addLore("", "§e⭐ Favorite", "", "§7Shift+Click to remove from favorites")
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

