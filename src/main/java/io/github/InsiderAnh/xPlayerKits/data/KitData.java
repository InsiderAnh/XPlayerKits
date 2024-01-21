package io.github.InsiderAnh.xPlayerKits.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data@AllArgsConstructor
public class KitData {

    private String kitName;
    private long countdown;
    private boolean oneTime;
    private boolean bought;

}