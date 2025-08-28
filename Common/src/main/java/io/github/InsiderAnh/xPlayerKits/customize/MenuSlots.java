package io.github.InsiderAnh.xPlayerKits.customize;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedList;

@Getter
public class MenuSlots {

    private final LinkedList<Integer> slots = new LinkedList<>();
    private int perPage = 1;

    public MenuSlots(YamlConfiguration configuration, String path) {
        Object configSlots = configuration.get(path);
        if (configSlots instanceof String) {
            String[] split = ((String) configSlots).split(";");
            if (split.length == 0) return;

            for (String slot : split) {
                String[] slotSplit = slot.split("-");
                if (slotSplit.length == 0) continue;

                int start = Integer.parseInt(slotSplit[0]);
                int end = slotSplit.length >= 2 ? Integer.parseInt(slotSplit[1]) : start;

                for (int i = start; i <= end; i++) {
                    this.slots.add(i);
                }
            }
        } else if (configSlots instanceof Integer) {
            this.slots.add((int) configSlots);
        }
        this.perPage = this.slots.size();
    }

    @Override
    public String toString() {
        return "MenuSlots{" +
            "slots=" + slots +
            '}';
    }

    public int getSlot(int index) {
        if (index >= slots.size()) return 0;
        return slots.get(index);
    }

}