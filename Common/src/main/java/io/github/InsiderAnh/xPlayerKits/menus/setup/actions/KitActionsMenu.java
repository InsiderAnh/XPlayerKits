package io.github.InsiderAnh.xPlayerKits.menus.setup.actions;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitActionsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;
    private final String actionType;

    public KitActionsMenu(Player player, Kit kit, String actionType) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.actions.title"));
        this.kit = kit;
        this.actionType = actionType;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            switch (action) {
                case "newAction":
                    new KitSelectActionsMenu(player).open();
                    return;
                case "back":
                    new KitMainActionsMenu(player, kit).open();
                    return;
                case "close":
                    close();
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        ArrayList<Execution> executions = actionType.equals("claim") ? kit.getActionsOnClaim() : kit.getActionsOnDeny();

        AtomicInteger index = new AtomicInteger();
        for (Execution execution : executions) {
            String action = execution.getActionType();
            ItemStack itemStack = new ItemUtils(XMaterial.PAPER.get())
                .displayName(playerKits.getLang().getString("menus.selectActions." + action + ".nameItem"))
                .lore(playerKits.getLang().getString("menus.selectActions." + action + ".loreItem")
                    .replace("<action>", action))
                .build();
            inventory.setItem(XPKUtils.SLOTS[index.getAndIncrement()], itemStack);
        }

        ItemStack back = new ItemUtils(XMaterial.ARROW.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem")).build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem")).build();
        ItemStack newAction = new ItemUtils(XMaterial.EMERALD.get()).displayName(playerKits.getLang().getString("menus.actions.newAction.nameItem")).lore(playerKits.getLang().getString("menus.actions.newAction.loreItem").replace("<action>", actionType)).build();

        inventory.setItem(45, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(49, XPKUtils.applySimpleTag(close, "action", "close"));
        inventory.setItem(53, XPKUtils.applySimpleTag(newAction, "action", "newAction"));
    }

}