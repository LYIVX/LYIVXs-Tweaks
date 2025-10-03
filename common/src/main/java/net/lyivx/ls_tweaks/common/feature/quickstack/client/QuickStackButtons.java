package net.lyivx.ls_tweaks.common.feature.quickstack.client;

import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.quickstack.QuickStackCommon;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelist;
import net.lyivx.ls_tweaks.common.feature.sort.client.SortingButtons;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.ui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public final class QuickStackButtons {

    private static final String BASE = "textures/gui/widgets/";
    private static ResourceLocation tex(String name, boolean hl) {
        String file = name + (hl ? "_highlight" : "") + ".png";
        return ResourceLocation.fromNamespaceAndPath("ls_tweaks", BASE + file);
    }
    public static List<IconButton> create(Screen screen) {
        List<IconButton> out = new ArrayList<>();
        if (!ConfigProvider.quickStackEnabled()) return out;
        if (!ConfigProvider.uiShowQuickStackButtons()) return out;
        if (!(screen instanceof AbstractContainerScreen<?> cs)) return out;

        boolean isPlayerInvUI = (screen instanceof InventoryScreen) || (screen instanceof CreativeModeInventoryScreen);
        boolean isContainerUI = !isPlayerInvUI;
        boolean containerAllowed = isContainerUI && SortingWhitelist.isAllowed(cs.getMenu(), cs);
        if (!containerAllowed && !isPlayerInvUI) return out;

        // --- CONSTANTS (match your SortingButtons sizing/gaps) ---
        final int bw = 12, bh = 10;
        final int slotSize = 18;
        final int gapAboveSlot = 4;
        final int gapRightOfSlot = 2;
        final int vGap = 8;
        final int stackGap = 2; // small extra gap between Sorting column and Quick-Stack column

        // --- We ALWAYS do a vertical column for Quick-Stack ---
        // Anchor to your INVENTORY column (same as Sorting does: above player slot 17)
        Anchor invAnchor = anchorRightOfPlayerSlot(cs, 17, bw, bh, gapRightOfSlot, slotSize);
        int x = invAnchor != null ? invAnchor.x() : 8;
        int y = invAnchor != null ? invAnchor.y() : 8;

        // If Sorting buttons are shown AND vertical, push Quick-Stack below them.
        if (ConfigProvider.sortingEnabled() && ConfigProvider.uiShowSortingButtons()) {
            int sortInventoryCount = SortingButtons.visibleInventoryColumnCount(screen);
            boolean sortVertical = SortingButtons.isInventoryColumnVertical(screen);
            if (sortInventoryCount > 0 && sortVertical) {
                y += sortInventoryCount * (bh + vGap) + stackGap;
            }
        }

        // --- Build Quick-Stack (Inventory → Container) ---
        out.add(new IconButton(
                x, y, bw, bh,
                tex("quickstack_inv_match_button", false), tex("quickstack_inv_match_button", true),
                Component.literal("Inventory → Container (Quick-Stack)"),
                btn -> send(QuickStackCommon.Direction.PLAYER_TO_CONTAINER, QuickStackCommon.Mode.MATCHING_ONLY)
        ));
        y += bh + vGap;
        out.add(new IconButton(
                x, y, bw, bh,
                tex("quickstack_inv_all_button", false), tex("quickstack_inv_all_button", true),
                Component.literal("Inventory → Container (Move All)"),
                btn -> send(QuickStackCommon.Direction.PLAYER_TO_CONTAINER, QuickStackCommon.Mode.MOVE_ALL)
        ));

        // --- Build Quick-Stack (Container → Inventory) stacked in its own column above container slot(s) ---
        if (containerAllowed) {
            Anchor contAnchor = anchorRightOfContainerIndex(cs, 8, bw, bh, gapRightOfSlot, slotSize);
            int cx = contAnchor != null ? contAnchor.x() : x; // fallback to inv column if weird screen
            int cy = contAnchor != null ? contAnchor.y() : (y + bh + vGap + stackGap);

            out.add(new IconButton(
                    cx, cy, bw, bh,
                    tex("quickstack_cont_match_button", false), tex("quickstack_cont_match_button", true),
                    Component.literal("Container → Inventory (Quick-Stack)"),
                    btn -> send(QuickStackCommon.Direction.CONTAINER_TO_PLAYER, QuickStackCommon.Mode.MATCHING_ONLY)
            ));
            cy += bh + vGap;
            out.add(new IconButton(
                    cx, cy, bw, bh,
                    tex("quickstack_cont_all_button", false), tex("quickstack_cont_all_button", true),
                    Component.literal("Container → Inventory (Move All)"),
                    btn -> send(QuickStackCommon.Direction.CONTAINER_TO_PLAYER, QuickStackCommon.Mode.MOVE_ALL)
            ));
        }

        return out;
    }


    // ----- Anchor helpers (same approach as SortingButtons) -----
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
            int x = (slotAbsX + (slotSize - bw) / 2) - 1;
            int y = slotAbsY - (bh + gapY);
            return new Anchor(x, y);
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
            int x = (slotAbsX + (slotSize - bw) / 2) - 1;
            int y = slotAbsY - (bh + gapY);
            return new Anchor(x, y);
        }

    private static Anchor anchorRightOfContainerIndex(AbstractContainerScreen<?> cs, int containerIndex,
                                                      int bw, int bh, int rightGap, int slotSize) {
        Slot target = nthContainerSlot(cs, containerIndex);
        if (target == null) return null;

        int left = guiLeft(cs), top = guiTop(cs);
        int slotAbsX = left + target.x, slotAbsY = top + target.y;

        int x = slotAbsX + slotSize + rightGap;
        int y = slotAbsY + ((slotSize - bh) / 2) - 1;

        return new Anchor(x, y);
    }

        private static Slot nthContainerSlot(AbstractContainerScreen<?> cs, int containerIndex) {
            var menu = cs.getMenu();
            var playerInv = getPlayerInventoryFromMenu(menu);
            java.util.List<Slot> containerSlots = new java.util.ArrayList<>();
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

        private static Slot findSlot(AbstractContainerMenu menu, java.util.function.Predicate<Slot> pred) {
            for (Slot s : menu.slots) if (pred.test(s)) return s;
            return null;
        }
        private static net.minecraft.world.entity.player.Inventory getPlayerInventoryFromMenu(AbstractContainerMenu menu) {
            for (Slot s : menu.slots) if (s.container instanceof net.minecraft.world.entity.player.Inventory inv) return inv;
            return null;
        }

    private static void send(QuickStackCommon.Direction dir, QuickStackCommon.Mode mode) {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        QolNet.sendQuickStack(dir, mode);
        mc.player.displayClientMessage(
                Component.literal((dir == QuickStackCommon.Direction.PLAYER_TO_CONTAINER ? "Moved to container" : "Moved to inventory")
                        + (mode == QuickStackCommon.Mode.MATCHING_ONLY ? " (matching)" : " (all)")), true);
    }

    private QuickStackButtons() {}
}
