package io.github.InsiderAnh.xPlayerKits.kits;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.items.ItemSerializer;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyInventory;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyTiming;
import io.github.InsiderAnh.xPlayerKits.managers.ExecutionManager;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.InventorySerializable;
import io.github.InsiderAnh.xPlayerKits.utils.ItemUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class Kit {

    private final ArrayList<String> requirements = new ArrayList<>();
    private final HashMap<String, ItemStack> icons = new HashMap<>();
    private final ArrayList<Execution> actionsOnClaim = new ArrayList<>();
    private final ArrayList<Execution> actionsOnDeny = new ArrayList<>();
    private String name;
    private PropertyTiming propertyTiming;
    private PropertyInventory propertyInventory;
    private boolean preview;
    private double price;
    private String permission;
    private ItemStack[] armor = new ItemStack[10];
    private ItemStack[] inventory = new ItemStack[50];
    private ItemStack offhand;

    public Kit(String name, int slot) {
        this.name = name;
        this.propertyTiming = new PropertyTiming();
        this.propertyInventory = new PropertyInventory();
        this.propertyInventory.setSlot(slot);
        this.preview = false;
        this.price = 0;
        this.permission = "none";
        this.armor = new ItemStack[10];
        this.inventory = new ItemStack[50];
        this.offhand = null;
        this.requirements.add("none");

        ExecutionManager executionManager = PlayerKits.getInstance().getExecutionManager();

        this.actionsOnClaim.add(executionManager.getExecution("console:say Test allow commands."));
        this.actionsOnClaim.add(executionManager.getExecution("command:/test command"));
        this.actionsOnClaim.add(executionManager.getExecution("sound:ENTITY_PLAYER_LEVELUP;1.0f;1.0f"));
        this.actionsOnDeny.add(executionManager.getExecution("console:say Test allow commands."));
        this.actionsOnDeny.add(executionManager.getExecution("command:/test command"));
        this.actionsOnDeny.add(executionManager.getExecution("sound:ENTITY_ENDERMAN_TELEPORT;1.0f;1.0f"));
        this.icons.put("CAN_CLAIM", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim!").build());
        this.icons.put("CANT_CLAIM", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou can´t claim this kit!").build());
        this.icons.put("NO_PERMISSION", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou don´t have permission!").build());
        this.icons.put("COUNTDOWN", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cIn countdown!").build());
        this.icons.put("ONE_TIME_CLAIMED", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cAlready claimed!").build());
        this.icons.put("ONE_TIME_REQUIREMENT", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim one time!").build());
    }

    public Kit(@NotNull InsiderConfig config) {
        this.name = config.getString("name");
        this.propertyTiming = new PropertyTiming(config);
        this.propertyInventory = new PropertyInventory(config);
        this.preview = config.getBooleanOrDefault("preview", true);
        this.price = config.getDouble("price");
        this.permission = config.getString("permission");
        this.requirements.addAll(config.getList("requirements"));

        for (String action : config.getList("actionsOnClaim")) {
            this.actionsOnClaim.add(PlayerKits.getInstance().getExecutionManager().getExecution(action));
        }
        for (String action : config.getList("actionsOnDeny")) {
            this.actionsOnDeny.add(PlayerKits.getInstance().getExecutionManager().getExecution(action));
        }

        if (config.isSet("armor")) {
            this.armor = InventorySerializable.itemStackArrayFromBase64(config.getString("armor"));
            this.inventory = InventorySerializable.itemStackArrayFromBase64(config.getString("inventory"));
            if (config.isSet("icons")) {
                for (String key : config.getConfig().getConfigurationSection("icons").getKeys(false)) {
                    this.icons.put(key, InventorySerializable.itemStackFromBase64(config.getConfig().getString("icons." + key)));
                }
            }
            save();
        }

        if (config.isSet("armorSet")) {
            for (String key : config.getConfig().getConfigurationSection("armorSet").getKeys(false)) {
                this.armor[Integer.parseInt(key)] = config.getConfig().getItemStack("armorSet." + key);
            }
            if (config.isSet("inventorySet")) {
                for (String key : config.getConfig().getConfigurationSection("inventorySet").getKeys(false)) {
                    this.inventory[Integer.parseInt(key)] = config.getConfig().getItemStack("inventorySet." + key);
                }
            }
            if (config.isSet("iconSet")) {
                for (String key : config.getConfig().getConfigurationSection("iconSet").getKeys(false)) {
                    this.icons.put(key, config.getConfig().getItemStack("iconSet." + key));
                }
            }
            if (config.isSet("offhand")) {
                this.offhand = config.getConfig().getItemStack("offhand");
            }

            save();
            PlayerKits.getInstance().sendDebugMessage("Kit " + name + " migrated correctly.");
        }

        if (config.isSet("playerArmor")) {
            for (String key : config.getConfig().getConfigurationSection("playerArmor").getKeys(false)) {
                HashMap<String, Object> data = new HashMap<>(config.getConfig().getConfigurationSection("playerArmor." + key).getValues(false));
                this.armor[Integer.parseInt(key)] = ItemSerializer.deserialize(data);
            }
        }
        if (config.isSet("playerInventory")) {
            for (String key : config.getConfig().getConfigurationSection("playerInventory").getKeys(false)) {
                HashMap<String, Object> data = new HashMap<>(config.getConfig().getConfigurationSection("playerInventory." + key).getValues(false));
                this.inventory[Integer.parseInt(key)] = ItemSerializer.deserialize(data);
            }
        }
        if (config.isSet("kitIcons")) {
            for (String key : config.getConfig().getConfigurationSection("kitIcons").getKeys(false)) {
                HashMap<String, Object> data = new HashMap<>(config.getConfig().getConfigurationSection("kitIcons." + key).getValues(false));
                this.icons.put(key, ItemSerializer.deserialize(data));
            }
        }
        if (config.isSet("playerOffhand")) {
            HashMap<String, Object> data = new HashMap<>(config.getConfig().getConfigurationSection("playerOffhand").getValues(false));
            this.offhand = ItemSerializer.deserialize(data);
        }
        this.icons.put("CAN_CLAIM", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim!").build());
        this.icons.put("CANT_CLAIM", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou can´t claim this kit!").build());
        this.icons.put("NO_PERMISSION", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cYou don´t have permission!").build());
        this.icons.put("COUNTDOWN", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cIn countdown!").build());
        this.icons.put("ONE_TIME_CLAIMED", new ItemUtils(Material.STONE).displayName("§cKit test").lore("§7You can´t claim this kit.\n\n§cAlready claimed!").build());
        this.icons.put("ONE_TIME_REQUIREMENT", new ItemUtils(Material.STONE).displayName("§aKit test").lore("§7You can claim this kit.\n\n§eClick to claim one time!").build());
    }

    public void setIcon(String icon, ItemStack itemStack) {
        ItemStack item = itemStack.clone();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;
        if (!itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName("§aKit test");
        }
        if (!itemMeta.hasLore()) {
            itemMeta.setLore(Arrays.asList("§7You can claim this kit.\n\n§cThis is a example lore".split("\n")));
        }
        item.setItemMeta(itemMeta);
        this.icons.put(icon, item);
    }

    public void save() {
        InsiderConfig config = new InsiderConfig(PlayerKits.getInstance(), "kits/" + name, false, false);
        propertyTiming.save(config);
        propertyInventory.save(config);

        config.set("playerArmor", null);
        config.set("playerInventory", null);
        config.set("kitIcons", null);
        config.set("playerOffhand", null);

        config.set("name", name);
        config.set("preview", preview);
        config.set("price", price);
        config.set("permission", permission);
        config.set("requirements", requirements);
        config.set("actionsOnClaim", actionsOnClaim);
        config.set("actionsOnDeny", actionsOnDeny);
        if (offhand != null) {
            ItemSerializer.serialize(offhand, config.getConfig(), "playerOffhand");
        }
        for (int i = 0; i < armor.length; i++) {
            ItemStack itemStack = armor[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemSerializer.serialize(itemStack, config.getConfig(), "playerArmor." + i);
        }
        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemSerializer.serialize(itemStack, config.getConfig(), "playerInventory." + i);
        }
        for (Map.Entry<String, ItemStack> entry : icons.entrySet()) {
            String key = entry.getKey();
            ItemStack itemStack = entry.getValue();
            ItemSerializer.serialize(itemStack, config.getConfig(), "kitIcons." + key);
        }
        config.set("armor", null);
        config.set("inventory", null);
        config.set("icons", null);
        config.set("offhand", null);
        config.set("iconsSet", null);
        config.set("armorSet", null);
        config.set("inventorySet", null);
        config.save();
    }

    public boolean isNoInventorySpace(Player player) {
        AtomicBoolean occupied = new AtomicBoolean(false);
        Inventory playerInv = player.getInventory();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = playerInv.getItem(i);
            if (toItem != null) {
                if (toItem.getType().name().equals("AIR") || toItem.getType().name().equals("VOID_AIR") || toItem.getType().name().equals("CAVE_AIR"))
                    continue;
                occupied.set(true);
            }
        }
        for (int i = 0; i < armor.length; i++) {
            ItemStack itemStack = armor[i];
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            ItemStack toItem = player.getInventory().getArmorContents()[i];
            if (toItem != null) {
                if (toItem.getType().name().equals("AIR") || toItem.getType().name().equals("VOID_AIR") || toItem.getType().name().equals("CAVE_AIR"))
                    continue;
                occupied.set(true);
            }
        }
        return occupied.get();
    }

    public void giveKit(Player player) {
        PlayerKits.getInstance().getExecutionManager().execute(player, actionsOnClaim, new Placeholder("<player>", player.getName()));

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
        for (Execution execution : actionsOnClaim) {
            stringBuilder.append(execution.getAction()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getActionsOnDenyString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Execution execution : actionsOnDeny) {
            stringBuilder.append(execution.getAction()).append("\n");
        }
        return stringBuilder.toString();
    }

}