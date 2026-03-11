package io.github.InsiderAnh.xPlayerKits.menus;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuItem;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.actions.MenuAction;
import io.github.InsiderAnh.xPlayerKits.data.KitVotingData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.inventory.AInventory;
import io.github.InsiderAnh.xPlayerKits.inventory.InventorySizes;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class VotingMenu extends AInventory {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Menu menu;
    private final PlayerKitData playerKitData;

    public VotingMenu(Player player, PlayerKitData playerKitData) {
        super(player, PlayerKits.getInstance().getMenuManager().getInventorySizes("voting", InventorySizes.GENERIC_9X6),
                PlayerKits.getInstance().getMenuManager().getTitle("voting", "Kit Voting"));
        this.menu = playerKits.getMenuManager().getMenu("voting");
        this.playerKitData = playerKitData;
        onUpdate(getInventory());
    }

    @Override
    protected void onClick(InventoryClickEvent event, ItemStack currentItem, ClickType click, Consumer<Boolean> canceled) {
        canceled.accept(true);
        Player player = getPlayer();
        NBTItem nbtItem = new NBTItem(currentItem);

        if (nbtItem.hasTag("xpk-menu:item")) {
            String menuItemId = nbtItem.getString("xpk-menu:item");
            MenuItem menuItem = menu.getItems().get(menuItemId);
            if (menuItem == null) return;

            for (MenuAction action : menuItem.getActions()) {
                if (action.getAction().equals("close_menu")) {
                    close();
                }
            }

            playerKits.getExecutionManager().execute(player, menuItem.getExecutions(),
                    new Placeholder("<player>", player.getName()));
        }

        if (nbtItem.hasTag("voting-kit")) {
            String kitName = nbtItem.getString("voting-kit");
            boolean voted = playerKits.getVotingManager().vote(player, kitName);

            if (voted) {
                player.sendMessage(playerKits.getLang().getString("messages.votedSuccessfully")
                        .replace("<kit>", kitName));
                onUpdate(getInventory());
            } else {
                KitVotingData votingData = playerKits.getVotingManager().getVotingData(kitName);
                if (votingData != null && votingData.hasVoted(player.getUniqueId().toString())) {
                    player.sendMessage(playerKits.getLang().getString("messages.alreadyVoted"));
                } else {
                    player.sendMessage(playerKits.getLang().getString("messages.votingNotActive"));
                }
            }
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

        if (menu != null) {
            for (MenuItem menuItem : menu.getItems().values()) {
                MenuSlots menuSlots = menuItem.getSlots();
                ItemStack itemStack = menuItem.buildItem(player);
                for (int slot : menuSlots.getSlots()) {
                    inventory.setItem(slot, itemStack);
                }
            }
        }

        // Obtener votaciones activas
        List<KitVotingData> activeVotings = playerKits.getVotingManager().getActiveVotings();

        if (activeVotings.isEmpty()) {
            // Mostrar mensaje de no hay votaciones activas
            ItemStack noVotings = new ItemUtils(Material.BARRIER)
                    .displayName("§cNo Active Votings")
                    .lore("§7There are no active kit votings at the moment.")
                    .build();
            inventory.setItem(22, noVotings);
            return;
        }

        // Mostrar kits en votación
        int[] slots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
        int index = 0;

        for (KitVotingData votingData : activeVotings) {
            if (index >= slots.length) break;

            Kit kit = playerKits.getKitManager().getKit(votingData.getKitName());
            if (kit == null) continue;

            boolean hasVoted = votingData.hasVoted(player.getUniqueId().toString());
            ItemStack icon = kit.getIcons().get("CAN_CLAIM");
            if (icon == null) {
                icon = new ItemStack(Material.CHEST);
            }

            String loreText = "§7Total Votes: §a" + votingData.getTotalVotes() + "\n" +
                    "§7Your Status: " + (hasVoted ? "§aVoted" : "§7Not voted") + "\n" +
                    "\n" +
                    (hasVoted ? "§cYou already voted!" : "§eClick to vote!");

            ItemStack votingItem = new ItemUtils(icon.clone())
                    .displayName("§e" + kit.getName())
                    .lore(loreText)
                    .build();

            votingItem = XPKUtils.applySimpleTag(votingItem, "voting-kit", kit.getName());
            inventory.setItem(slots[index], votingItem);
            index++;
        }
    }

}

