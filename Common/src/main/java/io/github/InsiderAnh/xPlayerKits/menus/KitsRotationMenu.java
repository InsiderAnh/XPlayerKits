package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.MenuVarItem;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Consumer;

public class KitsRotationMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Menu menu;
    private final PlayerKitData playerKitData;
    private int page;

    public KitsRotationMenu(Player player, PlayerKitData playerKitData, int page) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getMenuManager().getTitle("rotative_kits", "Kits rotation"));
        this.menu = playerKits.getMenuManager().getMenu("rotative_kits");
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
            String action = nbtItem.getString("xpk-menu:item");
            if (action.equals("close")) {
                close();
            }
            if (action.equals("last_page")) {
                page = page + 1;
                onUpdate(getInventory());
            }
            if (action.equals("next_page")) {
                page = page - 1;
                onUpdate(getInventory());
            }
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
            MenuSlots menuSlots = menuItem.getSlots();

            ItemStack itemStack = menuItem.buildItem(player);
            for (int slot : menuSlots.getSlots()) {
                inventory.setItem(slot, itemStack);
            }
        }

        MenuVarItem menuVarItem = menu.getVarItems().get("kitSlots");
        if (menuVarItem != null) {
            MenuSlots menuSlots = menuVarItem.getSlots();
            int skipSlot = menuSlots.getPerPage() * (page - 1);
            Map<String, Kit> kits = playerKits.getKitManager().getSubMap(skipSlot, menuSlots.getPerPage());

            for (Map.Entry<String, Kit> entry : kits.entrySet()) {
                Kit kitSlot = entry.getValue();
                if (kitSlot == null) continue;

                buildAndSet(player, kitSlot.getPropertyInventory().getSlot(), kitSlot);
            }
        }
    }

    public void buildAndSet(Player player, int slot, Kit kit) {
        String state = getState(player, kit, playerKitData);
        ItemStack icon = kit.getIcons().get(state);
        getInventory().setItem(slot, XPKUtils.applySimpleTag(new ItemUtils(icon).displayName(playerKits.getLang().getString("menus.kitsMenu." + state + ".nameItem").replace("<name>", kit.getName())).build(), "kit", kit.getName()));
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