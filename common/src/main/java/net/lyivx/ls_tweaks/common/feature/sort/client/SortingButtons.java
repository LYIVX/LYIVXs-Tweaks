package net.lyivx.ls_tweaks.common.feature.sort.client;

import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.QolConfig;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelist;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.ui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class SortingButtons {
    private SortingButtons() {}

    // ---- Texture helpers ----
    private static final String BASE = "textures/gui/widgets/";
    private static ResourceLocation tex(String name, boolean hl) {
        // e.g. sorting_a_button.png / sorting_a_button_highlighted.png
        String file = name + (hl ? "_highlight" : "") + ".png";
        return ResourceLocation.fromNamespaceAndPath("ls_tweaks", BASE + file);
    }

    /** Build all buttons appropriate for the current screen. */
    public static List<IconButton> createSortButtons(Screen screen) {
        List<IconButton> out = new ArrayList<>();
        if (!QolConfig.sorting().enabled) return out;
        if (!(screen instanceof AbstractContainerScreen<?> cs)) {
            // Non-container UI (rare) — nothing to place against
            return out;
        }

        boolean isPlayerInvUI = isPlayerInventoryScreen(screen);
        boolean isContainerUI = !isPlayerInvUI; // inv/creative are also container screens
        boolean containerAllowed = isContainerUI && SortingWhitelist.isAllowed(cs.getMenu(), cs);

        // sizes/placement
        final int bw = 12, bh = 10; // icon size
        final int gapX = 6;
        final int gapY = 3;         // vertical gap above slot
        final int slotSize = 18;    // vanilla slot size

        // -------- helper: place a set A · Z · Category anchored to the given "Category" (rightmost) x,y
        var placeSet = (java.util.function.BiConsumer<Anchor, QolNet.SortC2SPayload.Target>) (anchor, target) -> {
            if (anchor == null) return;
            int xCat = anchor.x();
            int y    = anchor.y();

            int xZ = xCat - gapX - bw;
            int xA = xZ   - gapX - bw;

            // A
            out.add(new IconButton(
                    xA, y, bw, bh,
                    tex("sorting_a_button", false),
                    tex("sorting_a_button", true),
                    Component.literal(labelFor(target) + " (A→Z)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_ASC)
            ));
            // Z
            out.add(new IconButton(
                    xZ, y, bw, bh,
                    tex("sorting_z_button", false),
                    tex("sorting_z_button", true),
                    Component.literal(labelFor(target) + " (Z→A)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_DESC)
            ));
            // Category
            out.add(new IconButton(
                    xCat, y, bw, bh,
                    tex("sorting_category_button", false),
                    tex("sorting_category_button", true),
                    Component.literal(labelFor(target) + " (Category)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.DEFAULT)
            ));
        };

        // ---- INVENTORY (anchor above player slot 17)
        Anchor invAnchor = anchorAbovePlayerSlot(cs, 17, bw, bh, gapY, slotSize);

        if (isPlayerInvUI) {
            // Only inventory set on inventory screen
            placeSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            return out;
        }

        // Container anchor: above container's 8th slot (index 8)
        if (isContainerUI) {
            Anchor contAnchor = anchorAboveContainerIndex(cs, 8, bw, bh, gapY, slotSize);
            if (QolConfig.sorting().allowContainerSorting && containerAllowed) {
                placeSet.accept(contAnchor, QolNet.SortC2SPayload.Target.CONTAINER);
            }
            placeSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
        }

        return out;
    }

    // ----- Anchor helpers -----

    /** Where to place the rightmost (Category) button. */
    private record Anchor(int x, int y) {}

    private static Anchor anchorAbovePlayerSlot(AbstractContainerScreen<?> cs, int playerSlotIndex,
                                                int bw, int bh, int gapY, int slotSize) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        if (playerInv == null) return null;

        Slot target = findSlot(menu, s -> s.container == playerInv && s.getContainerSlot() == playerSlotIndex);
        if (target == null) return null;

        int left = guiLeft(cs);
        int top  = guiTop(cs);
        int slotAbsX = left + target.x;
        int slotAbsY = top  + target.y;

        int xCat = (slotAbsX + (slotSize - bw) / 2) - 1;
        int y    = slotAbsY - (bh + gapY);
        return new Anchor(xCat, y);
    }

    private static Anchor anchorAboveContainerIndex(AbstractContainerScreen<?> cs, int containerIndex,
                                                    int bw, int bh, int gapY, int slotSize) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (playerInv == null || s.container != playerInv) containerSlots.add(s);
        }
        if (containerSlots.isEmpty()) return null;
        int idx = Math.min(containerIndex, containerSlots.size() - 1);
        Slot target = containerSlots.get(idx);

        int left = guiLeft(cs);
        int top  = guiTop(cs);
        int slotAbsX = left + target.x;
        int slotAbsY = top  + target.y;

        int xCat = (slotAbsX + (slotSize - bw) / 2) - 1;
        int y    = slotAbsY - (bh + gapY);
        return new Anchor(xCat, y);
    }

    private static int guiLeft(AbstractContainerScreen<?> cs) {
        // try protected field "leftPos"
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("leftPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            // fallback: center using imageWidth (176 default if reflection fails)
            return (cs.width - guiImageWidth(cs)) / 2;
        }
    }
    private static int guiTop(AbstractContainerScreen<?> cs) {
        // try protected field "topPos"
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("topPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            // fallback: center using imageHeight (166 default if reflection fails)
            return (cs.height - guiImageHeight(cs)) / 2;
        }
    }
    private static int guiImageWidth(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("imageWidth");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return 176; // vanilla default for most containers
        }
    }
    private static int guiImageHeight(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("imageHeight");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return 166; // vanilla default for most containers
        }
    }

    private static Slot findSlot(AbstractContainerMenu menu, Predicate<Slot> pred) {
        for (Slot s : menu.slots) if (pred.test(s)) return s;
        return null;
    }

    private static Inventory getPlayerInventoryFromMenu(AbstractContainerMenu menu) {
        // Find the player's Inventory instance among the menu's slots
        for (Slot s : menu.slots) {
            if (s.container instanceof Inventory inv) return inv;
        }
        return null;
    }

    // ----- Utility -----

    private static boolean isPlayerInventoryScreen(Screen s) {
        return (s instanceof InventoryScreen) || (s instanceof CreativeModeInventoryScreen);
    }

    private static String labelFor(QolNet.SortC2SPayload.Target t) {
        return t == QolNet.SortC2SPayload.Target.PLAYER ? "Inventory" : "Container";
    }

    private static void send(QolNet.SortC2SPayload.Target target, QolNet.SortC2SPayload.Mode mode) {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        var payload = new QolNet.SortC2SPayload(
                target, mode,
                QolConfig.sorting().mergeBeforeSort,
                QolConfig.sorting().unstackablesLast
        );
        QolDebug.log("Button: sort target=" + target + " mode=" + mode);
        NetworkManager.sendToServer(payload);

        mc.player.displayClientMessage(
                Component.literal("Sorting " + (target == QolNet.SortC2SPayload.Target.PLAYER ? "Inventory" : "Container")
                        + " [" + (mode == QolNet.SortC2SPayload.Mode.DEFAULT ? "Category" :
                        mode == QolNet.SortC2SPayload.Mode.ALPHA_ASC ? "A→Z" : "Z→A") + "]"), true);
    }
}