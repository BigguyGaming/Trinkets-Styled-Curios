/* Registers the client tooltip options and exposes them to the inventory screens. */
package com.trinketsstyledcurios.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ITEM_SLOTS;
    private static final ModConfigSpec.BooleanValue ARMOR_ITEM_SLOTS;
    private static final ModConfigSpec.BooleanValue EMPTY_SLOT_NAMES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ITEM_SLOTS = builder.comment("Show equipment slot names in item tooltips.")
                .define("showItemSlots", true);
        ARMOR_ITEM_SLOTS = builder.comment("Include vanilla armor slot names when item slot tooltips are enabled.")
                .define("showArmorItemSlots", true);
        EMPTY_SLOT_NAMES = builder.comment("Show the name of an empty armor, offhand, or Curios slot on hover.")
                .define("showEmptySlotNames", true);
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static boolean showItemSlots() {
        return enabled(ITEM_SLOTS);
    }

    public static boolean showArmorItemSlots() {
        return showItemSlots() && enabled(ARMOR_ITEM_SLOTS);
    }

    public static boolean showEmptySlotNames() {
        return enabled(EMPTY_SLOT_NAMES);
    }

    private static boolean enabled(ModConfigSpec.BooleanValue option) {
        return SPEC.isLoaded() ? option.get() : option.getDefault();
    }
}
