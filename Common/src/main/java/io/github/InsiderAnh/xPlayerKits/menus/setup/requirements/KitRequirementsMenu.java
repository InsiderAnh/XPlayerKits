package io.github.InsiderAnh.xPlayerKits.menus.setup.requirements;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class KitRequirementsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    public KitRequirementsMenu(Player player) {
        super(player, InventorySizes.GENERIC_9X4, PlayerKits.getInstance().getLang().getString("menus.actions.title"));
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {

    }

    @Override
    protected void onUpdate(Inventory inventory) {

    }

}