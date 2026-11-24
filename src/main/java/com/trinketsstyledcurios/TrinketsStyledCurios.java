/* Registers the mod's datapack, networking, and client tooltip hooks. */
package com.trinketsstyledcurios;

import com.mojang.logging.LogUtils;
import com.trinketsstyledcurios.client.ClientConfig;
import com.trinketsstyledcurios.client.ClientFirstLaunch;
import com.trinketsstyledcurios.client.ClientTooltipHandler;
import com.trinketsstyledcurios.data.SlotAssignmentReloadListener;
import com.trinketsstyledcurios.network.SyncSlotAssignmentsPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

@Mod(TrinketsStyledCurios.MOD_ID)
public final class TrinketsStyledCurios {
    public static final String MOD_ID = "trinkets_styled_curios";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrinketsStyledCurios(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::syncDatapacks);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientConfig.register(modContainer);
            modEventBus.addListener(ClientFirstLaunch::onClientSetup);
            NeoForge.EVENT_BUS.addListener(ClientTooltipHandler::onItemTooltip);
        }
        LOGGER.info("Loading Trinkets Styled Curios");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(SyncSlotAssignmentsPayload.TYPE,
                SyncSlotAssignmentsPayload.STREAM_CODEC, SyncSlotAssignmentsPayload::handle);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SlotAssignmentReloadListener());
    }

    private void syncDatapacks(OnDatapackSyncEvent event) {
        SyncSlotAssignmentsPayload payload = SyncSlotAssignmentsPayload.current();
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}
