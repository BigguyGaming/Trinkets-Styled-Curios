/* Adds Curios-backed slots to the player inventory menu and routes shift-click transfers. */
package com.trinketsstyledcurios.mixin;

import com.trinketsstyledcurios.inventory.CurioMenuSlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import top.theillusivec4.curios.api.type.ICuriosMenu;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu implements ICuriosMenu {
    @Unique
    private int trinketsStyledCurios$slotStart;
    @Unique
    private Player trinketsStyledCurios$owner;

    protected InventoryMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void trinketsStyledCurios$addCuriosSlots(Inventory inventory, boolean active, Player owner,
                                                     CallbackInfo callback) {
        // Record the original slot count so Curios slots can be rebuilt when the handler changes.
        trinketsStyledCurios$slotStart = this.slots.size();
        trinketsStyledCurios$owner = owner;
        CurioMenuSlots.addTo(this, owner);
    }

    @Override
    public void resetSlots() {
        // Remove the appended slots and their entries in both of Minecraft's parallel synchronization
        // lists.
        AbstractContainerMenuAccessor accessor = (AbstractContainerMenuAccessor) this;
        while (this.slots.size() > trinketsStyledCurios$slotStart) {
            int last = this.slots.size() - 1;
            this.slots.remove(last);
            accessor.trinketsStyledCurios$getLastSlots().remove(last);
            accessor.trinketsStyledCurios$getRemoteSlots().remove(last);
        }
        if (trinketsStyledCurios$owner != null) {
            CurioMenuSlots.addTo(this, trinketsStyledCurios$owner);
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void trinketsStyledCurios$quickMove(Player player, int index,
                                                CallbackInfoReturnable<ItemStack> callback) {
        // Handle transfers involving Curios first and let vanilla handle unrelated shift-clicks.
        if (index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot source = this.slots.get(index);
        if (!source.hasItem()) {
            return;
        }

        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        if (index >= trinketsStyledCurios$slotStart) {
            if (!this.moveItemStackTo(sourceStack, InventoryMenu.INV_SLOT_START,
                    InventoryMenu.USE_ROW_SLOT_END, false)) {
                callback.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (index >= InventoryMenu.INV_SLOT_START && index < InventoryMenu.USE_ROW_SLOT_END) {
            if (!this.moveItemStackTo(sourceStack, trinketsStyledCurios$slotStart,
                    this.slots.size(), false)) {
                return;
            }
        } else {
            return;
        }

        if (sourceStack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        source.onTake(player, sourceStack);
        callback.setReturnValue(original);
    }
}
