/* Applies the global item slot tooltip setting at the Curios tooltip source. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.client.ClientConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.ClientEventHandler;

@Mixin(value = ClientEventHandler.class, remap = false)
abstract class CuriosItemTooltipMixin {
    // This handler adds only the Curios slot line. Curio attributes use a separate event.
    @Inject(method = "onTooltip", at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$applyItemSlotSetting(ItemTooltipEvent event, CallbackInfo callback) {
        if (!ClientConfig.showItemSlots()) {
            callback.cancel();
        }
    }
}
