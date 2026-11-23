/* Stores the active slot-to-anchor map and its defaults. */
package com.trinketsstyledcurios.data;

import com.trinketsstyledcurios.inventory.VanillaAnchor;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SlotAssignments {
    private static volatile Map<String, VanillaAnchor> assignments = defaults();

    private SlotAssignments() {
    }

    public static VanillaAnchor anchorFor(String curiosIdentifier) {
        // Unknown identifiers use the offhand anchor.
        return assignments.getOrDefault(curiosIdentifier, VanillaAnchor.OFFHAND);
    }

    public static Map<String, VanillaAnchor> snapshot() {
        return Map.copyOf(assignments);
    }

    public static void replace(Map<String, VanillaAnchor> replacements) {
        // Publish a complete immutable snapshot for readers on either side.
        assignments = Map.copyOf(replacements);
    }

    public static Map<String, VanillaAnchor> fromGroups(Map<VanillaAnchor, List<String>> groups) {
        Map<String, VanillaAnchor> result = new LinkedHashMap<>();
        groups.forEach((anchor, identifiers) -> identifiers.forEach(id -> result.put(id, anchor)));
        return result;
    }

    private static Map<String, VanillaAnchor> defaults() {
        Map<VanillaAnchor, List<String>> groups = new EnumMap<>(VanillaAnchor.class);
        groups.put(VanillaAnchor.HEAD, List.of("head", "hat", "face", "eyes", "glasses", "mask"));
        groups.put(VanillaAnchor.CHEST, List.of("back", "body", "cape", "charm", "necklace"));
        groups.put(VanillaAnchor.LEGS, List.of("belt", "legs"));
        groups.put(VanillaAnchor.FEET, List.of("feet", "shoes", "anklet"));
        groups.put(VanillaAnchor.OFFHAND,
                List.of("hand", "hands", "glove", "gloves", "bracelet", "ring", "shield", "offhand"));
        return fromGroups(groups);
    }
}
