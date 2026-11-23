/* Provides screen-space hit tests for flyout input and overlay rendering. */
package com.trinketsstyledcurios.client;

import com.trinketsstyledcurios.inventory.VanillaAnchor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;

public final class FlyoutHitTest {
    private FlyoutHitTest() {
    }

    public static boolean isOverActiveFlyout(double mouseX, double mouseY) {
        // Use the current screen's display coordinates, because Creative wrappers can move independently
        // of their targets.
        Minecraft minecraft = Minecraft.getInstance();
        VanillaAnchor active = SlotUiState.activeAnchor();
        if (minecraft.player == null || active == null
                || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }
        int relativeX = (int) mouseX - screen.getGuiLeft();
        int relativeY = (int) mouseY - screen.getGuiTop();
        return screen.getMenu().slots.stream()
                .filter(slot -> DisplayedSlot.curio(slot) != null)
                .filter(slot -> DisplayedSlot.curio(slot).anchor() == active && slot.isActive())
                .anyMatch(slot -> relativeX >= slot.x - 1 && relativeX < slot.x + 17
                        && relativeY >= slot.y - 1 && relativeY < slot.y + 17);
    }

    public static boolean intersectsActiveFlyout(int left, int top, int width, int height) {
        // Check the entire rectangle so an overlay sprite cannot protrude into a covered cell.
        Minecraft minecraft = Minecraft.getInstance();
        VanillaAnchor active = SlotUiState.activeAnchor();
        if (minecraft.player == null || active == null
                || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }
        return screen.getMenu().slots.stream()
                .filter(slot -> DisplayedSlot.curio(slot) != null)
                .filter(slot -> DisplayedSlot.curio(slot).anchor() == active && slot.isActive())
                .anyMatch(slot -> {
                    int flyoutLeft = screen.getGuiLeft() + slot.x - 1;
                    int flyoutTop = screen.getGuiTop() + slot.y - 1;
                    return left < flyoutLeft + 18 && left + width > flyoutLeft
                            && top < flyoutTop + 18 && top + height > flyoutTop;
                });
    }

    /**
     * A stable key for the currently visible flyout footprint. EMI caches its
     * sidebar item vertices, so its batch must be rebuilt when this changes.
     */
    public static int activeFlyoutLayoutKey() {
        Minecraft minecraft = Minecraft.getInstance();
        VanillaAnchor active = SlotUiState.activeAnchor();
        if (minecraft.player == null || active == null
                || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return 0;
        }

        int key = 31 + System.identityHashCode(screen);
        key = 31 * key + active.ordinal();
        key = 31 * key + screen.getGuiLeft();
        key = 31 * key + screen.getGuiTop();
        for (var slot : screen.getMenu().slots) {
            var curio = DisplayedSlot.curio(slot);
            if (curio != null && curio.anchor() == active && slot.isActive()) {
                key = 31 * key + slot.x;
                key = 31 * key + slot.y;
            }
        }
        return key;
    }
}
