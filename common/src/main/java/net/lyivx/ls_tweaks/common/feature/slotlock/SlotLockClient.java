package net.lyivx.ls_tweaks.common.feature.slotlock;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Client-only slot locking: Ctrl-Click to lock player inventory slots; overlay renderer; network sync. */
public final class SlotLockClient {
    private static boolean modeEnabled = false;
    private static int lockMask = 0; // bits 0..35
    private static final net.minecraft.world.item.ItemStack[] SIGNATURES = new net.minecraft.world.item.ItemStack[36];
    private static boolean prevRight = false;

    public static void register() {
        // Ctrl-Click detection while in player inventory or creative inventory
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (client == null || client.player == null) return;
            if (!ConfigProvider.slotLockEnabled()) return;
            Screen scr = client.screen;
            if (!(scr instanceof AbstractContainerScreen<?> cs)) return;

            long win = client.getWindow().getWindow();
            boolean ctrl = InputConstants.isKeyDown(win, InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(win, InputConstants.KEY_RCONTROL);
            boolean mid = org.lwjgl.glfw.GLFW.glfwGetMouseButton(win, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            boolean edge = mid && !prevRight;
            prevRight = mid;
            if (!(ctrl && edge && modeEnabled)) return;

            double mx = client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getWidth();
            double my = client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getHeight();
            // Find any player-inventory slot under mouse on this screen
            Slot over = getPlayerSlotUnderMouse(cs, (int)mx, (int)my);
            if (over == null) { System.out.println("[LS Tweaks][SlotLock] no player slot under mouse"); return; }
            int invIndex = over.getContainerSlot();
            if (invIndex < 0 || invIndex >= 36) return; // only handle 0..35
            boolean willLock = (lockMask & (1 << invIndex)) == 0;
            // optimistic local update
            if (willLock) {
                lockMask |= (1 << invIndex);
                SIGNATURES[invIndex] = over.getItem().copy();
            } else {
                lockMask &= ~(1 << invIndex);
                SIGNATURES[invIndex] = null;
            }
            // send to server
            NetworkManager.sendToServer(new QolNet.SlotLockToggleC2SPayload(invIndex, willLock));
            System.out.println("[LS Tweaks][SlotLock] toggled invIndex=" + invIndex + " lock=" + willLock);
        });
    }

    public static void updateMask(int mask) { lockMask = mask; }
    public static void updateMaskAndSigs(int mask, java.util.List<net.minecraft.world.item.ItemStack> sigs) {
        lockMask = mask;
        for (int i = 0; i < SIGNATURES.length; i++) {
            SIGNATURES[i] = (i < sigs.size() && sigs.get(i) != null && !sigs.get(i).isEmpty()) ? sigs.get(i) : null;
        }
    }
    public static boolean isLockedPlayerSlot(int inventoryIndex) { return (lockMask & (1 << inventoryIndex)) != 0; }
    public static boolean isModeEnabled() { return modeEnabled; }
    public static void toggleMode() { modeEnabled = !modeEnabled; }

    /** Returns true if a mouse click would place a non-matching item into a locked player slot. */
    public static boolean shouldBlockPlacement(AbstractContainerScreen<?> cs, double mx, double my, int button) {
        if (!ConfigProvider.slotLockEnabled()) return false;
        if (button != 0 && button != 1) return false; // left or right click only
        Slot over = getPlayerSlotUnderMouse(cs, (int)mx, (int)my);
        if (over == null) return false;
        int idx = over.getContainerSlot();
        if (idx < 0 || idx >= 36) return false;
        if (!isLockedPlayerSlot(idx)) return false;
        var carried = cs.getMenu().getCarried();
        if (carried == null || carried.isEmpty()) return false;
        var sig = SIGNATURES[idx];
        if (sig == null) return false;
        return !net.minecraft.world.item.ItemStack.isSameItemSameComponents(carried, sig);
    }

