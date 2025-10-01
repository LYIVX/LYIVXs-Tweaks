package net.lyivx.ls_tweaks.neoforge;

import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.lyivx.ls_tweaks.client.LYIVXs_tweaksClient;
import net.lyivx.ls_tweaks.common.config.ClientConfigHelper;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.quickstack.client.QuickStackButtons;
import net.lyivx.ls_tweaks.common.feature.slotlock.client.SlotLockButtons;
import net.lyivx.ls_tweaks.common.feature.sort.client.SortingButtons;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed;
import net.lyivx.ls_tweaks.client.overlay.OverlayHooks;

@Mod(LYIVXs_tweaks.MOD_ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LYIVXs_tweaksNeoForgeClient {
    private static IEventBus modEventBus;
    public static KeyMapping OPEN_CONFIG;


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
        Screen screen = event.getScreen();
        if (!(screen instanceof AbstractContainerScreen<?>) && !isPlayerInventory(screen)) return;

        addSortButtons(event, screen);
        addQuickButtons(event, screen);
        addSlotLockButtons(event, screen);
    }

    private static void addSortButtons(ScreenEvent.Init.Post event, Screen screen) {
        if (!ConfigProvider.sortingEnabled() && !ConfigProvider.uiShowSortingButtons()) return;
        for (var b : SortingButtons.createSortButtons(screen)) event.addListener(b);
    }

    private static void addQuickButtons(ScreenEvent.Init.Post event, Screen screen) {
        if (!ConfigProvider.quickStackEnabled() && ConfigProvider.uiShowQuickStackButtons()) return;
        for (var b : QuickStackButtons.create(screen)) event.addListener(b);
    }

    private static void addSlotLockButtons(ScreenEvent.Init.Post event, Screen screen) {
        if (!ConfigProvider.slotLockEnabled()) return;
        for (var b : SlotLockButtons.create(screen)) event.addListener(b);
    }

    private static boolean isPlayerInventory(Screen s) {
        return s instanceof InventoryScreen
                || s instanceof CreativeModeInventoryScreen;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (OPEN_CONFIG != null && OPEN_CONFIG.consumeClick()) {
            ClientConfigHelper.openConfigScreen();
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> cs) {
            SlotLockClient.drawOverlay(cs, event.getGuiGraphics());
            OverlayHooks.render(cs, event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onMousePre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> cs)) return;
        if (SlotLockClient.shouldBlockPlacement(cs, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
        if (OverlayHooks.mouseClick(cs, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> cs)) return;
        if (SlotLockClient.shouldBlockPlacement(cs, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
        // Let overlay handle after release as well, opening screens if needed
        if (OverlayHooks.mouseClick(cs, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyPre(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> cs)) return;
        int key = event.getKeyCode();
        if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_1 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
            int hotbar = key - org.lwjgl.glfw.GLFW.GLFW_KEY_1;
            double mx = net.minecraft.client.Minecraft.getInstance().mouseHandler.xpos() * (double)net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)net.minecraft.client.Minecraft.getInstance().getWindow().getWidth();
            double my = net.minecraft.client.Minecraft.getInstance().mouseHandler.ypos() * (double)net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)net.minecraft.client.Minecraft.getInstance().getWindow().getHeight();
            if (SlotLockClient.shouldBlockHotbarSwap(cs, mx, my, hotbar)) {
                event.setCanceled(true);
            }
        }
        
        // Handle overlay keyboard input
        if (OverlayHooks.keyPressed(cs, event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }
    
    // Character input is now handled by ScreenMixin for both platforms
}
