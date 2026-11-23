/* Defines the vanilla equipment anchors and their Survival inventory positions. */
package com.trinketsstyledcurios.inventory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.Locale;

public enum VanillaAnchor {
    HEAD(5, 8, 8, EquipmentSlot.HEAD),
    CHEST(6, 8, 26, EquipmentSlot.CHEST),
    LEGS(7, 8, 44, EquipmentSlot.LEGS),
    FEET(8, 8, 62, EquipmentSlot.FEET),
    OFFHAND(45, 77, 62, EquipmentSlot.OFFHAND);

    private final int menuSlot;
    private final int x;
    private final int y;
    private final EquipmentSlot equipmentSlot;

    VanillaAnchor(int menuSlot, int x, int y, EquipmentSlot equipmentSlot) {
        this.menuSlot = menuSlot;
        this.x = x;
        this.y = y;
        this.equipmentSlot = equipmentSlot;
    }

    public int menuSlot() {
        return menuSlot;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public EquipmentSlot equipmentSlot() {
        return equipmentSlot;
    }

    public static VanillaAnchor byMenuSlot(int menuSlot) {
        for (VanillaAnchor anchor : values()) {
            if (anchor.menuSlot == menuSlot) {
                return anchor;
            }
        }
        return null;
    }

    public static VanillaAnchor byName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
