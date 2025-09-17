package net.lyivx.ls_tweaks.common.feature.sort.client;

import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelist;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelistData;
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
        // e.g. sorting_a_button.png / sorting_a_button_highlight.png
        String file = name + (hl ? "_highlight" : "") + ".png";
        return ResourceLocation.fromNamespaceAndPath("ls_tweaks", BASE + file);
    }

    /** Build all buttons appropriate for the current screen with layout rules applied. */
    public static List<IconButton> createSortButtons(Screen screen) {
        List<IconButton> out = new ArrayList<>();
        if (!ConfigProvider.sortingEnabled()) return out;
        if (!(screen instanceof AbstractContainerScreen<?> cs)) {
            // Non-container UI — nothing to place against
            return out;
        }

        boolean isPlayerInvUI = isPlayerInventoryScreen(screen);
        boolean isContainerUI = !isPlayerInvUI; // inventory/creative are also container screens
        boolean containerAllowed = isContainerUI && SortingWhitelist.isAllowed(cs.getMenu(), cs);

        // sizes/placement
        final int bw = 12, bh = 10; // icon size in px (your textures)
        final int gapX = 6;         // horizontal space between icons
        final int gapY = 3;         // vertical gap above slot (horizontal layout)
        final int vGap = 4;         // vertical space between icons (vertical layout)
        final int rightGap = 4;     // horizontal gap to the right of the slot (vertical layout)
        final int slotSize = 18;    // vanilla slot size

        // -------- helpers to add a set of buttons in the chosen layout --------

        // Horizontal: anchor is the CATEGORY button position (rightmost); A and Z go to the left.
        var placeHorizontalSet = (java.util.function.BiConsumer<Anchor, QolNet.SortC2SPayload.Target>) (anchor, target) -> {
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
            // Category (rightmost)
            out.add(new IconButton(
                    xCat, y, bw, bh,
                    tex("sorting_category_button", false),
                    tex("sorting_category_button", true),
                    Component.literal(labelFor(target) + " (Category)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.DEFAULT)
            ));
        };

        // Vertical: anchor is the TOPMOST button position (A); Z and Category go under it.
        var placeVerticalSet = (java.util.function.BiConsumer<Anchor, QolNet.SortC2SPayload.Target>) (anchor, target) -> {
            if (anchor == null) return;
            int x = anchor.x();
            int y = anchor.y();

            // A (top)
            out.add(new IconButton(
                    x, y, bw, bh,
                    tex("sorting_a_button", false),
                    tex("sorting_a_button", true),
                    Component.literal(labelFor(target) + " (A→Z)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_ASC)
            ));
            y += bh + vGap;

            // Z
            out.add(new IconButton(
                    x, y, bw, bh,
                    tex("sorting_z_button", false),
                    tex("sorting_z_button", true),
                    Component.literal(labelFor(target) + " (Z→A)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_DESC)
            ));
            y += bh + vGap;

            // Category (bottom)
            out.add(new IconButton(
                    x, y, bw, bh,
                    tex("sorting_category_button", false),
                    tex("sorting_category_button", true),
                    Component.literal(labelFor(target) + " (Category)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.DEFAULT)
            ));
        };

        // ---- INVENTORY (anchor at player slot 17 for HORIZONTAL; to the right of slot 17 for VERTICAL)
        SortingWhitelistData.Layout invLayout = SortingWhitelist.getLayout(null, cs); // screen-based (lets datapack pick layout for inventory/creative)
        Anchor invAnchor = (invLayout == SortingWhitelistData.Layout.VERTICAL)
                ? anchorRightOfPlayerSlot(cs, 17, bw, bh, rightGap, slotSize)
                : anchorAbovePlayerSlot(cs, 17, bw, bh, gapY, slotSize);

        if (isPlayerInvUI) {
            // Only inventory set on inventory screen
            if (invLayout == SortingWhitelistData.Layout.VERTICAL) {
                placeVerticalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            } else {
                placeHorizontalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            }
            return out;
        }

        // ---- CONTAINER (anchor at container's 8th slot; per-container layout from datapack)
        if (isContainerUI) {
            SortingWhitelistData.Layout contLayout = SortingWhitelist.getLayout(cs.getMenu(), cs);

            Anchor contAnchor = (contLayout == SortingWhitelistData.Layout.VERTICAL)
                    ? anchorRightOfContainerIndex(cs, 8, bw, bh, rightGap, slotSize)
                    : anchorAboveContainerIndex(cs, 8, bw, bh, gapY, slotSize);

            if (ConfigProvider.sortingAllowContainerSorting() && containerAllowed) {
                if (contLayout == SortingWhitelistData.Layout.VERTICAL) {
                    placeVerticalSet.accept(contAnchor, QolNet.SortC2SPayload.Target.CONTAINER);
                } else {
                    placeHorizontalSet.accept(contAnchor, QolNet.SortC2SPayload.Target.CONTAINER);
                }
            }

            // Always also show the player's inventory buttons on container screens,
            // using the inventory layout chosen by the datapack (screen class).
            if (invLayout == SortingWhitelistData.Layout.VERTICAL) {
                placeVerticalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            } else {
                placeHorizontalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            }
        }

        return out;
    }

    // ----- Anchor helpers -----

    /** Where to place a button. */
    private record Anchor(int x, int y) {}

    /** Horizontal layout: anchor above a player slot (Category button sits here). */
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

    /** Vertical layout: anchor to the right of a player slot (topmost A button sits here). */
    private static Anchor anchorRightOfPlayerSlot(AbstractContainerScreen<?> cs, int playerSlotIndex,
                                                  int bw, int bh, int rightGap, int slotSize) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        if (playerInv == null) return null;

        Slot target = findSlot(menu, s -> s.container == playerInv && s.getContainerSlot() == playerSlotIndex);
        if (target == null) return null;

        int left = guiLeft(cs);
        int top  = guiTop(cs);
        int slotAbsX = left + target.x;
        int slotAbsY = top  + target.y;

        int x = slotAbsX + slotSize + rightGap;
        int y = slotAbsY + ((slotSize - bh) / 2) - 1; // vertically centered to slot row
        return new Anchor(x, y);
    }

    /** Horizontal layout: anchor above a container slot (Category button sits here). */
    private static Anchor anchorAboveContainerIndex(AbstractContainerScreen<?> cs, int containerIndex,
                                                    int bw, int bh, int gapY, int slotSize) {
        Slot target = nthContainerSlot(cs, containerIndex);
        if (target == null) return null;

        int left = guiLeft(cs);
        int top  = guiTop(cs);
        int slotAbsX = left + target.x;
        int slotAbsY = top  + target.y;

        int xCat = (slotAbsX + (slotSize - bw) / 2) - 1;
        int y    = slotAbsY - (bh + gapY);
        return new Anchor(xCat, y);
    }

    /** Vertical layout: anchor to the right of a container slot (topmost A button sits here). */
    private static Anchor anchorRightOfContainerIndex(AbstractContainerScreen<?> cs, int containerIndex,
                                                      int bw, int bh, int rightGap, int slotSize) {
        Slot target = nthContainerSlot(cs, containerIndex);
        if (target == null) return null;

        int left = guiLeft(cs);
        int top  = guiTop(cs);
        int slotAbsX = left + target.x;
        int slotAbsY = top  + target.y;

        int x = slotAbsX + slotSize + rightGap;
        int y = slotAbsY + ((slotSize - bh) / 2) - 1;
        return new Anchor(x, y);
    }

    /** Find the Nth container slot (excluding player inventory slots). */
    private static Slot nthContainerSlot(AbstractContainerScreen<?> cs, int containerIndex) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (playerInv == null || s.container != playerInv) containerSlots.add(s);
        }
        if (containerSlots.isEmpty()) return null;
        int idx = Math.min(containerIndex, containerSlots.size() - 1);
        return containerSlots.get(idx);
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
                ConfigProvider.sortingMergeBeforeSort(),
                ConfigProvider.sortingUnstackablesLast()
        );
        QolDebug.log("Button: sort target=" + target + " mode=" + mode);
        NetworkManager.sendToServer(payload);

        mc.player.displayClientMessage(
                Component.literal("Sorting " + (target == QolNet.SortC2SPayload.Target.PLAYER ? "Inventory" : "Container")
                        + " [" + (mode == QolNet.SortC2SPayload.Mode.DEFAULT ? "Category" :
                        mode == QolNet.SortC2SPayload.Mode.ALPHA_ASC ? "A→Z" : "Z→A") + "]"), true);
    }
}