    public static void drawOverlay(AbstractContainerScreen<?> cs, GuiGraphics g) {
        if (!ConfigProvider.slotLockEnabled() || !ConfigProvider.slotLockShowIcons()) return;
        for (Slot s : cs.getMenu().slots) {
            if (!(s.container instanceof Inventory)) continue;
            int idx = s.getContainerSlot();
            if (idx < 0 || idx >= 36) continue;
            boolean locked = isLockedPlayerSlot(idx);
            // ghost item when slot is empty but has a signature
            var sig = SIGNATURES[idx];
            if (sig != null && s.getItem().isEmpty()) {
                int gx = getLeft(cs) + s.x;
                int gy = getTop(cs) + s.y;
                renderGhostItem(g, sig, gx, gy);
            }
            // draw lock icon LAST so it appears above everything in the slot
            if (locked) {
                int x = getLeft(cs) + s.x + 12;
                int y = getTop(cs) + s.y - 2;
                g.blit(RenderType::guiTexturedOverlay, ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/widgets/lock_overlay.png"), x, y, 0, 0, 6, 6, 6, 6);
            }
        }
    }

    private static void renderGhostItem(GuiGraphics g, net.minecraft.world.item.ItemStack stack, int x, int y) {
        g.renderFakeItem(stack, x, y);
        // Dim the item to indicate ghosted state
        g.fill(x, y, x + 16, y + 16, 0x66000000);
        // Draw a subtle white outline
        int c = 0x80FFFFFF;
        g.fill(x, y, x + 16, y + 1, c);
        g.fill(x, y + 15, x + 16, y + 16, c);
        g.fill(x, y, x + 1, y + 16, c);
        g.fill(x + 15, y, x + 16, y + 16, c);
    }

    /** Client-side helper for mixins: whether a stack is allowed into a locked slot index. */
    public static boolean allowsClientPlacement(int inventoryIndex, net.minecraft.world.item.ItemStack stack) {
        if (inventoryIndex < 0 || inventoryIndex >= SIGNATURES.length) return true;
        if (!isLockedPlayerSlot(inventoryIndex)) return true;
        var sig = SIGNATURES[inventoryIndex];
        if (sig == null) return false;
        return net.minecraft.world.item.ItemStack.isSameItemSameComponents(stack, sig);
    }

    /** Returns true if a number-key hotbar swap into the hovered player slot should be blocked. */
    public static boolean shouldBlockHotbarSwap(AbstractContainerScreen<?> cs, double mx, double my, int hotbarIndex) {
        if (!ConfigProvider.slotLockEnabled()) return false;
        if (hotbarIndex < 0 || hotbarIndex > 8) return false;
        Slot over = getPlayerSlotUnderMouse(cs, (int)mx, (int)my);
        if (over == null) return false;
        int idx = over.getContainerSlot();
        if (idx < 0 || idx >= 36) return false;
        if (!isLockedPlayerSlot(idx)) return false;
        var sig = SIGNATURES[idx];
        if (sig == null) return true; // locked with no signature: block all
        var src = Minecraft.getInstance().player == null ? net.minecraft.world.item.ItemStack.EMPTY : Minecraft.getInstance().player.getInventory().getItem(hotbarIndex);
        if (src.isEmpty()) return false; // swapping empty hotbar is fine
        return !net.minecraft.world.item.ItemStack.isSameItemSameComponents(src, sig);
    }

    // kept intentionally for parity with other client classes; may be referenced reflectively
    private static boolean isPlayerInventory(Screen s) { return (s instanceof InventoryScreen) || (s instanceof CreativeModeInventoryScreen); }
    private static Slot getPlayerSlotUnderMouse(AbstractContainerScreen<?> cs, int mx, int my) {
        try {
            var m = AbstractContainerScreen.class.getDeclaredMethod("getSlotUnderMouse");
            m.setAccessible(true);
            Object o = m.invoke(cs);
            if (o instanceof Slot s && s.container instanceof Inventory) return s;
        } catch (Throwable ignored) {
        }
        // Fallback scan with bounds
        for (Slot s : cs.getMenu().slots) if (s.container instanceof Inventory && isPoint(cs, s.x, s.y, 16, 16, mx, my)) return s;
        return null;
    }
    private static boolean isPoint(AbstractContainerScreen<?> cs, int x, int y, int w, int h, int px, int py) {
        int left = getLeft(cs), top = getTop(cs);
        int rx = px - left, ry = py - top;
        return rx >= x && ry >= y && rx < x + w && ry < y + h;
        }
    private static int getLeft(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("leftPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.width - 176) / 2; }
    }
    private static int getTop(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("topPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.height - 166) / 2; }
    }

    private SlotLockClient() {}
}


