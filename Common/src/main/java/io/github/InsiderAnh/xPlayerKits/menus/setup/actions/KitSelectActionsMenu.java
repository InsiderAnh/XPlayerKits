package io.github.InsiderAnh.xPlayerKits.menus.setup.actions;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitSelectActionsMenu extends AInventory {

    private static final String[] availableActions = {"player_command", "console_command", "message", "center_message", "mini_message", "action_bar", "broadcast", "sound", "playsound_resource_pack", "titles", "wait_ticks"};
    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;
    private final String actionType;

    public KitSelectActionsMenu(Player player, Kit kit, String actionType) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.selectActions.title"));
        this.kit = kit;
        this.actionType = actionType;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);

        if (nbtItem.hasTag("actionType")) {
            String selectedAction = nbtItem.getString("actionType");
            handleActionInput(player, selectedAction);
            return;
        }

        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            if (action.equals("back")) {
                new KitActionsMenu(player, kit, actionType).open();
            } else if (action.equals("close")) {
                close();
            }
        }
    }

    private void handleActionInput(Player player, String actionType) {
        String title = playerKits.getLang().getString("menus.selectActions." + actionType + ".inputTitle");
        String defaultText = getDefaultText(actionType);

        new AnvilGUI.Builder()
            .plugin(playerKits)
            .title(XPKUtils.color(title))
            .text(defaultText)
            .onClick((index, completion) -> {
                String input = completion.getText();
                String fullAction = buildAction(actionType, input);

                if (this.actionType.equals("claim")) {
                    kit.getActionsOnClaim().add(playerKits.getExecutionManager().getExecution(fullAction));
                } else {
                    kit.getActionsOnDeny().add(playerKits.getExecutionManager().getExecution(fullAction));
                }

                player.sendMessage(playerKits.getLang().getString("messages.actionAdded"));
                player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0f, 1.0f);

                new KitActionsMenu(player, kit, this.actionType).open();
                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            })
            .open(player);
    }

    private String getDefaultText(String actionType) {
        switch (actionType) {
            case "player_command":
                return "/help";
            case "console_command":
                return "say Hello <player>";
            case "message":
            case "center_message":
            case "mini_message":
            case "action_bar":
                return "Hello <player>!";
            case "broadcast":
                return "<player> claimed a kit!";
            case "sound":
                return "ENTITY_PLAYER_LEVELUP;1.0;1.0";
            case "playsound_resource_pack":
                return "custom.sound;1.0;1.0";
            case "titles":
                return "Title;Subtitle;10;70;20";
            case "wait_ticks":
                return "20";
            default:
                return "";
        }
    }

    private String buildAction(String actionType, String input) {
        switch (actionType) {
            case "player_command":
                return "command:" + input;
            case "console_command":
                return "console:" + input;
            case "message":
                return "message:" + input;
            case "center_message":
                return "center_message:" + input;
            case "mini_message":
                return "mini_message:" + input;
            case "action_bar":
                return "action_bar:" + input;
            case "broadcast":
                return "broadcast:" + input;
            case "sound":
                return "sound:" + input;
            case "playsound_resource_pack":
                return "playsound_resource_pack:" + input;
            case "titles":
                return "titles:" + input;
            case "wait_ticks":
                return "wait_ticks:" + input;
            default:
                return input;
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        AtomicInteger index = new AtomicInteger();
        for (String action : availableActions) {
            ItemStack itemStack = new ItemUtils(Material.PAPER)
                .displayName(playerKits.getLang().getString("menus.selectActions." + action + ".nameItem"))
                .lore(playerKits.getLang().getString("menus.selectActions." + action + ".loreItem").replace("<action>", action))
                .build();

            inventory.setItem(XPKUtils.SLOTS[index.getAndIncrement()], XPKUtils.applySimpleTag(itemStack, "actionType", action));
        }

        ItemStack back = new ItemUtils(XMaterial.ARROW.get())
            .displayName(playerKits.getLang().getString("menus.mainKitEditor.back.nameItem"))
            .build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get())
            .displayName(playerKits.getLang().getString("menus.mainKitEditor.close.nameItem"))
            .build();

        inventory.setItem(45, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(49, XPKUtils.applySimpleTag(close, "action", "close"));
    }

}