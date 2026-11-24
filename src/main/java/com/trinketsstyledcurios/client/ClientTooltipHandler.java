/* Adds localized equipment-slot names to vanilla and modded armor tooltips. */
package com.trinketsstyledcurios.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Locale;

public final class ClientTooltipHandler {
    private ClientTooltipHandler() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ClientConfig.showArmorItemSlots()) {
            return;
        }

        // Ask Minecraft where the stack equips so modded armor uses the same tooltip path.
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (stack.isEmpty() || player == null) {
            return;
        }

        EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(stack);
        if (!isPlayerArmorSlot(equipmentSlot)) {
            return;
        }

        // Curios already emits this exact tooltip for matching Curios slots. Avoid
        // duplicating the line when its dynamic identifier is the vanilla slot name.
        if (CuriosApi.getItemStackSlots(stack, player).containsKey(equipmentSlot.getName())) {
            return;
        }

        MutableComponent line = Component.translatable("curios.tooltip.slot")
                .append(" ")
                .withStyle(ChatFormatting.GOLD);
        line.append(armorSlotName(equipmentSlot).withStyle(ChatFormatting.YELLOW));
        event.getToolTip().add(Math.min(1, event.getToolTip().size()), line);
    }

    public static void renderSlotName(GuiGraphics graphics, Font font, List<Component> tooltip,
                                      int mouseX, int mouseY) {
        graphics.pose().pushPose();
        try {
            // Match the item tooltip layer so a wide flyout cannot cover its own label.
            graphics.pose().translate(0.0F, 0.0F, SlotUiState.FLYOUT_DEPTH);
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    public static MutableComponent armorSlotName(EquipmentSlot slot) {
        String name = slot.getName();
        String fallback = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase(Locale.ROOT);
        return Component.translatableWithFallback("trinkets_styled_curios.slot." + name, fallback);
    }

    private static boolean isPlayerArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }
}
