package io.github.InsiderAnh.xPlayerKits.menus.setup.actions;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.menus.setup.KitEditorMenu;
import io.github.InsiderAnh.xPlayerKits.menus.setup.requirements.KitRequirementsMenu;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class KitMainActionsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;

    public KitMainActionsMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X4, PlayerKits.getInstance().getLang().getString("menus.mainActions.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("action")) {
            switch (nbtItem.getString("action")) {
                case "claimCommands":
                    new KitActionsMenu(player, kit, "claim").open();
                    return;
                case "denyCommands":
                    new KitActionsMenu(player, kit, "deny").open();
                    return;
                case "requirements":
                    new KitRequirementsMenu(player).open();
                    return;
                case "back":
                    new KitEditorMenu(player, kit).open();
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
        ItemStack requirements = new ItemUtils(XMaterial.BOOK.get()).displayName(playerKits.getLang().getString("menus.newKit.requirements.nameItem")).lore(playerKits.getLang().getString("menus.newKit.requirements.loreItem").replace("<requirements>", kit.getRequirementsString())).build();
        ItemStack claimCommands = new ItemUtils(XMaterial.GOLD_INGOT.get()).displayName(playerKits.getLang().getString("menus.newKit.claimCommands.nameItem")).lore(playerKits.getLang().getString("menus.newKit.claimCommands.loreItem").replace("<claimCommands>", kit.getActionsOnClaimString())).build();
        ItemStack denyCommands = new ItemUtils(XMaterial.REDSTONE.get()).displayName(playerKits.getLang().getString("menus.newKit.denyCommands.nameItem")).lore(playerKits.getLang().getString("menus.newKit.denyCommands.loreItem").replace("<denyCommands>", kit.getActionsOnDenyString())).build();

        ItemStack back = new ItemUtils(XMaterial.ARROW.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.back.nameItem")).build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get()).displayName(playerKits.getLang().getString("menus.kitsMenu.close.nameItem")).build();

        inventory.setItem(11, XPKUtils.applySimpleTag(requirements, "action", "requirements"));
        inventory.setItem(13, XPKUtils.applySimpleTag(claimCommands, "action", "claimCommands"));
        inventory.setItem(15, XPKUtils.applySimpleTag(denyCommands, "action", "denyCommands"));

        inventory.setItem(27, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(31, XPKUtils.applySimpleTag(close, "action", "close"));
    }

}