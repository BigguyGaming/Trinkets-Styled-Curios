/* Tracks the anchor currently open in the client inventory. */
package com.trinketsstyledcurios.client;

import com.trinketsstyledcurios.inventory.VanillaAnchor;

public final class SlotUiState {
    // Offset the flyout and its tooltips together above inventory overlays.
    public static final float FLYOUT_DEPTH = 1000.0F;

    private static volatile VanillaAnchor activeAnchor;

    private SlotUiState() {
    }

    public static VanillaAnchor activeAnchor() {
        return activeAnchor;
    }

    public static void setActiveAnchor(VanillaAnchor anchor) {
        activeAnchor = anchor;
    }
}
