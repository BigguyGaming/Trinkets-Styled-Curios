/* Resolves display slots and Creative wrappers to their underlying inventory slots. */
package com.trinketsstyledcurios.client;

import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import com.trinketsstyledcurios.mixin.CreativeSlotWrapperAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;

public final class DisplayedSlot {
    private DisplayedSlot() {
    }

    public static Slot target(Slot displayed) {
        // Creative wraps player slots, while Survival exposes the original slots directly.
        if ((Object) displayed instanceof CreativeSlotWrapperAccessor wrapper) {
            return wrapper.trinketsStyledCurios$getTarget();
        }
        return displayed;
    }

    public static AnchoredCurioSlot curio(Slot displayed) {
        Slot target = target(displayed);
        return target instanceof AnchoredCurioSlot curio ? curio : null;
    }

    public static VanillaAnchor anchor(Slot displayed) {
        // Resolve vanilla anchors by slot identity so unrelated menus do not match by index alone.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }
        Slot target = target(displayed);
        if (target instanceof AnchoredCurioSlot curio) {
            return curio.anchor();
        }
        for (VanillaAnchor anchor : VanillaAnchor.values()) {
            if (minecraft.player.inventoryMenu.slots.get(anchor.menuSlot()) == target) {
                return anchor;
            }
        }
        return null;
    }

    public static Iterable<Slot> visibleMenuSlots() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            return screen.getMenu().slots;
        }
        return java.util.List.of();
    }
}
