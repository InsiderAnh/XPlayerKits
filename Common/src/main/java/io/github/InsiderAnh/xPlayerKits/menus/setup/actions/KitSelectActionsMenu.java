package io.github.InsiderAnh.xPlayerKits.menus.setup.actions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitSelectActionsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private static final String[] availableActions = {"player_command", "console_command", "message", "center_message", "mini_message", "action_bar", "broadcast", "sound", "playsound_resource_pack", "titles", "wait_ticks"};

    public KitSelectActionsMenu(Player player) {
        super(player, InventorySizes.GENERIC_9X5, PlayerKits.getInstance().getLang().getString("menus.selectActions.title"));
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        AtomicInteger index = new AtomicInteger();
        for (String action : availableActions) {
            ItemStack itemStack = new ItemUtils(Material.PAPER)
                .displayName(playerKits.getLang().getString("menus.selectActions." + action + ".nameItem"))
                .lore(playerKits.getLang().getString("menus.selectActions." + action + ".loreItem").replace("<action>", action))
                .build();

            inventory.setItem(XPKUtils.SLOTS[index.getAndIncrement()], itemStack);
        }
    }

}