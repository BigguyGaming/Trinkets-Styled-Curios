/* Exposes menu internals needed to append slots and keep their sync lists aligned. */
package com.trinketsstyledcurios.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    @Invoker("addSlot")
    Slot trinketsStyledCurios$addSlot(Slot slot);

    @Accessor("lastSlots")
    NonNullList<ItemStack> trinketsStyledCurios$getLastSlots();

    @Accessor("remoteSlots")
    NonNullList<ItemStack> trinketsStyledCurios$getRemoteSlots();
}
