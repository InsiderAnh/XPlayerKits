package io.github.InsiderAnh.xPlayerKits.menus;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.MenuVarItem;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class KitPreviewMenu extends AInventory {

    private final Kit kit;

    public KitPreviewMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.kitPreview.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
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
        Menu menu = PlayerKits.getInstance().getMenuManager().getMenu("preview");
        if (menu == null) return;

        Player player = getPlayer();

        MenuVarItem menuVarItem = menu.getVarItems().get("inventoryItem");
        if (menuVarItem != null) {
            for (int i = 0; i < kit.getInventory().length; i++) {
                MenuSlots menuSlots = menuVarItem.getSlots();
                if (menuSlots == null) continue;

                ItemStack itemStack = kit.getInventory()[i];
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;

                inventory.setItem(menuSlots.getSlot(i), itemStack);
            }
        }

        MenuVarItem menuVarArmorItem = menu.getVarItems().get("armorItem");
        if (menuVarArmorItem != null) {
            for (int i = 0; i < kit.getArmor().length; i++) {
                MenuSlots menuSlots = menuVarArmorItem.getSlots();
                if (menuSlots == null) continue;

                ItemStack itemStack = kit.getArmor()[i];
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;

                inventory.setItem(menuSlots.getSlot(i), itemStack);
            }
        }

        MenuVarItem menuVarOffhandItem = menu.getVarItems().get("offhandItem");
        if (menuVarOffhandItem != null) {
            ItemStack itemStack = kit.getOffhand();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) return;

            inventory.setItem(menuVarOffhandItem.getSlots().getSlot(0), itemStack);
        }

        for (MenuItem menuItem : menu.getItems().values()) {
            MenuSlots menuSlots = menuItem.getSlots();
            if (menuSlots == null) continue;

            ItemStack itemStack = menuItem.buildItem(player);
            for (int slot : menuSlots.getSlots()) {
                inventory.setItem(slot, itemStack);
            }
        }
    }

}