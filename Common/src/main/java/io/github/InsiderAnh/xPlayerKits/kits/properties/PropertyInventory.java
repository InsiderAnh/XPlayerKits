package io.github.InsiderAnh.xPlayerKits.kits.properties;

import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import lombok.Data;

@Data
public class PropertyInventory {

    private boolean autoArmor;
    private int slot;
    private int page;
    private boolean checkInventorySpace;

    public PropertyInventory(InsiderConfig config) {
        if (config.isSet("inventory")) {
            this.autoArmor = config.getConfig().getBoolean("inventory.autoArmor");
            this.slot = config.getConfig().get("inventory.slot") instanceof Integer ? config.getInt("inventory.slot") : -1;
            this.page = config.getConfig().get("inventory.page") instanceof Integer ? config.getInt("inventory.page") : -1;
            this.checkInventorySpace = config.getConfig().getBoolean("inventory.checkInventorySpace");
        } else {
            this.autoArmor = config.getBoolean("autoArmor");
            this.slot = config.getConfig().get("slot") instanceof Integer ? config.getInt("slot") : -1;
            this.page = config.getConfig().get("page") instanceof Integer ? config.getInt("page") : -1;
            this.checkInventorySpace = config.getBoolean("checkInventorySpace");
        }
    }

    public PropertyInventory() {
        this.autoArmor = false;
        this.slot = -1;
        this.page = -1;
        this.checkInventorySpace = true;
    }

    public void save(InsiderConfig config) {
        config.set("autoArmor", null);
        config.set("slot", null);
        config.set("page", null);
        config.set("checkInventorySpace", null);

        config.set("inventory.autoArmor", autoArmor);
        config.set("inventory.slot", slot);
        config.set("inventory.page", page);
        config.set("inventory.checkInventorySpace", checkInventorySpace);
    }

}
