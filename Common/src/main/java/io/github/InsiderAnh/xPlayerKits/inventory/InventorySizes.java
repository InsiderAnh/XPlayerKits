package io.github.InsiderAnh.xPlayerKits.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InventorySizes {

    GENERIC_9X1(1),
    GENERIC_9X2(2),
    GENERIC_9X3(3),
    GENERIC_9X4(4),
    GENERIC_9X5(5),
    GENERIC_9X6(6);

    private int size;

    public static InventorySizes fromInv(int size) {
        for (InventorySizes sizes : values()) {
            if (sizes.getSize() == size) {
                return sizes;
            }
        }
        return GENERIC_9X1;
    }

    public int toInv() {
        return size * 9;
    }

}
