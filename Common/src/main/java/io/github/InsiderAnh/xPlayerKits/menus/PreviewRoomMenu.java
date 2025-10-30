package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.actions.MenuAction;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PreviewRoomMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Menu menu;
    private int page;

    public PreviewRoomMenu(Player player, int page) {
        super(player, PlayerKits.getInstance().getMenuManager().getInventorySizes("preview_room", InventorySizes.GENERIC_9X6),
              PlayerKits.getInstance().getMenuManager().getTitle("preview_room", "Kit Preview Room"));
        this.menu = playerKits.getMenuManager().getMenu("preview_room");
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

        if (nbtItem.hasTag("preview-kit")) {
            Kit kit = playerKits.getKitManager().getKit(nbtItem.getString("preview-kit"));
            if (kit == null) return;

            new KitPreviewMenu(player, kit).open();
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

        // Obtener todos los kits con preview habilitado
        List<Kit> previewableKits = new ArrayList<>();
        for (Kit kit : playerKits.getKitManager().getKits().values()) {
            if (kit != null && kit.isPreview()) {
                previewableKits.add(kit);
            }
        }

        if (previewableKits.isEmpty()) {
            // Mostrar mensaje de no hay kits disponibles
            ItemStack noKits = new ItemUtils(Material.BARRIER)
                .displayName("§cNo Previewable Kits")
                .lore("§7There are no kits available for preview.")
                .build();
            inventory.setItem(22, noKits);
            return;
        }

        // Mostrar kits disponibles para preview
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int startIndex = (page - 1) * slots.length;
        int index = 0;

        for (int i = startIndex; i < previewableKits.size() && index < slots.length; i++) {
            Kit kit = previewableKits.get(i);
            if (kit == null) continue;

            ItemStack icon = kit.getIcons().get("CAN_CLAIM");
            if (icon == null) {
                icon = new ItemStack(Material.CHEST);
            }

            ItemUtils itemBuilder = new ItemUtils(icon.clone())
                .displayName("§e" + kit.getName())
                .addLore("", "§7Click to preview this kit!");

            ItemStack previewItem = XPKUtils.applySimpleTag(itemBuilder.build(), "preview-kit", kit.getName());
            inventory.setItem(slots[index], previewItem);
            index++;
        }
    }

}

