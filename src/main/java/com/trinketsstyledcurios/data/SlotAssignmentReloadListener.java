/* Loads Curios slot anchor assignments from datapack JSON. */
package com.trinketsstyledcurios.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import com.trinketsstyledcurios.TrinketsStyledCurios;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SlotAssignmentReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public SlotAssignmentReloadListener() {
        super(GSON, "trinkets_styled_curios/slot_assignments");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        // Process resources in identifier order so conflicting assignments resolve consistently.
        Map<String, VanillaAnchor> result = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey().toString());
                if (GsonHelper.getAsBoolean(json, "replace", false)) {
                    result.clear();
                }
                for (VanillaAnchor anchor : VanillaAnchor.values()) {
                    String key = anchor.name().toLowerCase(java.util.Locale.ROOT);
                    if (!json.has(key)) {
                        continue;
                    }
                    json.getAsJsonArray(key).forEach(value -> {
                        String identifier = value.getAsString();
                        if (!identifier.isBlank()) {
                            result.put(identifier, anchor);
                        }
                    });
                }
            } catch (RuntimeException exception) {
                TrinketsStyledCurios.LOGGER.error("Invalid Curios slot assignment file {}", entry.getKey(), exception);
            }
        });
        SlotAssignments.replace(result);
        TrinketsStyledCurios.LOGGER.info("Loaded {} Curios slot assignments", result.size());
    }
}
