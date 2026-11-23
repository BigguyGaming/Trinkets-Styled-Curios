/* Lets the client move display slots when their anchor or screen layout changes. */
package com.trinketsstyledcurios.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("x")
    void trinketsStyledCurios$setX(int x);

    @Mutable
    @Accessor("y")
    void trinketsStyledCurios$setY(int y);
}
