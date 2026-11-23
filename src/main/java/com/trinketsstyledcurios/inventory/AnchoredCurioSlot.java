/* Adapts a Curios slot to a player inventory anchor without owning a separate stack. */
package com.trinketsstyledcurios.inventory;

import com.trinketsstyledcurios.client.SlotUiState;
import com.trinketsstyledcurios.mixin.SlotAccessor;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public final class AnchoredCurioSlot extends CurioSlot {
    private VanillaAnchor anchor;

    public AnchoredCurioSlot(Player player, ICurioStacksHandler stacks, int index, String identifier,
                             int x, int y, VanillaAnchor anchor) {
        super(player, stacks.getStacks(), index, identifier, x, y,
                stacks.getRenders(), stacks.getActiveStates(), stacks.canToggleRendering(), false, false);
        this.anchor = anchor;
    }

    public VanillaAnchor anchor() {
        return anchor;
    }

    public void updatePlacement(VanillaAnchor anchor, int x, int y) {
        // Move the client-facing slot without replacing the Curios stack handler.
        this.anchor = anchor;
        ((SlotAccessor) (Object) this).trinketsStyledCurios$setX(x);
        ((SlotAccessor) (Object) this).trinketsStyledCurios$setY(y);
    }

    @Override
    public boolean isActive() {
        // The server always sees the slot. The client exposes it only while its anchor is open.
        Player player = this.player();
        return player == null || !player.level().isClientSide || SlotUiState.activeAnchor() == anchor;
    }

    private Player player() {
        return this.getSlotContext().entity() instanceof Player player ? player : null;
    }
}
