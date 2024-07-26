package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

public class MainKitEditorMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    public MainKitEditorMenu(Player player) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.mainKitEditor.title"));
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);
        if (nbtItem.hasTag("action")) {
            String action = nbtItem.getString("action");
            if (action.equals("newKit")) {
                new AnvilGUI.Builder()
                    .plugin(playerKits)
                    .onClick((slot, type) -> {
                        if (type.getText().length() > 36) {
                            player.sendMessage(playerKits.getLang().getString("messages.longName"));
                            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("§cLong name"));
                        }
                        String line = type.getText();
                        Kit kit = new Kit(line);
                        playerKits.getKitManager().addKit(kit);
                        new KitEditorMenu(player, kit).open();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    })
                    .text("Write a name")
                    .title("Write a kit name")
                    .open(player);
            }
        }
        if (nbtItem.hasTag("kit")) {
            Kit kit = playerKits.getKitManager().getKits().get(nbtItem.getString("kit"));
            if (kit == null) {
                player.sendMessage(playerKits.getLang().getString("messages.noExistsKit"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0f, 1.0f);
                return;
            }
            player.playSound(player.getLocation(), XSound.UI_BUTTON_CLICK.parseSound(), 1.0f, 1.0f);
            new KitEditorMenu(player, kit).open();
        }
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        for (Kit kit : playerKits.getKitManager().getKits().values()) {
            ItemStack kitItem = new ItemUtils(XMaterial.PAPER.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.kit.nameItem").replace("<name>", kit.getName())).lore(playerKits.getLang().getString("menus.mainKitEditor.kit.loreItem")).build();
            inventory.addItem(XPKUtils.applySimpleTag(kitItem, "kit", kit.getName()));
        }
        ItemStack newKit = new ItemUtils(XMaterial.EMERALD.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.newKit.nameItem")).lore(playerKits.getLang().getString("menus.mainKitEditor.newKit.loreItem")).build();
        inventory.setItem(53, XPKUtils.applySimpleTag(newKit, "action", "newKit"));
    }

}