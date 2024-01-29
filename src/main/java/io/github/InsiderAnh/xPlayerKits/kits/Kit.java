package io.github.InsiderAnh.xPlayerKits.kits;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.utils.InventorySerializable;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Kit {

    private final ArrayList<String> requirements = new ArrayList<>();
    private final HashMap<String, ItemStack> icons = new HashMap<>();
    private final ArrayList<String> actionsOnClaim = new ArrayList<>();
    private final ArrayList<String> actionsOnDeny = new ArrayList<>();
    private String name;
    private long countdown;
    private boolean oneTime, autoArmor, preview, checkInventorySpace;
    private int slot, page;
    private double price;
    private String permission;
    private ItemStack[] armor, inventory;

    public Kit(String name) {
        this.name = name;
        this.countdown = TimeUnit.MINUTES.toSeconds(5);
        this.oneTime = false;
        this.autoArmor = false;
        this.preview = false;
        this.checkInventorySpace = false;
        this.slot = 10;
        this.page = 1;
        this.price = 0;
        this.permission = "none";
        this.armor = new ItemStack[]{};
        this.inventory = new ItemStack[]{};
        this.requirements.add("%vault_eco_balance% >= 0");
        this.actionsOnClaim.add("console:say Test allow commands.");
        this.actionsOnClaim.add("command:/test command");
        this.actionsOnClaim.add("sound:ENTITY_PLAYER_LEVELUP;1.0f;1.0f");
        this.actionsOnDeny.add("console:say Test allow commands.");
        this.actionsOnDeny.add("command:/test command");
        this.actionsOnDeny.add("sound:ENTITY_ENDERMAN_TELEPORT;1.0f;1.0f");
        this.icons.put("CAN_CLAIM", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim!").build());
        this.icons.put("CANT_CLAIM", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou can´t claim this kit!").build());
        this.icons.put("NO_PERMISSION", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou don´t have permission!").build());
        this.icons.put("COUNTDOWN", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cIn countdown!").build());
        this.icons.put("ONE_TIME_CLAIMED", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cAlready claimed!").build());
        this.icons.put("ONE_TIME_REQUIREMENT", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim one time!").build());
    }

    public Kit(@NotNull InsiderConfig config) {
        this.name = config.getString("name");
        this.countdown = config.getLong("countdown");
        this.oneTime = config.getBoolean("oneTime");
        this.autoArmor = config.getBoolean("autoArmor");
        this.preview = config.getBooleanOrDefault("preview", true);
        this.checkInventorySpace = config.getBooleanOrDefault("checkInventorySpace", true);
        this.slot = config.getInt("slot");
        this.page = config.getInt("page");
        this.price = config.getDouble("price");
        this.permission = config.getString("permission");
        this.requirements.addAll(config.getList("requirements"));
        this.actionsOnClaim.addAll(config.getList("actionsOnClaim"));
        this.actionsOnDeny.addAll(config.getList("actionsOnDeny"));
        this.armor = InventorySerializable.itemStackArrayFromBase64(config.getString("armor"));
        this.inventory = InventorySerializable.itemStackArrayFromBase64(config.getString("inventory"));
        if (config.isSet("icons")) {
            for (String key : config.getConfig().getConfigurationSection("icons").getKeys(false)) {
                this.icons.put(key, InventorySerializable.itemStackFromBase64(config.getConfig().getString("icons." + key)));
            }
        }
    }

    public void save() {
        InsiderConfig config = new InsiderConfig(PlayerKits.getInstance(), "kits/" + name, false, false);
        config.set("name", name);
        config.set("countdown", countdown);
        config.set("oneTime", oneTime);
        config.set("autoArmor", autoArmor);
        config.set("preview", preview);
        config.set("checkInventorySpace", checkInventorySpace);
        config.set("slot", slot);
        config.set("page", page);
        config.set("price", price);
        config.set("permission", permission);
        config.set("requirements", requirements);
        config.set("actionsOnClaim", actionsOnClaim);
        config.set("actionsOnDeny", actionsOnDeny);
        config.set("armor", InventorySerializable.itemStackArrayToBase64(armor));
        config.set("inventory", InventorySerializable.itemStackArrayToBase64(inventory));
        for (String key : icons.keySet()) {
            config.set("icons." + key, InventorySerializable.itemStackToBase64(icons.get(key)));
        }
        config.save();
    }

    public boolean isInventorySpace(Player player) {
        AtomicBoolean occupied = new AtomicBoolean(false);
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = playerInv.getItem(i);
            if (toItem != null && !toItem.getType().equals(Material.AIR)) {
                occupied.set(true);
            }
        }
        for (int i = 0; i < armor.length; i++) {
            ItemStack itemStack = armor[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = player.getInventory().getArmorContents()[i];
            if (toItem != null && !toItem.getType().equals(Material.AIR)) {
                occupied.set(true);
            }
        }
        return occupied.get();
    }

    public void giveKit(Player player) {
        XPKUtils.executeActions(player, actionsOnClaim);
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = playerInv.getItem(i);
            if (toItem == null || toItem.getType().equals(Material.AIR)) {
                playerInv.setItem(i, itemStack);
            }
        }
        for (int i = 0; i < armor.length; i++) {
            ItemStack itemStack = armor[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = player.getInventory().getArmorContents()[i];
            if (toItem == null || toItem.getType().equals(Material.AIR)) {
                if (XPKUtils.isHelmet(itemStack.getType().name())) {
                    player.getInventory().setHelmet(itemStack);
                }
                if (XPKUtils.isChestplate(itemStack.getType().name())) {
                    player.getInventory().setChestplate(itemStack);
                }
                if (XPKUtils.isLeggings(itemStack.getType().name())) {
                    player.getInventory().setLeggings(itemStack);
                }
                if (XPKUtils.isBoots(itemStack.getType().name())) {
                    player.getInventory().setBoots(itemStack);
                }
            }
        }
    }

    public boolean isNoHasRequirements(Player player) {
        for (String requirement : requirements) {
            if (requirement.equalsIgnoreCase("none")) continue;
            if (!XPKUtils.passCondition(player, requirement)) {
                return true;
            }
        }
        return false;
    }

    public String getRequirementsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String requirement : requirements) {
            stringBuilder.append(requirement).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getActionsOnClaimString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String command : actionsOnClaim) {
            stringBuilder.append(command).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getActionsOnDenyString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String command : actionsOnDeny) {
            stringBuilder.append(command).append("\n");
        }
        return stringBuilder.toString();
    }

}