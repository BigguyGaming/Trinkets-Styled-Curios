/* Allows native Creative slot packets to update appended Curios slots through the real player menu. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Allows vanilla Creative slot updates to address the real Curios slots that
 * are appended to InventoryMenu. This is the same strategy used by Trinkets.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyConstant(method = "handleSetCreativeModeSlot", constant = @Constant(intValue = 45))
    private int trinketsStyledCurios$includeCuriosSlots(int vanillaMaximum,
            ServerboundSetCreativeModeSlotPacket packet) {
        // Expand the limit only for an existing Curios-backed target that accepts the supplied stack.
        int index = packet.slotNum();
        var slots = this.player.inventoryMenu.slots;
        if (index > vanillaMaximum && index < slots.size()
                && slots.get(index) instanceof AnchoredCurioSlot slot
                && (packet.itemStack().isEmpty() || slot.mayPlace(packet.itemStack()))) {
            return index;
        }
        return vanillaMaximum;
    }
}
