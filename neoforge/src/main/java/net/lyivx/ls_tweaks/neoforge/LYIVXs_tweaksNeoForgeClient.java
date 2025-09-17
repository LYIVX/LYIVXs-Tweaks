package net.lyivx.ls_tweaks.neoforge;

import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.lyivx.ls_tweaks.client.LYIVXs_tweaksClient;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.sort.client.SortingButtons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@Mod(LYIVXs_tweaks.MOD_ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LYIVXs_tweaksNeoForgeClient {
    private static IEventBus modEventBus;

    public LYIVXs_tweaksNeoForgeClient(IEventBus bus) {
        modEventBus = bus;
        modEventBus.addListener(LYIVXs_tweaksNeoForgeClient::onClientSetup);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LYIVXs_tweaksClient.init();
        });
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!ConfigProvider.sortingEnabled()) return;

        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?> || isPlayerInventory(screen)) {
            for (var b : SortingButtons.createSortButtons(screen)) {
                event.addListener(b);
            }
        }
    }

    private static boolean isPlayerInventory(Screen s) {
        return s instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen
                || s instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
    }
}
