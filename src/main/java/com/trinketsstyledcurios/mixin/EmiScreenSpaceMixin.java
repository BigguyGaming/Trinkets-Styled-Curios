/* Keeps cached EMI icons and highlights from drawing across active flyouts. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.client.FlyoutHitTest;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager$ScreenSpace", remap = false)
public abstract class EmiScreenSpaceMixin {
    @Shadow
    public StackBatcher batcher;

    @Unique
    private int trinketsStyledCurios$lastFlyoutLayout = Integer.MIN_VALUE;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void trinketsStyledCurios$refreshBatchForFlyout(CallbackInfo callback) {
        // Rebuild the cached icons only when the visible flyout geometry changes.
        int layout = FlyoutHitTest.activeFlyoutLayoutKey();
        if (layout != trinketsStyledCurios$lastFlyoutLayout) {
            trinketsStyledCurios$lastFlyoutLayout = layout;
            if (batcher != null) {
                batcher.repopulate();
            }
        }
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Ldev/emi/emi/screen/StackBatcher;render(Ldev/emi/emi/api/stack/EmiIngredient;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),
            require = 0)
    private void trinketsStyledCurios$hideCoveredStack(StackBatcher batcher, EmiIngredient ingredient,
                                                       GuiGraphics graphics, int x, int y, float delta) {
        // Skip any item sprite whose bounds intersect the visible flyout.
        if (!FlyoutHitTest.intersectsActiveFlyout(x, y, 16, 16)) {
            batcher.render(ingredient, graphics, x, y, delta);
        }
    }

    @Inject(method = "getRawOffsetFromMouse", at = @At("HEAD"), cancellable = true, require = 0)
    private void trinketsStyledCurios$hideCoveredCell(int mouseX, int mouseY,
                                                      CallbackInfoReturnable<Integer> callback) {
        // Hide the EMI hover highlight underneath a flyout cell.
        if (FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(-1);
        }
    }
}
