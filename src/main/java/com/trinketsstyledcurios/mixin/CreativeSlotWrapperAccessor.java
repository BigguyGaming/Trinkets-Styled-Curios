/* Exposes the real inventory slot behind Creative's display wrapper. */
package com.trinketsstyledcurios.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public interface CreativeSlotWrapperAccessor {
    @Accessor("target")
    Slot trinketsStyledCurios$getTarget();
}
