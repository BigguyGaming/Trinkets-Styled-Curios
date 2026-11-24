/* Manages Survival flyout hover, empty-slot tooltips, and recipe-book overlap. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.client.FlyoutHitTest;
import com.trinketsstyledcurios.client.SlotUiState;
import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import com.trinketsstyledcurios.client.ClientConfig;
import com.trinketsstyledcurios.client.ClientTooltipHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    @Shadow
    private boolean widthTooNarrow;

    @Shadow
    public abstract RecipeBookComponent getRecipeBookComponent();

    protected InventoryScreenMixin(InventoryMenu menu, Inventory inventory) {
        super(menu, inventory, inventory.getDisplayName());
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;mouseClicked(DDI)Z"))
    private boolean trinketsStyledCurios$blockCoveredRecipeBookClick(RecipeBookComponent recipeBook,
            double mouseX, double mouseY, int button) {
        // In narrow mode the recipe book replaces the inventory entirely.
        return (this.widthTooNarrow || !FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY))
                && recipeBook.mouseClicked(mouseX, mouseY, button);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void trinketsStyledCurios$skipRecipeBookDrawnBelowFlyout(RecipeBookComponent recipeBook,
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        VanillaAnchor active = SlotUiState.activeAnchor();
        if (active == null || this.menu.slots.stream().noneMatch(slot ->
                slot instanceof AnchoredCurioSlot curio && curio.anchor() == active && slot.isActive())) {
            recipeBook.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIII)V"))
    private void trinketsStyledCurios$hideCoveredRecipeBookTooltip(RecipeBookComponent recipeBook,
            GuiGraphics graphics, int guiLeft, int guiTop, int mouseX, int mouseY) {
        if (!FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            recipeBook.renderTooltip(graphics, guiLeft, guiTop, mouseX, mouseY);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void trinketsStyledCurios$updateHoveredGroup(GuiGraphics graphics, int mouseX, int mouseY,
                                                         float partialTick, CallbackInfo callback) {
        // Keep a group open as the pointer moves from its anchor to one of its accessory cells.
        if (this.widthTooNarrow && this.getRecipeBookComponent().isVisible()) {
            SlotUiState.setActiveAnchor(null);
            return;
        }
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;
        VanillaAnchor previous = SlotUiState.activeAnchor();
        VanillaAnchor hovered = null;

        for (VanillaAnchor anchor : VanillaAnchor.values()) {
            if (trinketsStyledCurios$inside(relativeX, relativeY,
                    anchor.x() - 1, anchor.y() - 1, 18, 18)) {
                hovered = anchor;
                break;
            }
        }

        if (hovered == null && previous != null) {
            for (Slot slot : this.menu.slots) {
                if (slot instanceof AnchoredCurioSlot curioSlot
                        && curioSlot.anchor() == previous
                        && trinketsStyledCurios$inside(relativeX, relativeY,
                                slot.x - 1, slot.y - 1, 18, 18)) {
                    hovered = previous;
                    break;
                }
            }
        }

        SlotUiState.setActiveAnchor(hovered);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void trinketsStyledCurios$renderEmptySlotName(GuiGraphics graphics, int mouseX, int mouseY,
                                                          float partialTick, CallbackInfo callback) {
        // Occupied slots use vanilla item tooltips. Supply a slot name only when the displayed stack is
        // empty.
        if (!ClientConfig.showEmptySlotNames() || this.minecraft == null || this.minecraft.player == null
                || !this.menu.getCarried().isEmpty() || this.hoveredSlot == null) {
            return;
        }

        if (this.hoveredSlot instanceof AnchoredCurioSlot curioSlot) {
            ItemStack displayStack = curioSlot.getSlotExtension()
                    .getDisplayStack(curioSlot.getSlotContext(), curioSlot.getItem());
            if (!displayStack.isEmpty()) {
                return;
            }
            List<Component> tooltip = new ArrayList<>(curioSlot.getSlotExtension().getSlotTooltip(
                    curioSlot.getSlotContext(),
                    ClientTooltipFlag.of(this.minecraft.options.advancedItemTooltips
                            ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)));
            if (tooltip.isEmpty()) {
                tooltip.add(Component.literal(curioSlot.getSlotName()));
            }
            ClientTooltipHandler.renderSlotName(graphics, this.font, tooltip, mouseX, mouseY);
            return;
        }

        int menuSlot = this.menu.slots.indexOf(this.hoveredSlot);
        VanillaAnchor anchor = VanillaAnchor.byMenuSlot(menuSlot);
        if (anchor != null && this.hoveredSlot.getItem().isEmpty()) {
            ClientTooltipHandler.renderSlotName(graphics, this.font,
                    List.of(ClientTooltipHandler.armorSlotName(anchor.equipmentSlot())),
                    mouseX, mouseY);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"), remap = false, require = 0)
    private void trinketsStyledCurios$clearState(CallbackInfo callback) {
        SlotUiState.setActiveAnchor(null);
    }

    @Unique
    private static boolean trinketsStyledCurios$inside(int mouseX, int mouseY,
                                                       int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
