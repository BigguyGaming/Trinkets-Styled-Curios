/* Stops EMI from handling pointer interactions over flyout slots. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.client.FlyoutHitTest;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager", remap = false)
public abstract class EmiScreenManagerMixin {
    private static Object trinketsStyledCurios$emptyInteraction;

    @Inject(method = "getHoveredStack(IIZ)Ldev/emi/emi/api/stack/EmiStackInteraction;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void trinketsStyledCurios$hideCoveredHover(int mouseX, int mouseY, boolean includeStandard,
                                                              CallbackInfoReturnable<Object> callback) {
        trinketsStyledCurios$suppressHover(mouseX, mouseY, callback);
    }

    @Inject(method = "getHoveredStack(IIZZ)Ldev/emi/emi/api/stack/EmiStackInteraction;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void trinketsStyledCurios$hideCoveredHoverExtended(int mouseX, int mouseY,
                                                                      boolean includeStandard,
                                                                      boolean includeCraftables,
                                                                      CallbackInfoReturnable<Object> callback) {
        trinketsStyledCurios$suppressHover(mouseX, mouseY, callback);
    }

    private static void trinketsStyledCurios$suppressHover(int mouseX, int mouseY,
                                                            CallbackInfoReturnable<Object> callback) {
        // Return EMI's empty hover result without requiring EMI to be installed.
        if (!FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            return;
        }
        try {
            if (trinketsStyledCurios$emptyInteraction == null) {
                Class<?> interaction = Class.forName("dev.emi.emi.api.stack.EmiStackInteraction");
                trinketsStyledCurios$emptyInteraction = interaction.getField("EMPTY").get(null);
            }
            callback.setReturnValue(trinketsStyledCurios$emptyInteraction);
        } catch (ReflectiveOperationException ignored) {
            // EMI changed incompatibly; leave its result untouched instead of breaking the inventory.
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private static void trinketsStyledCurios$blockCoveredClick(double mouseX, double mouseY, int button,
                                                               CallbackInfoReturnable<Boolean> callback) {
        if (FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true, require = 0)
    private static void trinketsStyledCurios$blockCoveredRelease(double mouseX, double mouseY, int button,
                                                                 CallbackInfoReturnable<Boolean> callback) {
        if (FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, require = 0)
    private static void trinketsStyledCurios$blockCoveredScroll(double mouseX, double mouseY, double amount,
                                                                CallbackInfoReturnable<Boolean> callback) {
        if (FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(false);
        }
    }
}
