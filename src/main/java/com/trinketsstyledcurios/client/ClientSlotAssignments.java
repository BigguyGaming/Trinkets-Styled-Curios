/* Applies server assignments to client slot positions without replacing the backing handlers. */
package com.trinketsstyledcurios.client;

import com.trinketsstyledcurios.data.SlotAssignments;
import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import com.trinketsstyledcurios.inventory.CurioMenuSlots;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ClientSlotAssignments {
    private ClientSlotAssignments() {
    }

    public static void apply(Map<String, VanillaAnchor> assignments) {
        SlotAssignments.replace(assignments);
        // Existing menu slots keep the same Curios handlers; only their visual anchors need changing.
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            CurioMenuSlots.addTo(minecraft.player.inventoryMenu, minecraft.player);
            Map<VanillaAnchor, List<AnchoredCurioSlot>> grouped = new EnumMap<>(VanillaAnchor.class);
            minecraft.player.inventoryMenu.slots.stream()
                    .filter(AnchoredCurioSlot.class::isInstance)
                    .map(AnchoredCurioSlot.class::cast)
                    .forEach(slot -> {
                        VanillaAnchor anchor = SlotAssignments.anchorFor(slot.getIdentifier());
                        grouped.computeIfAbsent(anchor, ignored -> new ArrayList<>()).add(slot);
                    });
            grouped.forEach((anchor, slots) -> {
                slots.sort(Comparator.comparing(AnchoredCurioSlot::getIdentifier)
                        .thenComparingInt(AnchoredCurioSlot::getSlotIndex));
                for (int position = 0; position < slots.size(); position++) {
                    int x = anchor.x() + CurioMenuSlots.horizontalOffset(position);
                    slots.get(position).updatePlacement(anchor, x, anchor.y());
                }
            });
        }
    }
}
