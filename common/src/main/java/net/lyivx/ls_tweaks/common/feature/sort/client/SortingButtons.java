package net.lyivx.ls_tweaks.common.feature.sort.client;

import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.feature.sort.InventoryButtonLayoutData;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelist;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.ui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
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
        String file = name + (hl ? "_highlight" : "") + ".png";
        return ResourceLocation.fromNamespaceAndPath("ls_tweaks", BASE + file);
    }

    /** Build all buttons appropriate for the current screen. */
    public static List<IconButton> createSortButtons(Screen screen) {
        List<IconButton> out = new ArrayList<>();
        if (!ConfigProvider.sortingEnabled() || !ConfigProvider.uiShowSortingButtons()) {
            return java.util.Collections.emptyList();
        }
        if (!(screen instanceof AbstractContainerScreen<?> cs)) return out;

        boolean isPlayerInvUI = isPlayerInventoryScreen(screen);
        boolean isContainerUI = !isPlayerInvUI;
        boolean containerAllowed = isContainerUI && SortingWhitelist.isAllowed(cs.getMenu(), cs);

        // Config-driven behavior
        boolean minimized = ConfigProvider.sortingMinimizedDisplay();
        // Map default sort mode → payload mode
        QolNet.SortC2SPayload.Mode defaultMode = switch (ConfigProvider.defaultSortMode()) {
            case CATEGORY -> QolNet.SortC2SPayload.Mode.DEFAULT;
            case A_TO_Z -> QolNet.SortC2SPayload.Mode.ALPHA_ASC;
            case Z_TO_A -> QolNet.SortC2SPayload.Mode.ALPHA_DESC;
        };

        // sizes/placement
        final int bw = 12, bh = 10;
        final int gapX = 6;     // between icons (horizontal)
        final int gapY = 3;     // above slot (horizontal)
        final int vGap = 8;     // between icons (vertical)
        final int rightGap = 2; // to the right of slot (vertical)
        final int slotSize = 18;

        // --- helpers to place a set of buttons ---

        // Horizontal: anchor is Category (rightmost)
        var placeHorizontalSet = (java.util.function.BiConsumer<Anchor, QolNet.SortC2SPayload.Target>) (anchor, target) -> {
            if (anchor == null) return;
            int xCat = anchor.x();
            int y    = anchor.y();
            if (minimized) {
                String texName;
                String suffix;
                switch (defaultMode) {
                    case DEFAULT -> { texName = "sorting_category_button"; suffix = " (Category)"; }
                    case ALPHA_ASC -> { texName = "sorting_a_button"; suffix = " (A→Z)"; }
                    case ALPHA_DESC -> { texName = "sorting_z_button"; suffix = " (Z→A)"; }
                    default -> { texName = "sorting_category_button"; suffix = " (Category)"; }
                }
                out.add(new IconButton(xCat, y, bw, bh, tex(texName, false), tex(texName, true),
                        Component.literal(labelFor(target) + suffix),
                        btn -> send(target, defaultMode)));
                return;
            }
            int xZ = xCat - gapX - bw;
            int xA = xZ   - gapX - bw;
            out.add(new IconButton(xA, y, bw, bh, tex("sorting_a_button", false), tex("sorting_a_button", true),
                    Component.literal(labelFor(target) + " (A→Z)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_ASC)));
            out.add(new IconButton(xZ, y, bw, bh, tex("sorting_z_button", false), tex("sorting_z_button", true),
                    Component.literal(labelFor(target) + " (Z→A)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_DESC)));
            out.add(new IconButton(xCat, y, bw, bh, tex("sorting_category_button", false), tex("sorting_category_button", true),
                    Component.literal(labelFor(target) + " (Category)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.DEFAULT)));
        };

        // Vertical: anchor is A (topmost)
        var placeVerticalSet = (java.util.function.BiConsumer<Anchor, QolNet.SortC2SPayload.Target>) (anchor, target) -> {
            if (anchor == null) return;
            int x = anchor.x();
            int y = anchor.y();

            if (minimized) {
                String texName;
                String suffix;
                switch (defaultMode) {
                    case DEFAULT -> { texName = "sorting_category_button"; suffix = " (Category)"; }
                    case ALPHA_ASC -> { texName = "sorting_a_button"; suffix = " (A→Z)"; }
                    case ALPHA_DESC -> { texName = "sorting_z_button"; suffix = " (Z→A)"; }
                    default -> { texName = "sorting_category_button"; suffix = " (Category)"; }
                }
                out.add(new IconButton(x, y, bw, bh,
                        tex(texName, false), tex(texName, true),
                        Component.literal(labelFor(target) + suffix),
                        btn -> send(target, defaultMode)));
                return;
            }

            // Category (top)
            out.add(new IconButton(x, y, bw, bh,
                    tex("sorting_category_button", false), tex("sorting_category_button", true),
                    Component.literal(labelFor(target) + " (Category)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.DEFAULT)));

            y += bh + vGap;

            // A → Z (middle)
            out.add(new IconButton(x, y, bw, bh,
                    tex("sorting_a_button", false), tex("sorting_a_button", true),
                    Component.literal(labelFor(target) + " (A→Z)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_ASC)));

            y += bh + vGap;

            // Z → A (bottom)
            out.add(new IconButton(x, y, bw, bh,
                    tex("sorting_z_button", false), tex("sorting_z_button", true),
                    Component.literal(labelFor(target) + " (Z→A)"),
                    btn -> send(target, QolNet.SortC2SPayload.Mode.ALPHA_DESC)));
        };

        // ---------- INVENTORY BUTTONS (this is what we flip per-container) ----------
        // Default inventory layout is HORIZONTAL, but if a container is open and listed in the
        // InventoryButtonLayoutData vertical set, we flip the INVENTORY buttons to VERTICAL
        // to avoid overlapping that container's widgets. The container's own buttons never change.

        // Decide the INVENTORY layout for current screen
        boolean invVertical = false;
        if (isContainerUI) {
            // We are inside some container; get its menu id
            var menu = cs.getMenu();
            var id = BuiltInRegistries.MENU.getKey(menu.getType());
            String key = (id != null ? id.toString() : null);
            // also allow screen class matches if someone prefers that
            String scr = screen.getClass().getName();
            invVertical = (key != null && InventoryButtonLayoutData.inventoryButtonsVerticalFor(key))
                    || InventoryButtonLayoutData.inventoryButtonsVerticalFor(scr);
        } else {
            // plain inventory/creative UI — keep horizontal by default (or allow screen class toggle)
            String scr = screen.getClass().getName();
            invVertical = InventoryButtonLayoutData.inventoryButtonsVerticalFor(scr);
        }

        // Inventory anchors
        Anchor invAnchor = invVertical
                ? anchorRightOfPlayerSlot(cs, 17, bw, bh, rightGap, slotSize)
                : anchorAbovePlayerSlot(cs, 17, bw, bh, gapY, slotSize);

        if (isPlayerInvUI) {
            // Only INVENTORY set on inventory screen
            if (invVertical) placeVerticalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            else             placeHorizontalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            return out;
        }

        // ---------- CONTAINER BUTTONS (unchanged) ----------
        if (isContainerUI) {
            Anchor contAnchor = anchorAboveContainerIndex(cs, 8, bw, bh, gapY, slotSize); // unchanged
            if (ConfigProvider.sortingAllowContainerSorting() && containerAllowed) {
                placeHorizontalSet.accept(contAnchor, QolNet.SortC2SPayload.Target.CONTAINER);
            }

            // Also render the INVENTORY set on container screens, using our chosen invVertical
            if (invVertical) placeVerticalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
            else             placeHorizontalSet.accept(invAnchor, QolNet.SortC2SPayload.Target.PLAYER);
        }

        return out;
    }

    // ----- Anchor & slot helpers -----

    private record Anchor(int x, int y) {}

    private static Anchor anchorAbovePlayerSlot(AbstractContainerScreen<?> cs, int playerSlotIndex,
                                                int bw, int bh, int gapY, int slotSize) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        if (playerInv == null) return null;

        Slot target = findSlot(menu, s -> s.container == playerInv && s.getContainerSlot() == playerSlotIndex);
        if (target == null) return null;

        int left = guiLeft(cs), top = guiTop(cs);
        int slotAbsX = left + target.x, slotAbsY = top + target.y;

        int xCat = (slotAbsX + (slotSize - bw) / 2) - 1;
        int y    = slotAbsY - (bh + gapY);
        return new Anchor(xCat, y);
    }

    private static Anchor anchorRightOfPlayerSlot(AbstractContainerScreen<?> cs, int playerSlotIndex,
                                                  int bw, int bh, int rightGap, int slotSize) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        if (playerInv == null) return null;

        Slot target = findSlot(menu, s -> s.container == playerInv && s.getContainerSlot() == playerSlotIndex);
        if (target == null) return null;

        int left = guiLeft(cs), top = guiTop(cs);
        int slotAbsX = left + target.x, slotAbsY = top + target.y;

        int x = slotAbsX + slotSize + rightGap;
        int y = slotAbsY + ((slotSize - bh) / 2) - 1;
        return new Anchor(x, y);
    }

    private static Anchor anchorAboveContainerIndex(AbstractContainerScreen<?> cs, int containerIndex,
                                                    int bw, int bh, int gapY, int slotSize) {
        Slot target = nthContainerSlot(cs, containerIndex);
        if (target == null) return null;

        int left = guiLeft(cs), top = guiTop(cs);
        int slotAbsX = left + target.x, slotAbsY = top + target.y;

        int xCat = (slotAbsX + (slotSize - bw) / 2) - 1;
        int y    = slotAbsY - (bh + gapY);
        return new Anchor(xCat, y);
    }

    private static Slot nthContainerSlot(AbstractContainerScreen<?> cs, int containerIndex) {
        var menu = cs.getMenu();
        var playerInv = getPlayerInventoryFromMenu(menu);
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot s : menu.slots) if (playerInv == null || s.container != playerInv) containerSlots.add(s);
        if (containerSlots.isEmpty()) return null;
        int idx = Math.min(containerIndex, containerSlots.size() - 1);
        return containerSlots.get(idx);
    }

    private static int guiLeft(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("leftPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return (cs.width - guiImageWidth(cs)) / 2;
        }
    }
    private static int guiTop(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("topPos");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return (cs.height - guiImageHeight(cs)) / 2;
        }
    }
    private static int guiImageWidth(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("imageWidth");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return 176;
        }
    }
    private static int guiImageHeight(AbstractContainerScreen<?> cs) {
        try {
            var f = AbstractContainerScreen.class.getDeclaredField("imageHeight");
            f.setAccessible(true);
            return f.getInt(cs);
        } catch (Throwable ignore) {
            return 166;
        }
    }

    private static Slot findSlot(AbstractContainerMenu menu, Predicate<Slot> pred) {
        for (Slot s : menu.slots) if (pred.test(s)) return s;
        return null;
    }

    private static Inventory getPlayerInventoryFromMenu(AbstractContainerMenu menu) {
        for (Slot s : menu.slots) if (s.container instanceof Inventory inv) return inv;
        return null;
    }

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

    public static boolean isInventoryColumnVertical(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> cs)) return false;

        // 1) By screen class (always safe)
        String scr = screen.getClass().getName();
        boolean byScreen = InventoryButtonLayoutData.inventoryButtonsVerticalFor(scr);

        // 2) By menu type (can throw for Inventory/Creative)
        boolean byMenu = false;
        try {
            var type = cs.getMenu().getType(); // may throw UnsupportedOperationException
            var id   = net.minecraft.core.registries.BuiltInRegistries.MENU.getKey(type);
            if (id != null) {
                byMenu = InventoryButtonLayoutData.inventoryButtonsVerticalFor(id.toString());
            }
        } catch (UnsupportedOperationException ignored) {
            // Happens for player inventory / creative inventory. Fall back to screen rule.
        } catch (Throwable ignored) {
            // Be defensive against any other client-only menu quirks.
        }

        return byMenu || byScreen;
    }

    /** How many buttons Sorting actually showed in the INVENTORY column on this screen. */
    public static int visibleInventoryColumnCount(Screen screen) {
        if (!ConfigProvider.sortingEnabled() || !ConfigProvider.uiShowSortingButtons()) return 0;
        // In minimized mode, only the default button is shown; otherwise we show 3 (Category, A→Z, Z→A)
        return ConfigProvider.sortingMinimizedDisplay() ? 1 : 3;
    }
}