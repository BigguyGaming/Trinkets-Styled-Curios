/* Builds a deterministic layout backed directly by the player's Curios handlers. */
package com.trinketsstyledcurios.inventory;

import com.trinketsstyledcurios.data.SlotAssignments;
import com.trinketsstyledcurios.mixin.AbstractContainerMenuAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CurioMenuSlots {
    private CurioMenuSlots() {
    }

    public static void addTo(AbstractContainerMenu menu, Player owner) {
        // Sort identifiers before grouping them so both sides append the same slot indices.
        if (menu.slots.stream().anyMatch(AnchoredCurioSlot.class::isInstance)) {
            return;
        }
        CuriosApi.getCuriosInventory(owner).ifPresent(handler -> {
            Map<VanillaAnchor, List<Map.Entry<String, ICurioStacksHandler>>> grouped =
                    new EnumMap<>(VanillaAnchor.class);
            handler.getCurios().entrySet().stream()
                    .filter(entry -> entry.getValue().isVisible() && entry.getValue().getSlots() > 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> grouped
                            .computeIfAbsent(SlotAssignments.anchorFor(entry.getKey()), ignored -> new ArrayList<>())
                            .add(entry));

            grouped.forEach((anchor, entries) -> {
                int position = 0;
                for (Map.Entry<String, ICurioStacksHandler> entry : entries) {
                    for (int index = 0; index < entry.getValue().getSlots(); index++) {
                        int x = anchor.x() + horizontalOffset(position++);
                        ((AbstractContainerMenuAccessor) menu).trinketsStyledCurios$addSlot(
                                new AnchoredCurioSlot(owner, entry.getValue(), index,
                                        entry.getKey(), x, anchor.y(), anchor));
                    }
                }
            });
        });
    }

    public static int horizontalOffset(int position) {
        // Alternate right and left in 18-pixel steps, expanding away from the anchor.
        int distance = position / 2 + 1;
        return (position % 2 == 0 ? distance : -distance) * 18;
    }
}
