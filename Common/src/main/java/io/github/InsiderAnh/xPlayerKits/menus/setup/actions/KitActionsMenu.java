package io.github.InsiderAnh.xPlayerKits.menus.setup.actions;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
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

        if (nbtItem.hasTag("actionIndex")) {
            int actionIndex = nbtItem.getInteger("actionIndex");
            ArrayList<Execution> executions = actionType.equals("claim") ? kit.getActionsOnClaim() : kit.getActionsOnDeny();

            if (actionIndex >= 0 && actionIndex < executions.size()) {
                if (click == ClickType.LEFT) {
                    // Click izquierdo: Mostrar información
                    String actionInfo = executions.get(actionIndex).getAction();
                    player.sendMessage(playerKits.getLang().getString("messages.actionInfo")
                            .replace("<action>", actionInfo));
                    player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                } else if (click == ClickType.RIGHT) {
                    // Click derecho: Eliminar acción
                    executions.remove(actionIndex);
                    player.sendMessage(playerKits.getLang().getString("messages.actionRemoved"));
                    player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
                } else if (click == ClickType.SHIFT_LEFT) {
                    // Shift + Click izquierdo: Mover arriba
                    if (actionIndex > 0) {
                        Execution temp = executions.get(actionIndex);
                        executions.set(actionIndex, executions.get(actionIndex - 1));
                        executions.set(actionIndex - 1, temp);
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0f, 1.0f);
                        onUpdate(getInventory());
                    }
                } else if (click == ClickType.SHIFT_RIGHT) {
                    // Shift + Click derecho: Mover abajo
                    if (actionIndex < executions.size() - 1) {
                        Execution temp = executions.get(actionIndex);
                        executions.set(actionIndex, executions.get(actionIndex + 1));
                        executions.set(actionIndex + 1, temp);
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0f, 1.0f);
                        onUpdate(getInventory());
                    }
                }
            }
            return;
        }

        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            switch (action) {
                case "newAction":
                    new KitSelectActionsMenu(player, kit, actionType).open();
                    return;
                case "back":
                    new KitMainActionsMenu(player, kit).open();
                    return;
                case "close":
                    close();
                    return;
                case "clearAll":
                    ArrayList<Execution> executions = actionType.equals("claim") ? kit.getActionsOnClaim() : kit.getActionsOnDeny();
                    executions.clear();
                    player.sendMessage(playerKits.getLang().getString("messages.actionsCleared"));
                    player.playSound(player.getLocation(), XSound.ENTITY_GENERIC_EXPLODE.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        inventory.clear();

        ArrayList<Execution> executions = actionType.equals("claim") ? kit.getActionsOnClaim() : kit.getActionsOnDeny();

        AtomicInteger index = new AtomicInteger();
        for (int i = 0; i < executions.size(); i++) {
            Execution execution = executions.get(i);
            String actionTypeName = execution.getActionType();

            ItemStack itemStack = new ItemUtils(getMaterialForActionType(actionTypeName))
                    .displayName(playerKits.getLang().getString("menus.actions.actionItem.nameItem")
                            .replace("<type>", actionTypeName)
                            .replace("<index>", String.valueOf(i + 1)))
                    .lore(playerKits.getLang().getString("menus.actions.actionItem.loreItem")
                            .replace("<action>", execution.getAction())
                            .replace("<type>", actionTypeName))
                    .build();

            int slot = XPKUtils.SLOTS[index.getAndIncrement()];
            if (slot < 45) {
                inventory.setItem(slot, XPKUtils.applySimpleTag(itemStack, "actionIndex", i));
            }
        }

        ItemStack back = new ItemUtils(XMaterial.ARROW.get())
                .displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem"))
                .build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get())
                .displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem"))
                .build();
        ItemStack newAction = new ItemUtils(XMaterial.EMERALD.get())
                .displayName(playerKits.getLang().getString("menus.actions.newAction.nameItem"))
                .lore(playerKits.getLang().getString("menus.actions.newAction.loreItem")
                        .replace("<action>", actionType))
                .build();
        ItemStack clearAll = new ItemUtils(XMaterial.TNT.get())
                .displayName(playerKits.getLang().getString("menus.actions.clearAll.nameItem"))
                .lore(playerKits.getLang().getString("menus.actions.clearAll.loreItem"))
                .build();

        inventory.setItem(45, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(49, XPKUtils.applySimpleTag(close, "action", "close"));
        inventory.setItem(52, XPKUtils.applySimpleTag(clearAll, "action", "clearAll"));
        inventory.setItem(53, XPKUtils.applySimpleTag(newAction, "action", "newAction"));
    }

    private XMaterial getMaterialForActionType(String actionType) {
        switch (actionType.toLowerCase()) {
            case "player_command":
            case "console_command":
                return XMaterial.COMMAND_BLOCK;
            case "message":
            case "center_message":
            case "mini_message":
            case "action_bar":
                return XMaterial.WRITABLE_BOOK;
            case "broadcast":
                return XMaterial.BELL;
            case "sound":
            case "playsound_resource_pack":
                return XMaterial.NOTE_BLOCK;
            case "titles":
                return XMaterial.PAINTING;
            case "wait_ticks":
                return XMaterial.CLOCK;
            default:
                return XMaterial.PAPER;
        }
    }

}