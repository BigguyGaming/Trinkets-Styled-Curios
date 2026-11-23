/* Builds client inventory slots after Curios supplies the local player's handlers. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.inventory.CurioMenuSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import top.theillusivec4.curios.common.network.client.CuriosClientPackets;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncCurios;

@Mixin(value = CuriosClientPackets.class, remap = false)
public abstract class CuriosClientPacketsMixin {
    @Inject(method = "handle(Ltop/theillusivec4/curios/common/network/server/sync/SPacketSyncCurios;)V",
            at = @At("RETURN"))
    private static void trinketsStyledCurios$buildInventorySlotsAfterCuriosSync(
            SPacketSyncCurios packet, CallbackInfo callback) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getId() == packet.entityId) {
            CurioMenuSlots.addTo(player.inventoryMenu, player);
        }
    }
}
