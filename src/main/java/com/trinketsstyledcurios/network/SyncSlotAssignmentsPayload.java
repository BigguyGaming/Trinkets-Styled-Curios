/* Sends the server's Curios-to-anchor assignments to clients when datapacks synchronize. */
package com.trinketsstyledcurios.network;

import com.trinketsstyledcurios.client.ClientSlotAssignments;
import com.trinketsstyledcurios.data.SlotAssignments;
import com.trinketsstyledcurios.inventory.VanillaAnchor;
import com.trinketsstyledcurios.TrinketsStyledCurios;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncSlotAssignmentsPayload(Map<String, VanillaAnchor> assignments)
        implements CustomPacketPayload {
    public static final Type<SyncSlotAssignmentsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TrinketsStyledCurios.MOD_ID, "sync_slot_assignments"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSlotAssignmentsPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SyncSlotAssignmentsPayload decode(RegistryFriendlyByteBuf buffer) {
                    int size = buffer.readVarInt();
                    Map<String, VanillaAnchor> assignments = new LinkedHashMap<>();
                    for (int i = 0; i < size; i++) {
                        assignments.put(buffer.readUtf(64), buffer.readEnum(VanillaAnchor.class));
                    }
                    return new SyncSlotAssignmentsPayload(assignments);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, SyncSlotAssignmentsPayload payload) {
                    buffer.writeVarInt(payload.assignments.size());
                    payload.assignments.forEach((identifier, anchor) -> {
                        buffer.writeUtf(identifier, 64);
                        buffer.writeEnum(anchor);
                    });
                }
            };

    public static SyncSlotAssignmentsPayload current() {
        return new SyncSlotAssignmentsPayload(SlotAssignments.snapshot());
    }

    public static void handle(SyncSlotAssignmentsPayload payload, IPayloadContext context) {
        // Apply the server's assignment snapshot on the client thread.
        context.enqueueWork(() -> ClientSlotAssignments.apply(payload.assignments));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
