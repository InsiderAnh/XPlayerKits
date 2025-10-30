package io.github.InsiderAnh.xPlayerKits.menus.setup.requirements;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XMaterial;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
import io.github.InsiderAnh.xPlayerKits.menus.setup.actions.KitMainActionsMenu;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class KitRequirementsMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Kit kit;

    public KitRequirementsMenu(Player player, Kit kit) {
        super(player, InventorySizes.GENERIC_9X6, PlayerKits.getInstance().getLang().getString("menus.requirements.title"));
        this.kit = kit;
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);

        if (nbtItem.hasTag("requirementIndex")) {
            int requirementIndex = nbtItem.getInteger("requirementIndex");
            ArrayList<String> requirements = kit.getRequirements();

            if (requirementIndex >= 0 && requirementIndex < requirements.size()) {
                if (click == ClickType.LEFT) {
                    String requirement = requirements.get(requirementIndex);
                    player.sendMessage(playerKits.getLang().getString("messages.requirementInfo")
                        .replace("<requirement>", requirement));
                    player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0f, 1.0f);
                } else if (click == ClickType.RIGHT) {
                    requirements.remove(requirementIndex);
                    if (requirements.isEmpty()) {
                        requirements.add("none");
                    }
                    player.sendMessage(playerKits.getLang().getString("messages.requirementRemoved"));
                    player.playSound(player.getLocation(), XSound.ENTITY_ITEM_BREAK.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
                } else if (click == ClickType.SHIFT_LEFT) {
                    if (requirementIndex > 0) {
                        String temp = requirements.get(requirementIndex);
                        requirements.set(requirementIndex, requirements.get(requirementIndex - 1));
                        requirements.set(requirementIndex - 1, temp);
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0f, 1.0f);
                        onUpdate(getInventory());
                    }
                } else if (click == ClickType.SHIFT_RIGHT) {
                    if (requirementIndex < requirements.size() - 1) {
                        String temp = requirements.get(requirementIndex);
                        requirements.set(requirementIndex, requirements.get(requirementIndex + 1));
                        requirements.set(requirementIndex + 1, temp);
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
                case "newRequirement":
                    openRequirementInput(player);
                    return;
                case "clearAll":
                    kit.getRequirements().clear();
                    kit.getRequirements().add("none");
                    player.sendMessage(playerKits.getLang().getString("messages.requirementsCleared"));
                    player.playSound(player.getLocation(), XSound.ENTITY_GENERIC_EXPLODE.get(), 1.0f, 1.0f);
                    onUpdate(getInventory());
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

    private void openRequirementInput(Player player) {
        String title = playerKits.getLang().getString("menus.requirements.input.title");
        String defaultText = "%player_name% == Steve";

        new AnvilGUI.Builder()
            .plugin(playerKits)
            .title(XPKUtils.color(title))
            .text(defaultText)
            .onClick((index, completion) -> {
                String input = completion.getText();

                String[] parts = input.split(" ");
                if (parts.length < 3) {
                    player.sendMessage(playerKits.getLang().getString("messages.invalidRequirementFormat"));
                    player.playSound(player.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0f, 1.0f);
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                }

                ArrayList<String> requirements = kit.getRequirements();
                if (requirements.size() == 1 && requirements.get(0).equalsIgnoreCase("none")) {
                    requirements.clear();
                }

                requirements.add(input);
                player.sendMessage(playerKits.getLang().getString("messages.requirementAdded"));
                player.playSound(player.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0f, 1.0f);

                new KitRequirementsMenu(player, kit).open();
                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            })
            .open(player);
    }

    @Override
    protected void onUpdate(Inventory inventory) {
        inventory.clear();

        ArrayList<String> requirements = kit.getRequirements();
        Player player = getPlayer();

        AtomicInteger index = new AtomicInteger();
        for (int i = 0; i < requirements.size(); i++) {
            String requirement = requirements.get(i);

            Material material;
            String status;
            boolean passes;

            if (requirement.equalsIgnoreCase("none")) {
                material = Material.GRAY_DYE;
                status = "&7No requirement";
            } else {
                passes = XPKUtils.passCondition(player, requirement);
                material = passes ? Material.LIME_DYE : Material.RED_DYE;
                status = passes ? "&aPass ✓" : "&cFail ✗";

                String[] parts = requirement.split(" ");
                if (parts.length >= 1) {
                    String placeholder = parts[0];
                    String resolved = PlaceholderAPI.setPlaceholders(player, placeholder);
                    status += "\n&7Preview: &f" + resolved;
                }
            }

            ItemStack itemStack = new ItemUtils(material)
                .displayName(playerKits.getLang().getString("menus.requirements.requirement.nameItem")
                    .replace("<index>", String.valueOf(i + 1))
                    .replace("<status>", status))
                .lore(playerKits.getLang().getString("menus.requirements.requirement.loreItem")
                    .replace("<requirement>", requirement)
                    .replace("<status>", status))
                .build();

            int slot = XPKUtils.SLOTS[index.getAndIncrement()];
            if (slot < 45) {
                inventory.setItem(slot, XPKUtils.applySimpleTag(itemStack, "requirementIndex", i));
            }
        }

        ItemStack back = new ItemUtils(XMaterial.ARROW.get())
            .displayName(playerKits.getLang().getString("menus.mainKitEditor.back.nameItem"))
            .build();
        ItemStack close = new ItemUtils(XMaterial.BARRIER.get())
            .displayName(playerKits.getLang().getString("menus.mainKitEditor.close.nameItem"))
            .build();
        ItemStack newRequirement = new ItemUtils(XMaterial.EMERALD.get())
            .displayName(playerKits.getLang().getString("menus.requirements.newRequirement.nameItem"))
            .lore(playerKits.getLang().getString("menus.requirements.newRequirement.loreItem"))
            .build();
        ItemStack clearAll = new ItemUtils(XMaterial.TNT.get())
            .displayName(playerKits.getLang().getString("menus.requirements.clearAll.nameItem"))
            .lore(playerKits.getLang().getString("menus.requirements.clearAll.loreItem"))
            .build();

        inventory.setItem(45, XPKUtils.applySimpleTag(back, "action", "back"));
        inventory.setItem(49, XPKUtils.applySimpleTag(close, "action", "close"));
        inventory.setItem(52, XPKUtils.applySimpleTag(clearAll, "action", "clearAll"));
        inventory.setItem(53, XPKUtils.applySimpleTag(newRequirement, "action", "newRequirement"));
    }

}