/* Draws connected flyout frames and gives visible flyout slots priority over covered controls. */
package com.trinketsstyledcurios.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trinketsstyledcurios.client.DisplayedSlot;
import com.trinketsstyledcurios.client.FlyoutHitTest;
import com.trinketsstyledcurios.client.SlotUiState;
import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import com.trinketsstyledcurios.TrinketsStyledCurios;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z",
            at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$preferCreativeFlyoutSlot(Slot slot, double mouseX, double mouseY,
            CallbackInfoReturnable<Boolean> callback) {
        // Treat the visible flyout as the topmost hit target where it crosses a Creative slot.
        if ((Object) this instanceof CreativeModeInventoryScreen creative && creative.isInventoryOpen()
                && FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)
                && DisplayedSlot.curio(slot) == null) {
            callback.setReturnValue(false);
        }
    }

    @Unique
    private static final ResourceLocation TRINKETS_STYLED_CURIOS$BLANK_BACK =
            ResourceLocation.fromNamespaceAndPath(TrinketsStyledCurios.MOD_ID, "textures/gui/blank_back.png");
    @Unique
    private static final ResourceLocation TRINKETS_STYLED_CURIOS$MORE_SLOTS =
            ResourceLocation.fromNamespaceAndPath(TrinketsStyledCurios.MOD_ID, "textures/gui/more_slots.png");

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V", ordinal = 0))
    private void trinketsStyledCurios$renderRecipeBookBelowFlyout(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick, CallbackInfo callback) {
        // Wide Survival inventory renders the book after its slots by default. Draw it before
        // the slot pass when a flyout is open so even widgets drawn without depth stay below it.
        if ((Object) this instanceof InventoryScreen inventory && trinketsStyledCurios$hasActiveFlyout()) {
            // Widget tooltips are also queued during render, before RecipeBookComponent.renderTooltip.
            boolean covered = FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY);
            inventory.getRecipeBookComponent().render(graphics,
                    covered ? Integer.MIN_VALUE : mouseX, covered ? Integer.MIN_VALUE : mouseY, partialTick);
        }
    }

    @Unique
    private static boolean trinketsStyledCurios$hasActiveFlyout() {
        VanillaAnchor active = SlotUiState.activeAnchor();
        return active != null && trinketsStyledCurios$hasVisibleSlots(active);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void trinketsStyledCurios$renderFlyoutSlot(AbstractContainerScreen<?> screen, GuiGraphics graphics,
                                                      Slot slot, Operation<Void> original) {
        VanillaAnchor anchor = trinketsStyledCurios$panelAnchor(slot);
        if (anchor == null) {
            original.call(screen, graphics, slot);
            return;
        }

        graphics.pose().pushPose();
        try {
            // Use the original slot renderer so item decorations and drag previews stay intact.
            graphics.pose().translate(0.0F, 0.0F, SlotUiState.FLYOUT_DEPTH + 310.0F);
            if (DisplayedSlot.curio(slot) != null) {
                graphics.blit(TRINKETS_STYLED_CURIOS$BLANK_BACK,
                        slot.x, slot.y, 0, 0, 16, 16, 16, 16);
                if (slot.getItem().isEmpty() && slot.getNoItemIcon() != null) {
                    var icon = slot.getNoItemIcon();
                    TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(icon.getFirst())
                            .apply(icon.getSecond());
                    graphics.pose().translate(0.0F, 0.0F, 1.0F);
                    graphics.blit(slot.x, slot.y, 0, 16, 16, sprite);
                    graphics.pose().translate(0.0F, 0.0F, -1.0F);
                }
            }
            // Trinkets builds a connected frame from a 4 px cap and an 18 px
            // center for every slot. Neighbouring centers touch, so a run of
            // Curios slots reads as one expanding panel instead of separate boxes.
            if (!trinketsStyledCurios$hasNeighbour(anchor, slot, -18)) {
                graphics.blit(TRINKETS_STYLED_CURIOS$MORE_SLOTS,
                        slot.x - 5, slot.y - 5, 0, 0, 4, 26, 256, 256);
            }
            graphics.blit(TRINKETS_STYLED_CURIOS$MORE_SLOTS,
                    slot.x - 1, slot.y - 5, 4, 0, 18, 26, 256, 256);
            if (!trinketsStyledCurios$hasNeighbour(anchor, slot, 18)) {
                graphics.blit(TRINKETS_STYLED_CURIOS$MORE_SLOTS,
                        slot.x + 17, slot.y - 5, 22, 0, 4, 26, 256, 256);
            }
            graphics.pose().translate(0.0F, 0.0F, -240.0F);
            original.call(screen, graphics, slot);
        } finally {
            graphics.pose().popPose();
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V"))
    private void trinketsStyledCurios$renderFlyoutHighlight(AbstractContainerScreen<?> screen, GuiGraphics graphics,
            Slot slot, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (trinketsStyledCurios$panelAnchor(slot) == null) {
            original.call(screen, graphics, slot, mouseX, mouseY, partialTick);
            return;
        }

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, 0.0F, SlotUiState.FLYOUT_DEPTH + 311.0F);
            original.call(screen, graphics, slot, mouseX, mouseY, partialTick);
        } finally {
            graphics.pose().popPose();
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void trinketsStyledCurios$renderCarriedStack(AbstractContainerScreen<?> screen, GuiGraphics graphics,
            ItemStack stack, int x, int y, String count, Operation<Void> original) {
        if (!trinketsStyledCurios$hasActiveFlyout()) {
            original.call(screen, graphics, stack, x, y, count);
            return;
        }

        // Keep cursor stacks and returning stacks above the raised panel.
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, 0.0F, SlotUiState.FLYOUT_DEPTH + 100.0F);
            original.call(screen, graphics, stack, x, y, count);
        } finally {
            graphics.pose().popPose();
        }
    }

    @WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void trinketsStyledCurios$renderItemTooltipAboveFlyout(GuiGraphics graphics,
            Font font, List<Component> lines, Optional<TooltipComponent> image,
            ItemStack stack, int mouseX, int mouseY, Operation<Void> original) {
        if (!trinketsStyledCurios$hasActiveFlyout()) {
            original.call(graphics, font, lines, image, stack, mouseX, mouseY);
            return;
        }
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, 0.0F, SlotUiState.FLYOUT_DEPTH);
            original.call(graphics, font, lines, image, stack, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    @Unique
    private static VanillaAnchor trinketsStyledCurios$panelAnchor(Slot slot) {
        var player = Minecraft.getInstance().player;
        VanillaAnchor active = SlotUiState.activeAnchor();
        if (player == null || active == null || !slot.isActive()
                || !trinketsStyledCurios$hasVisibleSlots(active)) {
            return null;
        }
        AnchoredCurioSlot curioSlot = DisplayedSlot.curio(slot);
        if (curioSlot != null) {
            return curioSlot.anchor() == active ? active : null;
        }
        return player.inventoryMenu.slots.get(active.menuSlot()) == DisplayedSlot.target(slot) ? active : null;
    }

    @Unique
    private static boolean trinketsStyledCurios$hasVisibleSlots(VanillaAnchor anchor) {
        var player = Minecraft.getInstance().player;
        return player != null && java.util.stream.StreamSupport.stream(
                        DisplayedSlot.visibleMenuSlots().spliterator(), false)
                .anyMatch(slot -> {
                    AnchoredCurioSlot curio = DisplayedSlot.curio(slot);
                    return curio != null && curio.anchor() == anchor && slot.isActive();
                });
    }

    @Redirect(method = "mouseClicked",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"))
    private boolean trinketsStyledCurios$suppressCoveredWidgets(Screen screen,
                                                                 double mouseX, double mouseY,
                                                                 int button) {
        boolean supportedInventory = screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen creative && creative.isInventoryOpen();
        if (supportedInventory && FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            return false;
        }

        // This is Screen's normal child-event dispatch. Keeping it here lets us
        // suppress every widget under a flyout without knowing which mod owns it.
        for (var child : screen.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                screen.setFocused(child);
                if (button == 0) {
                    screen.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean trinketsStyledCurios$hasNeighbour(VanillaAnchor anchor, Slot slot, int xOffset) {
        // Only adjacent cells in the same visible row share a frame edge.
        int neighbourX = slot.x + xOffset;
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        return java.util.stream.StreamSupport.stream(
                        DisplayedSlot.visibleMenuSlots().spliterator(), false)
                .anyMatch(other -> other != slot
                        && other.isActive()
                        && DisplayedSlot.anchor(other) == anchor
                        && other.y == slot.y
                        && other.x == neighbourX);
    }
}
