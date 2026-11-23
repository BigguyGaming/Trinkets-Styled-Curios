/* Applies first-launch defaults once per client config directory. */
package com.trinketsstyledcurios.client;

import com.trinketsstyledcurios.TrinketsStyledCurios;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import top.theillusivec4.curios.client.CuriosClientConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientFirstLaunch {
    private ClientFirstLaunch() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientFirstLaunch::initializeCuriosButton);
    }

    private static void initializeCuriosButton() {
        Path marker = FMLPaths.CONFIGDIR.get()
                .resolve(TrinketsStyledCurios.MOD_ID + "-curios-button-initialized");
        if (Files.exists(marker)) {
            return;
        }

        if (!CuriosClientConfig.CLIENT_SPEC.isLoaded()) {
            TrinketsStyledCurios.LOGGER.warn("Curios client config is not loaded. Button setup will retry on the next launch.");
            return;
        }

        try {
            var button = CuriosClientConfig.CLIENT.enableButton;
            if (button.get()) {
                button.set(false);
                CuriosClientConfig.CLIENT_SPEC.save();
            }
            // Record completion only after saving so a failed setup can retry.
            Files.createFile(marker);
        } catch (IOException | RuntimeException exception) {
            TrinketsStyledCurios.LOGGER.warn("Could not finish first-launch Curios button setup", exception);
        }
    }
}
