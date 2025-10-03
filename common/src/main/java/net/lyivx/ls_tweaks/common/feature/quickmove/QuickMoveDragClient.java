package net.lyivx.ls_tweaks.common.feature.quickmove;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/** Client handler to upgrade Shift-click to Shift-drag quick-move across items into the open container. */
public final class QuickMoveDragClient {
    private static final Set<Integer> processedThisDrag = new HashSet<>();

    public static void register() {
        
        // Track mouse drag across container screens
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (client == null) return;
            if (!ConfigProvider.quickMoveDragEnabled()) {
                
                processedThisDrag.clear();
                return;
            }

            Screen scr = client.screen;
            if (!(scr instanceof AbstractContainerScreen<?> cs)) {
                
                processedThisDrag.clear();
                return;
            }

            long window = client.getWindow().getWindow();
            boolean shiftDown = InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                    || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);

            boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

            // Avoid interfering with vanilla carry/drag-split: only when not carrying an item
            if (client.player != null && client.player.containerMenu.getCarried() != null && !client.player.containerMenu.getCarried().isEmpty()) {
                
                processedThisDrag.clear();
                return;
            }

            if (shiftDown && leftDown) {
                // Compute scaled mouse
                double mx = client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getWidth();
                double my = client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getHeight();
                Slot over = getSlotUnderMouse(cs, (int)mx, (int)my);
                
                boolean isInv = over != null && isPlayerInventorySlot(cs.getMenu(), over);
                boolean isCont = over != null && !isInv; // any non-player slot considered container
                if (over != null && !processedThisDrag.contains(over.index) && over.hasItem() && (isInv || isCont)) {
                    // Respect slot locks for player inventory indices
                    if (isInv && SlotLockClient.isLockedPlayerSlot(over.getContainerSlot())) {
                        processedThisDrag.add(over.index);
                        return;
                    }
                    // Send to server to perform the quick move (networked, like other features)
                    dev.architectury.networking.NetworkManager.sendToServer(new net.lyivx.ls_tweaks.common.network.QolNet.QuickMoveC2SPayload(over.index));
                    processedThisDrag.add(over.index);
                }
            } else {
                
                processedThisDrag.clear();
            }
        });
    }

    private static Slot getSlotUnderMouse(AbstractContainerScreen<?> cs, int mouseX, int mouseY) {
        for (Slot s : cs.getMenu().slots) if (isPointInRegion(cs, s.x, s.y, 16, 16, mouseX, mouseY)) return s;
        return null;
    }

    private static boolean isPlayerInventorySlot(AbstractContainerMenu menu, Slot s) {
        return s != null && s.container instanceof Inventory;
    }

    private static boolean isPointInRegion(AbstractContainerScreen<?> cs, int x, int y, int w, int h, int px, int py) {
        int left = getGuiLeft(cs), top = getGuiTop(cs);
        px -= left; py -= top;
        return px >= x && py >= y && px < x + w && py < y + h;
    }

    private static int getGuiLeft(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("leftPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignored) {
            return (cs.width - 176) / 2;
        }
    }
    private static int getGuiTop(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("topPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignored) {
            return (cs.height - 166) / 2;
        }
    }

    private QuickMoveDragClient() {}
}


