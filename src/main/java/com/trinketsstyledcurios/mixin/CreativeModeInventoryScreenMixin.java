/* Adds Curios flyouts to Creative's Survival Inventory tab and synchronizes edits through the native Creative protocol. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.client.ClientConfig;
import com.trinketsstyledcurios.client.ClientTooltipHandler;
import com.trinketsstyledcurios.client.DisplayedSlot;
import com.trinketsstyledcurios.client.FlyoutHitTest;
import com.trinketsstyledcurios.client.SlotUiState;
import com.trinketsstyledcurios.inventory.AnchoredCurioSlot;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
        extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    protected CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu,
                                                Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Redirect(method = "slotClicked", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;mayPickup(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean trinketsStyledCurios$allowEmptyCurioClick(Slot slot, Player player) {
        // Creative checks whether an item can be removed before processing placement. An empty Curios
        // slot must still accept a placement attempt.
        return (trinketsStyledCurios$self().isInventoryOpen()
                && DisplayedSlot.curio(slot) != null && !slot.hasItem()) || slot.mayPickup(player);
    }

    @Redirect(method = "slotClicked", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/InventoryMenu;clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"))
    private void trinketsStyledCurios$syncCreativeCurioChanges(InventoryMenu inventory, int index,
            int button, ClickType type, Player player) {
        // Capture changes to the actual Curios slots, including those made by shift-clicking a vanilla
        // slot. The vanilla Creative listener alone can miss those changes.
        var before = new java.util.LinkedHashMap<Slot, ItemStack>();
        for (Slot slot : inventory.slots) {
            if (slot instanceof AnchoredCurioSlot) {
                before.put(slot, slot.getItem().copy());
            }
        }
        inventory.clicked(index, button, type, player);
        before.forEach((slot, oldStack) -> {
            ItemStack stack = slot.getItem();
            if (!ItemStack.matches(oldStack, stack)) {
                this.minecraft.gameMode.handleCreativeModeItemAdd(stack.copy(), slot.index);
            }
        });
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void trinketsStyledCurios$placeCreativeCurios(CreativeModeTab tab, CallbackInfo callback) {
        // Reposition Creative's wrappers. Their targets retain the Survival menu indices and Curios
        // handlers.
        if (!trinketsStyledCurios$self().isInventoryOpen()) {
            SlotUiState.setActiveAnchor(null);
            return;
        }
        for (Slot displayed : this.menu.slots) {
            AnchoredCurioSlot curio = DisplayedSlot.curio(displayed);
            if (curio == null) {
                continue;
            }
            VanillaAnchor anchor = curio.anchor();
            int x = trinketsStyledCurios$anchorX(anchor) + curio.x - anchor.x();
            int y = trinketsStyledCurios$anchorY(anchor);
            ((SlotAccessor) (Object) displayed).trinketsStyledCurios$setX(x);
            ((SlotAccessor) (Object) displayed).trinketsStyledCurios$setY(y);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void trinketsStyledCurios$updateCreativeHover(GuiGraphics graphics, int mouseX, int mouseY,
                                                           float partialTick, CallbackInfo callback) {
        // Keep the open flyout active when its cells overlap another anchor.
        if (!trinketsStyledCurios$self().isInventoryOpen()) {
            SlotUiState.setActiveAnchor(null);
            return;
        }
        int relativeX = mouseX - this.leftPos;
        int relativeY = mouseY - this.topPos;
        VanillaAnchor previous = SlotUiState.activeAnchor();
        VanillaAnchor hovered = null;

        for (VanillaAnchor anchor : VanillaAnchor.values()) {
            if (trinketsStyledCurios$inside(relativeX, relativeY,
                    trinketsStyledCurios$anchorX(anchor) - 1,
                    trinketsStyledCurios$anchorY(anchor) - 1, 18, 18)) {
                hovered = anchor;
                break;
            }
        }
        if (previous != null) {
            for (Slot displayed : this.menu.slots) {
                AnchoredCurioSlot curio = DisplayedSlot.curio(displayed);
                if (curio != null && curio.anchor() == previous && displayed.isActive()
                        && trinketsStyledCurios$inside(relativeX, relativeY,
                        displayed.x - 1, displayed.y - 1, 18, 18)) {
                    hovered = previous;
                    break;
                }
            }
        }
        SlotUiState.setActiveAnchor(hovered);
    }

    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$keepFlyoutClicksInside(double mouseX, double mouseY,
            int guiLeft, int guiTop, int button, CallbackInfoReturnable<Boolean> callback) {
        // A flyout can extend outside the Creative inventory panel without becoming a drop target.
        if (trinketsStyledCurios$self().isInventoryOpen()
                && FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "checkTabClicked", at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$protectFlyoutFromTabs(CreativeModeTab tab, double x, double y,
            CallbackInfoReturnable<Boolean> callback) {
        // Convert Creative's panel-relative tab coordinates to GUI coordinates before testing overlap.
        if (trinketsStyledCurios$self().isInventoryOpen()
                && FlyoutHitTest.isOverActiveFlyout(x + this.leftPos, y + this.topPos)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$hideCoveredTabTooltip(GuiGraphics graphics,
            CreativeModeTab tab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> callback) {
        // Suppress the tab's tooltip so only the flyout slot can describe the pointer target.
        if (trinketsStyledCurios$self().isInventoryOpen()
                && FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)) {
            callback.setReturnValue(false);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;isHovering(IIIIDD)Z"))
    private boolean trinketsStyledCurios$hideCoveredTrashTooltip(CreativeModeInventoryScreen screen,
            int x, int y, int width, int height, double mouseX, double mouseY) {
        // The trash tooltip has its own hover test, separate from normal slot hit testing.
        return !FlyoutHitTest.isOverActiveFlyout(mouseX, mouseY)
                && this.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void trinketsStyledCurios$renderCreativeSlotName(GuiGraphics graphics, int mouseX, int mouseY,
                                                             float partialTick, CallbackInfo callback) {
        // Use Curios' slot tooltip for empty accessory slots and localized names for empty vanilla
        // anchors.
        if (!ClientConfig.showEmptySlotNames() || !trinketsStyledCurios$self().isInventoryOpen()
                || this.minecraft == null || this.minecraft.player == null || !this.menu.getCarried().isEmpty()
                || this.hoveredSlot == null) {
            return;
        }

        AnchoredCurioSlot curio = DisplayedSlot.curio(this.hoveredSlot);
        if (curio != null) {
            ItemStack displayStack = curio.getSlotExtension()
                    .getDisplayStack(curio.getSlotContext(), curio.getItem());
            if (!displayStack.isEmpty()) {
                return;
            }
            List<Component> tooltip = new ArrayList<>(curio.getSlotExtension().getSlotTooltip(
                    curio.getSlotContext(), ClientTooltipFlag.of(this.minecraft.options.advancedItemTooltips
                            ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)));
            if (tooltip.isEmpty()) {
                tooltip.add(Component.literal(curio.getSlotName()));
            }
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            return;
        }

        VanillaAnchor anchor = DisplayedSlot.anchor(this.hoveredSlot);
        if (anchor != null && this.hoveredSlot.getItem().isEmpty()) {
            graphics.renderTooltip(this.font, ClientTooltipHandler.armorSlotName(anchor.equipmentSlot()),
                    mouseX, mouseY);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"), require = 0)
    private void trinketsStyledCurios$clearCreativeState(CallbackInfo callback) {
        SlotUiState.setActiveAnchor(null);
    }

    @Unique
    private CreativeModeInventoryScreen trinketsStyledCurios$self() {
        return (CreativeModeInventoryScreen) (Object) this;
    }

    @Unique
    private static int trinketsStyledCurios$anchorX(VanillaAnchor anchor) {
        return switch (anchor) {
            case HEAD, CHEST -> 54;
            case LEGS, FEET -> 108;
            case OFFHAND -> 35;
        };
    }

    @Unique
    private static int trinketsStyledCurios$anchorY(VanillaAnchor anchor) {
        return switch (anchor) {
            case HEAD, LEGS -> 6;
            case CHEST, FEET -> 33;
            case OFFHAND -> 20;
        };
    }

    @Unique
    private static boolean trinketsStyledCurios$inside(int mouseX, int mouseY,
                                                       int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
