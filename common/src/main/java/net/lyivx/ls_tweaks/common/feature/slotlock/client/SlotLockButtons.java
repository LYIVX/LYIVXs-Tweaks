package net.lyivx.ls_tweaks.common.feature.slotlock.client;

import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient;
import net.lyivx.ls_tweaks.common.ui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public final class SlotLockButtons {
    private SlotLockButtons() {}

    private static ResourceLocation tex(String name, boolean hl) {
        String file = name + (hl ? "_highlight" : "") + ".png";
        return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/widgets/" + file);
    }

    public static List<IconButton> create(Screen screen) {
        List<IconButton> out = new ArrayList<>();
        if (!ConfigProvider.slotLockEnabled()) return out;
        if (!(screen instanceof AbstractContainerScreen<?> cs)) return out;
        // Show on any screen containing the player's inventory (including containers and creative/inventory)

        final int bw = 12, bh = 10, rightGap = 2, slotSize = 18;
        Slot s = findPlayerSlot(cs, 17);
        if (s == null) return out;
        int left = getLeft(cs), top = getTop(cs);
        int x = left + s.x + slotSize + rightGap;
        int y = top + s.y + ((slotSize - bh) / 2) - 17;

        var btn = new IconButton(
                x, y, bw, bh,
                tex("lock_toggle_button", false), tex("lock_toggle_button", true),
                tooltip(),
                b -> {
                    SlotLockClient.toggleMode();
                    var mc = Minecraft.getInstance();
                    if (mc != null && mc.player != null)
                        mc.player.displayClientMessage(Component.literal("Slot Lock " + (SlotLockClient.isModeEnabled() ? "Enabled" : "Disabled")), true);
                    b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip()));
                }
        );
        out.add(btn);
        return out;
    }

    private static boolean isPlayerInventory(Screen s) {
        return (s instanceof InventoryScreen) || (s instanceof CreativeModeInventoryScreen);
    }
    private static Component tooltip() {
        return Component.literal("Slot Lock: " + (SlotLockClient.isModeEnabled() ? "ON" : "OFF") + "\nCtrl+Middle-Click a player slot");
    }
    private static Slot findPlayerSlot(AbstractContainerScreen<?> cs, int containerSlot) {
        for (Slot sl : cs.getMenu().slots) if (sl.container instanceof Inventory && sl.getContainerSlot() == containerSlot) return sl;
        return null;
    }
    private static int getLeft(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("leftPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.width - 176) / 2; }
    }
    private static int getTop(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("topPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.height - 166) / 2; }
    }
}


