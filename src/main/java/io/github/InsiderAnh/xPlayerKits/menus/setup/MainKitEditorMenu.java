package io.github.InsiderAnh.xPlayerKits.menus.setup;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
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

public class MainKitEditorMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private int page;
    private int passedSlots = 0;

    public MainKitEditorMenu(Player player, int page) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.mainKitEditor.title"));
        this.page = page;
        this.passedSlots = page * 21;
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
            if (action.equals("last")) {
                new MainKitEditorMenu(player, page - 1).open();
            }
            if (action.equals("next")) {
                new MainKitEditorMenu(player, page + 1).open();
            }
            if (action.equals("close")) {
                close();
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
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();
        for (Kit kit : playerKits.getKitManager().getKits().values()) {
            if (counter.getAndIncrement() < passedSlots || index.get() >= 21) {
                continue;
            }
            ItemStack kitItem = new ItemUtils(kit.getIcons().getOrDefault("CAN_CLAIM", new ItemStack(Material.PAPER))).displayName(playerKits.getLang().getString("menus.mainKitEditor.kit.nameItem").replace("<name>", kit.getName())).lore(playerKits.getLang().getString("menus.mainKitEditor.kit.loreItem")).build();
            inventory.setItem(XPKUtils.SLOTS[index.getAndIncrement()], XPKUtils.applySimpleTag(kitItem, "kit", kit.getName()));
        }
        ItemStack close = new ItemUtils(XMaterial.BARRIER.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.close.nameItem")).lore(playerKits.getLang().getString("menus.mainKitEditor.close.loreItem")).build();
        ItemStack newKit = new ItemUtils(XMaterial.EMERALD.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.newKit.nameItem")).lore(playerKits.getLang().getString("menus.mainKitEditor.newKit.loreItem")).build();
        ItemStack lastPage = new ItemUtils(XMaterial.ARROW.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.last.nameItem")).lore(playerKits.getLang().getString("menus.mainKitEditor.last.loreItem")).build();
        ItemStack nextPage = new ItemUtils(XMaterial.ARROW.parseMaterial()).displayName(playerKits.getLang().getString("menus.mainKitEditor.next.nameItem")).lore(playerKits.getLang().getString("menus.mainKitEditor.next.loreItem")).build();
        inventory.setItem(44, XPKUtils.applySimpleTag(lastPage, "action", "last"));
        inventory.setItem(45, XPKUtils.applySimpleTag(close, "action", "close"));
        inventory.setItem(50, XPKUtils.applySimpleTag(newKit, "action", "newKit"));
        inventory.setItem(52, XPKUtils.applySimpleTag(nextPage, "action", "next"));
    }

}