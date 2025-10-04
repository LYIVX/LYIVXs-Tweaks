package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Holder;

import java.util.List;
import java.util.function.Supplier;

/**
 * Helper class for easy widget creation
 */
public class Widgets {
    public enum PositionMode { NONE, TOP_LEFT, TOP_CENTER, TOP_RIGHT, MIDDLE_LEFT, CENTER, MIDDLE_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }
    // Universal positioning resolver for all widget types
        public static class Positioning {
        public static int[] resolve(PositionMode mode,
                                    int contentWidth, int contentHeight,
                                    int originX, int originY,
                                    int width, int height,
                                    int offsetX, int offsetY) {
            // Account for card/frame padding on right/bottom so anchors land exactly at visible edges
            final int padRight = 0;
            final int padBottom = 0;
            int cw = Math.max(0, contentWidth - padRight);
            int ch = Math.max(0, contentHeight - padBottom);
            // Use explicit formulas to avoid off-by-one from integer division round-down
            int baseX, baseY;
            switch (mode) {
                case TOP_LEFT:
                    baseX = 0; baseY = 0; break;
                case TOP_CENTER:
                    baseX = (cw - width) / 2; baseY = 0; break;
                case TOP_RIGHT:
                    baseX = cw - width; baseY = 0; break;
                case MIDDLE_LEFT:
                    baseX = 0; baseY = (ch - height) / 2; break;
                case CENTER:
                    baseX = (cw - width) / 2; baseY = (ch - height) / 2; break;
                case MIDDLE_RIGHT:
                    baseX = cw - width; baseY = (ch - height) / 2; break;
                case BOTTOM_LEFT:
                    baseX = 0; baseY = ch - height; break;
                case BOTTOM_CENTER:
                    baseX = (cw - width) / 2; baseY = ch - height; break;
                case BOTTOM_RIGHT:
                    baseX = cw - width; baseY = ch - height; break;
                case NONE:
                default:
                    baseX = 0; baseY = 0; break;
            }
            int nx = Math.max(0, baseX + originX + offsetX);
            int ny = Math.max(0, baseY + originY + offsetY);
            return new int[]{nx, ny};
        }
    }
    
    public static class AddSlot {
        private static java.util.List<ItemStack> holdersToStacks(java.util.List<Holder<Item>> holders) {
            java.util.ArrayList<ItemStack> list = new java.util.ArrayList<>();
            if (holders != null) for (Holder<Item> h : holders) if (h != null && h.isBound()) list.add(new ItemStack(h.value()));
            return list;
        }
        private static java.util.List<ItemStack> holdersToStacks(java.util.stream.Stream<Holder<Item>> holders) {
            if (holders == null) return java.util.Collections.emptyList();
            return holders.filter(java.util.Objects::nonNull).filter(Holder::isBound).map(h -> new ItemStack(h.value())).toList();
        }
        private static java.util.List<ItemStack> holdersToStacks(java.lang.Iterable<Holder<Item>> holders) {
            java.util.ArrayList<ItemStack> list = new java.util.ArrayList<>();
            if (holders != null) for (Holder<Item> h : holders) if (h != null && h.isBound()) list.add(new ItemStack(h.value()));
            return list;
        }
        public static SlotBuilder input(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.INPUT, true);
        }
        public static SlotBuilder input(int x, int y, Item item, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.INPUT, true);
        }
        public static SlotBuilder input(int x, int y, Holder<Item> holder, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.INPUT, true);
        }

        public static SlotBuilder input(int x, int y, Supplier<ItemStack> supplier, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicSupplier(supplier);
        }
        public static SlotBuilder input(int x, int y, Supplier<ItemStack> supplier, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicSupplier(supplier).cyclingSpeed(speed > 0 ? speed : 20);
        }
        // New overload: accept a list of stacks and bind by cell index
        public static SlotBuilder input(int x, int y, java.util.List<ItemStack> items, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(items);
        }
        public static SlotBuilder input(int x, int y, java.util.List<ItemStack> items, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(items).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, java.lang.Iterable<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder input(int x, int y, java.lang.Iterable<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder input(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, ItemStack stack, int speed, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.INPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, Item item, int speed, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.INPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, Holder<Item> holder, int speed, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.INPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder input(int x, int y, Ingredient ingredient, int salt, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).cyclingIngredient(ingredient, salt);
        }
        public static SlotBuilder input(int x, int y, Ingredient ingredient, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).cyclingIngredient(ingredient, 0);
        }
        // Unified object-based overload: supports List<Ingredient> and other dynamic sources
        public static SlotBuilder input(int x, int y, java.lang.Object source, int index) {
            if (source instanceof java.util.List<?> list) {
                java.util.List<?> raw = list;
                // Find first non-null element to determine list kind
                Object first = null;
                for (Object o : raw) { if (o != null) { first = o; break; } }
                if (first instanceof Ingredient) {
                    @SuppressWarnings("unchecked") java.util.List<Ingredient> ings = (java.util.List<Ingredient>) raw;
                    return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).ingredientList(ings);
                }
                if (first instanceof ItemStack) {
                    @SuppressWarnings("unchecked") java.util.List<ItemStack> stacks = (java.util.List<ItemStack>) raw;
                    return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(stacks);
                }
                if (first instanceof net.minecraft.core.Holder) {
                    @SuppressWarnings("unchecked") java.util.List<net.minecraft.core.Holder<Item>> holders = (java.util.List<net.minecraft.core.Holder<Item>>) raw;
                    return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders));
                }
            }
            if (source instanceof Ingredient ing) {
                return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).cyclingIngredient(ing, 0);
            }
            if (source instanceof ItemStack st) {
                return new SlotBuilder(x, y, st, index, SlotRole.INPUT, true);
            }
            if (source instanceof Item it) {
                return new SlotBuilder(x, y, new ItemStack((Item) it), index, SlotRole.INPUT, true);
            }
            if (source instanceof net.minecraft.core.Holder) {
                net.minecraft.core.Holder<Item> h = (net.minecraft.core.Holder<Item>) source;
                ItemStack s = h == null ? ItemStack.EMPTY : new ItemStack(h.value());
                return new SlotBuilder(x, y, s, index, SlotRole.INPUT, true);
            }
            if (source instanceof java.lang.Iterable<?>) {
                java.util.List<net.minecraft.core.Holder<Item>> holders = new java.util.ArrayList<>();
                for (Object o : (java.lang.Iterable<?>) source) if (o instanceof net.minecraft.core.Holder) holders.add((net.minecraft.core.Holder<Item>) o);
                return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders));
            }
            if (source instanceof java.util.stream.Stream<?>) {
                java.util.stream.Stream<?> s = (java.util.stream.Stream<?>) source;
                java.util.List<net.minecraft.core.Holder<Item>> holders = s.filter(o -> o instanceof net.minecraft.core.Holder).map(o -> (net.minecraft.core.Holder<Item>) o).toList();
                return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true).dynamicList(holdersToStacks(holders));
            }
            // Fallback
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.INPUT, true);
        }
        public static SlotBuilder input(int x, int y, java.lang.Object source, int speed, int index) {
            SlotBuilder b = input(x, y, source, index);
            return b.cyclingSpeed(speed > 0 ? speed : 20);
        }
        
        public static SlotBuilder output(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.OUTPUT, true);
        }
        public static SlotBuilder output(int x, int y, ItemStack stack, int speed, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.OUTPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder output(int x, int y, Item item, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.OUTPUT, true);
        }
        public static SlotBuilder output(int x, int y, Item item, int speed, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.OUTPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder output(int x, int y, Holder<Item> holder, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.OUTPUT, true);
        }
        public static SlotBuilder output(int x, int y, Holder<Item> holder, int speed, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.OUTPUT, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder output(int x, int y, java.util.function.Supplier<ItemStack> supplier, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicSupplier(supplier);
        }
        public static SlotBuilder output(int x, int y, java.util.function.Supplier<ItemStack> supplier, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicSupplier(supplier).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder output(int x, int y, java.util.List<ItemStack> items, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(items);
        }
        public static SlotBuilder output(int x, int y, java.util.List<ItemStack> items, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(items).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder outputHolders(int x, int y, java.util.List<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder output(int x, int y, java.lang.Iterable<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder output(int x, int y, java.lang.Iterable<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder outputHolderStream(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder output(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder output(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder output(int x, int y, Ingredient ingredient, int salt, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).cyclingIngredient(ingredient, salt);
        }
        public static SlotBuilder output(int x, int y, Ingredient ingredient, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.OUTPUT, true).cyclingIngredient(ingredient, 0);
        }
        
        public static SlotBuilder ghost(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.GHOST, true);
        }
        public static SlotBuilder ghost(int x, int y, ItemStack stack, int speed, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.GHOST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghost(int x, int y, Item item, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.GHOST, true);
        }
        public static SlotBuilder ghost(int x, int y, Item item, int speed, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.GHOST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghost(int x, int y, Holder<Item> holder, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.GHOST, true);
        }
        public static SlotBuilder ghost(int x, int y, Holder<Item> holder, int speed, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.GHOST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghost(int x, int y, java.util.List<ItemStack> items, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(items);
        }
        public static SlotBuilder ghost(int x, int y, java.util.List<ItemStack> items, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(items).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghostHolders(int x, int y, java.util.List<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder ghost(int x, int y, java.lang.Iterable<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder ghost(int x, int y, java.lang.Iterable<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghostHolderStream(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder ghost(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder ghost(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder ghost(int x, int y, Ingredient ingredient, int salt, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).cyclingIngredient(ingredient, salt);
        }
        public static SlotBuilder ghost(int x, int y, Ingredient ingredient, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.GHOST, true).cyclingIngredient(ingredient, 0);
        }
        
        public static SlotBuilder catalyst(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.CATALYST, true);
        }
        public static SlotBuilder catalyst(int x, int y, ItemStack stack, int speed, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.CATALYST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalyst(int x, int y, Item item, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.CATALYST, true);
        }
        public static SlotBuilder catalyst(int x, int y, Item item, int speed, int index) {
            return new SlotBuilder(x, y, new ItemStack(item), index, SlotRole.CATALYST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalyst(int x, int y, Holder<Item> holder, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.CATALYST, true);
        }
        public static SlotBuilder catalyst(int x, int y, Holder<Item> holder, int speed, int index) {
            ItemStack s = holder == null ? ItemStack.EMPTY : new ItemStack(holder.value());
            return new SlotBuilder(x, y, s, index, SlotRole.CATALYST, true).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalyst(int x, int y, java.util.List<ItemStack> items, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(items);
        }
        public static SlotBuilder catalyst(int x, int y, java.util.List<ItemStack> items, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(items).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalystHolders(int x, int y, java.util.List<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder catalyst(int x, int y, java.lang.Iterable<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder catalyst(int x, int y, java.lang.Iterable<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalystHolderStream(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder catalyst(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders));
        }
        public static SlotBuilder catalyst(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int speed, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).dynamicList(holdersToStacks(holders)).cyclingSpeed(speed > 0 ? speed : 20);
        }
        public static SlotBuilder catalyst(int x, int y, Ingredient ingredient, int salt, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).cyclingIngredient(ingredient, salt);
        }
        public static SlotBuilder catalyst(int x, int y, Ingredient ingredient, int index) {
            return new SlotBuilder(x, y, ItemStack.EMPTY, index, SlotRole.CATALYST, true).cyclingIngredient(ingredient, 0);
        }
        
        public static SlotBuilder fuel(int x, int y, int index) {
            return new SlotBuilder(x, y, SlotWidget.getDefaultFuelItem(null), index, SlotRole.CATALYST, true, true); // true = isFuel
        }

        // Builder class for fluent API that extends SlotWidget
        public static class SlotBuilder extends SlotWidget {
            // Uses Widgets.PositionMode
            private boolean noBackground = false;
            private ResourceLocation customBackground = null;
            private final SlotRole role;
            private final boolean isFuel;
            private Supplier<ItemStack> stackSupplier;
            private List<ItemStack> stackList;
            private Ingredient cyclingIngredient;
            private int cyclingSalt;
            private int cyclingSpeedTicks = 20;
            private java.util.List<Ingredient> ingredientList;
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE; // relative-to-container positioning using initial x/y as offset
            
            public SlotBuilder(int x, int y, ItemStack stack, int index, SlotRole role, boolean drawBackground) {
                super(role, x, y, drawBackground, role == SlotRole.OUTPUT, stack, index, null, false, false);
                this.role = role;
                this.isFuel = false;
            }
            
            public SlotBuilder(int x, int y, ItemStack stack, int index, SlotRole role, boolean drawBackground, boolean isFuel) {
                super(role, x, y, drawBackground, role == SlotRole.OUTPUT, stack, index, null, false, false);
                this.role = role;
                this.isFuel = isFuel;
            }

            private SlotBuilder dynamicSupplier(Supplier<ItemStack> supplier) { this.stackSupplier = supplier; return this; }
            private SlotBuilder dynamicList(List<ItemStack> stackList) { this.stackList = stackList; return this; }
            private SlotBuilder cyclingIngredient(Ingredient ingredient, int salt) { this.cyclingIngredient = ingredient; this.cyclingSalt = salt; return this; }

            // Position helpers: interpret current x/y as offsets relative to the chosen anchor
            public SlotBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public SlotBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public SlotBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public SlotBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public SlotBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public SlotBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public SlotBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public SlotBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public SlotBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }

            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }

            SlotWidget finalizePosition(int contentWidth, int contentHeight) {
                int resolvedX = this.x;
                int resolvedY = this.y;
                // Anchor using the actual visual size: 26x26 for output-styled slots, 18x18 otherwise
                int anchorW = this.isOutputStyle ? 26 : 18;
                int anchorH = this.isOutputStyle ? 26 : 18;
                // Output slots render slightly inset relative to 26x26; correct by nudging right +4, down +1
                int offsetX = this.x + (this.isOutputStyle ? 4 : 0);
                int offsetY = this.y + (this.isOutputStyle ? 4 : 0);
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, anchorW, anchorH, offsetX, offsetY);
                resolvedX = pos[0];
                resolvedY = pos[1];
                // Preserve SlotBuilder behavior (rendering/background selection) by returning another SlotBuilder with resolved coordinates
                // Debug trace
                try { if (this.positionMode != Widgets.PositionMode.NONE) System.out.println("[LS Tweaks][RV] Slot finalizePosition mode=" + this.positionMode + " -> (" + resolvedX + "," + resolvedY + ") cw=" + contentWidth + " ch=" + contentHeight); } catch (Throwable ignored) {}
                SlotBuilder copy = new SlotBuilder(resolvedX, resolvedY, this.stack, this.recipeJsonIndex, this.role, this.drawBackground, this.isFuel);
                // Copy builder-specific flags and dynamic sources
                if (this.noBackground) copy.noBackground();
                if (this.customBackground != null) copy.background(this.customBackground);
                if (this.stackSupplier != null) copy.dynamicSupplier(this.stackSupplier);
                if (this.stackList != null) copy.dynamicList(this.stackList);
                if (this.cyclingIngredient != null) copy.cyclingIngredient(this.cyclingIngredient, this.cyclingSalt);
                copy.cyclingSpeed(this.cyclingSpeedTicks);
                if (this.ingredientList != null) copy.ingredientList(this.ingredientList);
                // Clear positioning so we don't re-finalize
                copy.positionMode = Widgets.PositionMode.NONE;
                return copy;
            }
            
            public SlotBuilder noBackground() {
                this.noBackground = true;
                return this;
            }
            
            public SlotBuilder background(ResourceLocation background) {
                this.customBackground = background;
                return this;
            }

            private SlotBuilder cyclingSpeed(int ticksPerStep) {
                this.cyclingSpeedTicks = Math.max(1, ticksPerStep);
                return this;
            }
            private SlotBuilder ingredientList(java.util.List<Ingredient> ings) { this.ingredientList = ings; return this; }
            
            // Override render to use the builder's settings
            @Override
            public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
                SlotWidget widget;
                if (noBackground) {
                    widget = new SlotWidget(role, x, y, false, role == SlotRole.OUTPUT, stack, recipeJsonIndex, null, false, true, stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                } else if (customBackground != null) {
                    widget = new SlotWidget(role, x, y, true, role == SlotRole.OUTPUT, stack, recipeJsonIndex, customBackground, true, false, stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                } else {
                    // Use default slot creation based on role with proper textures
                    switch (role) {
                        case INPUT:
                            widget = new SlotWidget(SlotRole.INPUT, x, y, true, false, stack, recipeJsonIndex, 
                                ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png"), true, false,
                                stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                            break;
                        case OUTPUT:
                            widget = new SlotWidget(SlotRole.OUTPUT, x, y, true, true, stack, recipeJsonIndex, 
                                ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot_output.png"), true, false,
                                stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                            break;
                        case GHOST:
                            widget = new SlotWidget(SlotRole.GHOST, x, y, true, false, stack, recipeJsonIndex, 
                                ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png"), true, false,
                                stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                            break;
                        case CATALYST:
                            // For catalyst slots, use the appropriate texture
                            if (isFuel) {
                                widget = new SlotWidget(SlotRole.CATALYST, x, y, true, false, SlotWidget.getDefaultFuelItem(null), recipeJsonIndex,
                                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png"), true, false,
                                    stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                            } else {
                                widget = new SlotWidget(SlotRole.CATALYST, x, y, true, false, stack, recipeJsonIndex,
                                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png"), true, false,
                                    stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                            }
                            break;
                        default:
                            widget = new SlotWidget(role, x, y, true, role == SlotRole.OUTPUT, stack, recipeJsonIndex, null, false, false, stackSupplier, stackList, cyclingIngredient, cyclingSalt, cyclingSpeedTicks, true, ingredientList);
                    }
                }
                widget.render(g, font, baseX, baseY, mouseX, mouseY);
            }

            @Override
            public int getStackListSize() {
                return this.stackList == null ? 0 : this.stackList.size();
            }

            // Expose template cycling parameters to grids/scrolls
            @Override
            public int getCycleSpeedTicks() { return this.cyclingSpeedTicks; }
            @Override
            public java.util.List<Ingredient> getIngredientList() { return this.ingredientList; }

            // copyForGridCell() intentionally not exposed on builder; base SlotWidget handles cloning for grids
            SlotWidget copyForGridCell(int cellX, int cellY, int index) {
                boolean isOutput = this.role == SlotRole.OUTPUT;
                boolean drawBg = !this.noBackground;
                ResourceLocation bg = this.customBackground;
                boolean useCustom = bg != null;
                if (!useCustom && drawBg) {
                    bg = isOutput
                            ? ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot_output.png")
                            : ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png");
                    useCustom = true;
                }
                return new SlotWidget(
                        this.role,
                        cellX,
                        cellY,
                        drawBg,
                        isOutput,
                        this.stack,
                        index,
                        bg,
                        useCustom,
                        this.noBackground,
                        this.stackSupplier,
                        this.stackList,
                        this.cyclingIngredient,
                        this.cyclingSalt + Math.max(0, index),
                        this.cyclingSpeedTicks,
                        false,
                        this.ingredientList
                );
            }

            SlotWidget copyForGridCell(int cellX, int cellY, int index, boolean listCycleOverride) {
                boolean isOutput = this.role == SlotRole.OUTPUT;
                boolean drawBg = !this.noBackground;
                ResourceLocation bg = this.customBackground;
                boolean useCustom = bg != null;
                if (!useCustom && drawBg) {
                    bg = isOutput
                            ? ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot_output.png")
                            : ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png");
                    useCustom = true;
                }
                return new SlotWidget(
                        this.role,
                        cellX,
                        cellY,
                        drawBg,
                        isOutput,
                        this.stack,
                        index,
                        bg,
                        useCustom,
                        this.noBackground,
                        this.stackSupplier,
                        this.stackList,
                        this.cyclingIngredient,
                        this.cyclingSalt + Math.max(0, index),
                        this.cyclingSpeedTicks,
                        listCycleOverride,
                        this.ingredientList
                );
            }
        }
    }
    
    public static class AddArrow {
        public static class Right {
            public static AddTexture.TextureBuilder progress(int x, int y) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT); }
            public static AddTexture.TextureBuilder progress(int x, int y, boolean startFilled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, startFilled, 60, 10); }
            public static AddTexture.TextureBuilder animate(int x, int y, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, false, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, startFilled, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder state(int x, int y, boolean filled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, filled); }
        }
        
        public static class Left {
            public static AddTexture.TextureBuilder progress(int x, int y) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT); }
            public static AddTexture.TextureBuilder progress(int x, int y, boolean startFilled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, startFilled, 60, 10); }
            public static AddTexture.TextureBuilder animate(int x, int y, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, false, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, startFilled, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder state(int x, int y, boolean filled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, filled); }
        }
        
        public static class Up {
            public static AddTexture.TextureBuilder progress(int x, int y) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP); }
            public static AddTexture.TextureBuilder progress(int x, int y, boolean startFilled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, startFilled, 60, 10); }
            public static AddTexture.TextureBuilder animate(int x, int y, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, false, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, startFilled, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder state(int x, int y, boolean filled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, filled); }
        }
        
        public static class Down {
            public static AddTexture.TextureBuilder progress(int x, int y) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN); }
            public static AddTexture.TextureBuilder progress(int x, int y, boolean startFilled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, startFilled, 60, 10); }
            public static AddTexture.TextureBuilder animate(int x, int y, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, false, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, startFilled, rampTicks, pauseTicks); }
            public static AddTexture.TextureBuilder state(int x, int y, boolean filled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, filled); }
        }
    }
    
    public static class AddFlame {
        public static AddTexture.TextureBuilder progress(int x, int y) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP); }
        public static AddTexture.TextureBuilder progress(int x, int y, boolean startFilled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, startFilled, 60, 10); }
        public static AddTexture.TextureBuilder animate(int x, int y, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, true, rampTicks, pauseTicks); }
        public static AddTexture.TextureBuilder animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, startFilled, rampTicks, pauseTicks); }
        public static AddTexture.TextureBuilder state(int x, int y, boolean filled) { return new AddTexture.TextureBuilder(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, filled); }
    }
    
    public static class AddTexture {
        public static TextureBuilder staticTexture(int x, int y, ResourceLocation texture, int u, int v, int width, int height, int texWidth, int texHeight) {
            return new TextureBuilder(x, y, texture, u, v, width, height, texWidth, texHeight);
        }
        // Custom animated texture pair - dynamic
        public static TextureBuilder animate(int x, int y,
                                            ResourceLocation background, ResourceLocation fill,
                                            int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                            TextureWidget.Direction direction,
                                            boolean startFilled, int rampTicks, int pauseTicks) {
            return new TextureBuilder(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, startFilled, rampTicks, pauseTicks);
        }
        // Custom progress (default dynamic behavior, default timings)
        public static TextureBuilder progress(int x, int y,
                                             ResourceLocation background, ResourceLocation fill,
                                             int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                             TextureWidget.Direction direction) {
            return new TextureBuilder(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, false, 60, 10);
        }
        // Custom static state
        public static TextureBuilder state(int x, int y,
                                          ResourceLocation background, ResourceLocation fill,
                                          int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                          TextureWidget.Direction direction,
                                          boolean filled) {
            return new TextureBuilder(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, filled);
        }

        // Builder for textures with positioning helpers
        public static class TextureBuilder extends TextureWidget {
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            // Cache logical drawn width/height for anchoring
            private final int drawW;
            private final int drawH;
            // Store type-based params (for arrows/flames) to avoid accessing parent privates
            private final TextureWidget.Type typeKind;
            private final boolean typeDynamic;
            // Stored params for reconstruction
            private final boolean pairMode; // background/fill pair
            private final ResourceLocation storeBackground;
            private final ResourceLocation storeFill;
            private final int storeBgW, storeBgH, storeFillW, storeFillH;
            private final TextureWidget.Direction storeDirection;
            private final boolean storeStartFilled;
            private final int storeRampTicks, storePauseTicks;
            private final boolean staticMode; // single texture sprite
            private final ResourceLocation storeTexture;
            private final int storeU, storeV, storeWidth, storeHeight, storeTexW, storeTexH;
            // Constructor variants mirror TextureWidget, with draw size captured
            public TextureBuilder(int x, int y, ResourceLocation texture, int u, int v, int width, int height, int texWidth, int texHeight) {
                super(x, y, texture, u, v, width, height, texWidth, texHeight);
                this.drawW = width; this.drawH = height;
                this.typeKind = null; this.typeDynamic = false;
                this.pairMode = false;
                this.storeBackground = null; this.storeFill = null;
                this.storeBgW = 0; this.storeBgH = 0; this.storeFillW = 0; this.storeFillH = 0;
                this.storeDirection = null; this.storeStartFilled = false; this.storeRampTicks = 0; this.storePauseTicks = 0;
                this.staticMode = true;
                this.storeTexture = texture; this.storeU = u; this.storeV = v; this.storeWidth = width; this.storeHeight = height; this.storeTexW = texWidth; this.storeTexH = texHeight;
            }
            // Additional constructors to wrap animated/static arrow/flame builders
            public TextureBuilder(int x, int y, TextureWidget.Type type, TextureWidget.Direction direction) {
                super(x, y, type, direction);
                this.drawW = getWidthForType();
                this.drawH = getHeightForType();
                this.typeKind = type; this.typeDynamic = true;
                this.pairMode = false;
                this.storeBackground = null; this.storeFill = null;
                this.storeBgW = 0; this.storeBgH = 0; this.storeFillW = 0; this.storeFillH = 0;
                this.storeDirection = direction; this.storeStartFilled = false; this.storeRampTicks = 60; this.storePauseTicks = 10;
                this.staticMode = false;
                this.storeTexture = null; this.storeU = 0; this.storeV = 0; this.storeWidth = 0; this.storeHeight = 0; this.storeTexW = 0; this.storeTexH = 0;
            }
            public TextureBuilder(int x, int y, TextureWidget.Type type, TextureWidget.Direction direction, boolean startFilled, int rampTicks, int pauseTicks) {
                super(x, y, type, direction, startFilled, rampTicks, pauseTicks);
                this.drawW = getWidthForType();
                this.drawH = getHeightForType();
                this.typeKind = type; this.typeDynamic = true;
                this.pairMode = false;
                this.storeBackground = null; this.storeFill = null;
                this.storeBgW = 0; this.storeBgH = 0; this.storeFillW = 0; this.storeFillH = 0;
                this.storeDirection = direction; this.storeStartFilled = startFilled; this.storeRampTicks = rampTicks; this.storePauseTicks = pauseTicks;
                this.staticMode = false;
                this.storeTexture = null; this.storeU = 0; this.storeV = 0; this.storeWidth = 0; this.storeHeight = 0; this.storeTexW = 0; this.storeTexH = 0;
            }
            public TextureBuilder(int x, int y, TextureWidget.Type type, TextureWidget.Direction direction, boolean filled) {
                super(x, y, type, direction, filled);
                this.drawW = getWidthForType();
                this.drawH = getHeightForType();
                this.typeKind = type; this.typeDynamic = false;
                this.pairMode = false;
                this.storeBackground = null; this.storeFill = null;
                this.storeBgW = 0; this.storeBgH = 0; this.storeFillW = 0; this.storeFillH = 0;
                this.storeDirection = direction; this.storeStartFilled = filled; this.storeRampTicks = 60; this.storePauseTicks = 10;
                this.staticMode = false;
                this.storeTexture = null; this.storeU = 0; this.storeV = 0; this.storeWidth = 0; this.storeHeight = 0; this.storeTexW = 0; this.storeTexH = 0;
            }
            public TextureBuilder(int x, int y, ResourceLocation background, ResourceLocation fill, int bgWidth, int bgHeight, int fillWidth, int fillHeight, TextureWidget.Direction direction, boolean startFilled, int rampTicks, int pauseTicks) {
                super(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, startFilled, rampTicks, pauseTicks);
                this.drawW = bgWidth; this.drawH = bgHeight;
                this.typeKind = null; this.typeDynamic = false;
                this.pairMode = true;
                this.storeBackground = background; this.storeFill = fill;
                this.storeBgW = bgWidth; this.storeBgH = bgHeight; this.storeFillW = fillWidth; this.storeFillH = fillHeight;
                this.storeDirection = direction; this.storeStartFilled = startFilled; this.storeRampTicks = rampTicks; this.storePauseTicks = pauseTicks;
                this.staticMode = false;
                this.storeTexture = null; this.storeU = 0; this.storeV = 0; this.storeWidth = 0; this.storeHeight = 0; this.storeTexW = 0; this.storeTexH = 0;
            }
            public TextureBuilder(int x, int y, ResourceLocation background, ResourceLocation fill, int bgWidth, int bgHeight, int fillWidth, int fillHeight, TextureWidget.Direction direction, boolean filled) {
                super(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, filled);
                this.drawW = bgWidth; this.drawH = bgHeight;
                this.typeKind = null; this.typeDynamic = false;
                this.pairMode = true;
                this.storeBackground = background; this.storeFill = fill;
                this.storeBgW = bgWidth; this.storeBgH = bgHeight; this.storeFillW = fillWidth; this.storeFillH = fillHeight;
                this.storeDirection = direction; this.storeStartFilled = filled; this.storeRampTicks = 60; this.storePauseTicks = 10;
                this.staticMode = false;
                this.storeTexture = null; this.storeU = 0; this.storeV = 0; this.storeWidth = 0; this.storeHeight = 0; this.storeTexW = 0; this.storeTexH = 0;
            }
            public boolean hasPositioning() { return positionMode != Widgets.PositionMode.NONE; }
            public TextureBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public TextureBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public TextureBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public TextureBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public TextureBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public TextureBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public TextureBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public TextureBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public TextureBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }
            public TextureWidget finalizePosition(int contentWidth, int contentHeight) {
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, this.drawW, this.drawH, this.x, this.y);
                int nx = pos[0];
                int ny = pos[1];
                // Reconstruct a concrete TextureWidget with resolved coordinates based on stored params
                if (this.pairMode) {
                    return new TextureWidget(nx, ny, this.storeBackground, this.storeFill, this.storeBgW, this.storeBgH, this.storeFillW, this.storeFillH, this.storeDirection, this.storeStartFilled, this.storeRampTicks, this.storePauseTicks);
                } else if (this.staticMode) {
                    return new TextureWidget(nx, ny, this.storeTexture, this.storeU, this.storeV, this.storeWidth, this.storeHeight, this.storeTexW, this.storeTexH);
                } else {
                    // Type-based (arrows/flames)
                    if (this.typeKind != null) {
                        if (this.typeDynamic) {
                            return new TextureWidget(nx, ny, this.typeKind, this.storeDirection, this.storeStartFilled, this.storeRampTicks, this.storePauseTicks);
                        }
                        return new TextureWidget(nx, ny, this.typeKind, this.storeDirection, this.storeStartFilled);
                    }
                    // Fallback
                    return new TextureWidget(nx, ny, this.storeTexture, this.storeU, this.storeV, this.storeWidth, this.storeHeight, this.storeTexW, this.storeTexH);
                }
            }
        }
    }

    /** Composite widgets */
    public static class AddGrid {
        // Deprecated boolean-based overload removed; use SlotWidget-based overload instead

        // New overload: pass a SlotWidget template to derive style and items (if template is a GridSlotWidget)
        public static GridBuilder items(int x, int y, int columns, int rows, SlotWidget slotWidget) {
            return new GridBuilder(x, y, columns, rows, slotWidget);
        }

        // Builder adds positioning for GridSlotWidget
        public static class GridBuilder extends GridSlotWidget {
            private final int columns;
            private final int rows;
            private final SlotWidget template;
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            private boolean hideEmpty = false;
            private boolean disableListCycling = true; // disabled by default
            public GridBuilder(int x, int y, int columns, int rows, SlotWidget slotWidget) {
                super(x, y, columns, rows, slotWidget);
                this.columns = columns;
                this.rows = rows;
                this.template = slotWidget;
            }
            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }
            public GridBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public GridBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public GridBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public GridBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public GridBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public GridBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public GridBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public GridBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public GridBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }
            // Default hides empty cells; call to explicitly show empties
            public GridBuilder hideEmptySlots() { this.hideEmpty = true; return this; }
            // New: disable list-based cycling replication for non-ingredient lists (default already disabled)
            public GridBuilder disableListCycling() { this.disableListCycling = true; return this; }
            public GridSlotWidget finalizePosition(int contentWidth, int contentHeight) {
                int effectiveRows = rows;
                int effectiveCols = columns;
                int count = 0;
                if (hideEmpty) {
                    try { count = Math.max(0, template.getStackListSize()); } catch (Throwable ignored) {}
                    if (count > 0) {
                        effectiveRows = Math.max(1, (count + columns - 1) / columns);
                        if (count < columns) effectiveCols = count; // single-row case shrinks width
                    }
                }
                boolean outputCells = (template != null && template.role == SlotRole.OUTPUT);
                int step = outputCells ? 26 : 18;
                int gridW = Math.max(0, effectiveCols * step);
                int gridH = Math.max(0, effectiveRows * step);
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, gridW, gridH, this.x, this.y);
                java.util.List<net.minecraft.world.item.ItemStack> items = (template instanceof GridSlotWidget gsw) ? gsw.items : java.util.Collections.emptyList();
                GridSlotWidget g = new GridSlotWidget(pos[0], pos[1], effectiveCols, effectiveRows, template, items, this.hideEmpty);
                // Propagate cycling behavior preference
                if (template != null && this.disableListCycling) {
                    // Force cloned cells not to cycle the same list across all slots
                    try { g.listCycleOverride = false; } catch (Throwable ignored) {}
                }
                return g;
            }

            // Expose for scroll builder sizing
            public int getColumns() { return columns; }
            public int getRows() { return rows; }
            public boolean isHideEmpty() { return hideEmpty; }
            public int getTemplateCount() { try { return Math.max(0, template.getStackListSize()); } catch (Throwable t) { return 0; } }
            public int getEffectiveRows() {
                if (!hideEmpty) return rows;
                int count = getTemplateCount();
                return Math.max(1, (count + columns - 1) / columns);
            }
            public int getEffectiveColumns() {
                if (!hideEmpty) return columns;
                int count = getTemplateCount();
                if (count == 0) return columns;
                if (count < columns) return count;
                return columns;
            }
        }
    }

    public static class AddScroll {
        public static ScrollBuilder grid(int x, int y, int visibleRows, GridSlotWidget grid) {
            return new ScrollBuilder(x, y, visibleRows, grid);
        }

        public static class ScrollBuilder extends ScrollWidget {
            private final int visibleRows;
            private final GridSlotWidget grid;
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            private boolean disableListCycling = true; // disabled by default
            public ScrollBuilder(int x, int y, int visibleRows, GridSlotWidget grid) {
                super(x, y, visibleRows, grid);
                this.visibleRows = visibleRows;
                this.grid = grid;
            }
            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }
            public ScrollBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public ScrollBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public ScrollBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public ScrollBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public ScrollBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public ScrollBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public ScrollBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public ScrollBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public ScrollBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }
            public ScrollBuilder disableListCycling() { this.disableListCycling = true; return this; }
            public ScrollWidget finalizePosition(int contentWidth, int contentHeight) {
                int cols = 3;
                int effRows = visibleRows;
                boolean outputCells = (grid != null && grid.asOutput);
                int step = outputCells ? 26 : 18;
                if (grid instanceof Widgets.AddGrid.GridBuilder gb) {
                    cols = gb.getEffectiveColumns();
                    effRows = Math.min(visibleRows, gb.getEffectiveRows());
                    outputCells = (gb.template != null && gb.template.role == SlotRole.OUTPUT);
                    step = outputCells ? 26 : 18;
                }
                int visibleCols = Math.max(1, cols - 1); // reserve last col for scrollbar
                int widthPx = Math.max(0, visibleCols * step + 18);
                int heightPx = Math.max(0, effRows * step);
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, widthPx, heightPx, this.x, this.y);
                ScrollWidget s = new ScrollWidget(pos[0], pos[1], visibleRows, grid);
                try { if (disableListCycling && grid != null) grid.listCycleOverride = false; } catch (Throwable ignored) {}
                return s;
            }
        }
    }
    
    public static class AddText {
        public static TextBuilder component(int x, int y, Component text) {
            return new TextBuilder(x, y, text, null);
        }
        
        public static TextBuilder component(int x, int y, Component text, TextColor color) {
            return new TextBuilder(x, y, text, color);
        }
        
        // Builder class for fluent API that extends TextWidget
        public static class TextBuilder extends TextWidget {
            private boolean centered = false;
            private int width = 0;
            private int height = 0;
            private boolean autoScale = false;
            private Component text;
            private TextColor color;
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            
            public TextBuilder(int x, int y, Component text, TextColor color) {
                super(x, y, text, color, false);
                this.text = text;
                this.color = color;
            }
            
            public TextBuilder centered() {
                this.centered = true;
                return this;
            }
            
            public TextBuilder bounds(int width, int height) {
                this.width = width;
                this.height = height;
                this.autoScale = true;
                return this;
            }
            
            public TextBuilder color(ChatFormatting formatting) {
                this.color = TextColor.fromRgb(formatting.getColor());
                return this;
            }
            
            public TextBuilder color(String colorName) {
                ChatFormatting formatting = getColorFromName(colorName);
                if (formatting != null) {
                    this.color = TextColor.fromRgb(formatting.getColor());
                }
                return this;
            }

            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }
            public TextBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public TextBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public TextBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public TextBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public TextBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public TextBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public TextBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public TextBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public TextBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }

            public TextWidget finalizePosition(int contentWidth, int contentHeight) {
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, this.width, this.height, this.x, this.y);
                int nx = pos[0];
                int ny = pos[1];
                if (autoScale) {
                    return new TextWidget(nx, ny, this.width, this.height, this.text, this.color, centered);
                } else {
                    return new TextWidget(nx, ny, this.text, this.color, centered);
                }
            }
            
            private ChatFormatting getColorFromName(String colorName) {
                if (colorName == null) return null;
                
                String name = colorName.toLowerCase().trim();
                switch (name) {
                    case "black": return ChatFormatting.BLACK;
                    case "dark_blue": case "darkblue": return ChatFormatting.DARK_BLUE;
                    case "dark_green": case "darkgreen": return ChatFormatting.DARK_GREEN;
                    case "dark_aqua": case "darkaqua": return ChatFormatting.DARK_AQUA;
                    case "dark_red": case "darkred": return ChatFormatting.DARK_RED;
                    case "dark_purple": case "darkpurple": return ChatFormatting.DARK_PURPLE;
                    case "gold": return ChatFormatting.GOLD;
                    case "gray": case "grey": return ChatFormatting.GRAY;
                    case "dark_gray": case "darkgray": case "dark_grey": case "darkgrey": return ChatFormatting.DARK_GRAY;
                    case "blue": return ChatFormatting.BLUE;
                    case "green": case "lime": return ChatFormatting.GREEN;
                    case "aqua": case "cyan": return ChatFormatting.AQUA;
                    case "red": return ChatFormatting.RED;
                    case "light_purple": case "lightpurple": case "magenta": return ChatFormatting.LIGHT_PURPLE;
                    case "yellow": return ChatFormatting.YELLOW;
                    case "white": return ChatFormatting.WHITE;
                    default: return null;
                }
            }
            
            // Override render to use the builder's settings
            @Override
            public void render(GuiGraphics g, Font font, int baseX, int baseY) {
                TextWidget widget;
                if (autoScale) {
                    widget = new TextWidget(x, y, width, height, this.text, this.color, centered);
                } else {
                    widget = new TextWidget(x, y, this.text, this.color, centered);
                }
                widget.render(g, font, baseX, baseY);
            }
        }
    }

    public static class AddIcon {
        public static class IconBuilder extends IconWidget {
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            private boolean centered = false;
            private boolean autoBounds = false;
            private int boundW = 16;
            private int boundH = 16;
            private final ItemStack storeStack;
            private final int storeIndex;
            private final java.util.function.Supplier<ItemStack> storeSupplier;
            private final java.util.List<ItemStack> storeList;
            private final Ingredient storeIngredient;
            private final int storeSalt;

            public IconBuilder(int x, int y, ItemStack stack, int index) {
                super(x, y, stack, index);
                this.storeStack = stack;
                this.storeIndex = index;
                this.storeSupplier = null;
                this.storeList = null;
                this.storeIngredient = null;
                this.storeSalt = 0;
            }
            public IconBuilder(int x, int y, java.util.function.Supplier<ItemStack> supplier, int index) {
                super(x, y, ItemStack.EMPTY, index);
                this.storeStack = ItemStack.EMPTY;
                this.storeIndex = index;
                this.storeSupplier = supplier;
                this.storeList = null;
                this.storeIngredient = null;
                this.storeSalt = 0;
            }
            public IconBuilder(int x, int y, java.util.List<ItemStack> items, int index) {
                super(x, y, ItemStack.EMPTY, index);
                this.storeStack = ItemStack.EMPTY;
                this.storeIndex = index;
                this.storeSupplier = null;
                this.storeList = items;
                this.storeIngredient = null;
                this.storeSalt = 0;
            }
            public IconBuilder(int x, int y, Ingredient ingredient, int salt, int index) {
                super(x, y, ItemStack.EMPTY, index);
                this.storeStack = ItemStack.EMPTY;
                this.storeIndex = index;
                this.storeSupplier = null;
                this.storeList = null;
                this.storeIngredient = ingredient;
                this.storeSalt = salt;
            }

            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }
            public IconBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public IconBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public IconBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public IconBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public IconBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public IconBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public IconBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public IconBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public IconBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }

            public IconBuilder bounds(int width, int height) { this.autoBounds = true; this.boundW = Math.max(1, width); this.boundH = Math.max(1, height); return this; }
            public IconBuilder centered() { this.centered = true; return this; }

            public IconWidget finalizePosition(int contentWidth, int contentHeight) {
                int w = this.autoBounds ? this.boundW : 16;
                int h = this.autoBounds ? this.boundH : 16;
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, w, h, this.x, this.y);
                int nx = pos[0];
                int ny = pos[1];
                if (this.autoBounds) {
                    return new IconWidget(nx, ny, this.boundW, this.boundH, this.centered, this.storeStack, this.storeIndex, this.storeSupplier, this.storeList, this.storeIngredient, this.storeSalt);
                } else {
                    // ignore centered without bounds
                    if (this.storeSupplier != null) return new IconWidget(nx, ny, this.storeStack, this.storeIndex);
                    if (this.storeList != null) return new IconWidget(nx, ny, this.storeStack, this.storeIndex);
                    if (this.storeIngredient != null) return new IconWidget(nx, ny, this.storeStack, this.storeIndex);
                    return new IconWidget(nx, ny, this.storeStack, this.storeIndex);
                }
            }

            @Override
            public void render(GuiGraphics g, Font font, int baseX, int baseY) {
                IconWidget widget;
                if (this.autoBounds) {
                    widget = new IconWidget(x, y, this.boundW, this.boundH, this.centered, this.storeStack, this.storeIndex, this.storeSupplier, this.storeList, this.storeIngredient, this.storeSalt);
                } else {
                    widget = new IconWidget(x, y, this.storeStack, this.storeIndex);
                }
                widget.render(g, font, baseX, baseY);
            }
        }

        private static java.util.List<ItemStack> holdersToStacks(java.util.List<Holder<Item>> holders) {
            java.util.ArrayList<ItemStack> list = new java.util.ArrayList<>();
            if (holders != null) for (Holder<Item> h : holders) if (h != null && h.isBound()) list.add(new ItemStack(h.value()));
            return list;
        }
        private static java.util.List<ItemStack> holdersToStacks(java.util.stream.Stream<Holder<Item>> holders) {
            if (holders == null) return java.util.Collections.emptyList();
            return holders.filter(java.util.Objects::nonNull).filter(Holder::isBound).map(h -> new ItemStack(h.value())).toList();
        }

        public static IconBuilder of(int x, int y, ItemStack stack, int index) { return new IconBuilder(x, y, stack, index); }
        public static IconBuilder of(int x, int y, Item item, int index) { return new IconBuilder(x, y, new ItemStack(item), index); }
        public static IconBuilder of(int x, int y, Holder<Item> holder, int index) { return new IconBuilder(x, y, (holder == null ? ItemStack.EMPTY : new ItemStack(holder.value())), index); }
        public static IconBuilder of(int x, int y, java.util.function.Supplier<ItemStack> supplier, int index) { return new IconBuilder(x, y, supplier, index); }
        public static IconBuilder of(int x, int y, java.util.List<ItemStack> items, int index) { return new IconBuilder(x, y, items, index); }
        public static IconBuilder ofHolders(int x, int y, java.util.List<Holder<Item>> holders, int index) { return new IconBuilder(x, y, holdersToStacks(holders), index); }
        public static IconBuilder ofHolders(int x, int y, java.util.stream.Stream<Holder<Item>> holders, int index) { return new IconBuilder(x, y, holdersToStacks(holders), index); }
        public static IconBuilder of(int x, int y, Ingredient ingredient, int index) { return new IconBuilder(x, y, ingredient, 0, index); }
        public static IconBuilder of(int x, int y, Ingredient ingredient, int salt, int index) { return new IconBuilder(x, y, ingredient, salt, index); }
    }

    /** Hoverable tooltip widgets */
    public static class AddHover {
        public static HoverBuilder translatable(int x, int y, int width, int height, String key) { return new HoverBuilder(x, y, width, height, Component.translatable(key)); }
        public static HoverBuilder literal(int x, int y, int width, int height, String text) { return new HoverBuilder(x, y, width, height, Component.literal(text)); }

        public static class HoverBuilder extends HoverTooltipWidget {
            private Widgets.PositionMode positionMode = Widgets.PositionMode.NONE;
            private final Component hoverText;
            public HoverBuilder(int x, int y, int width, int height, Component text) { super(x, y, width, height, text); this.hoverText = text; }
            public boolean hasPositioning() { return this.positionMode != Widgets.PositionMode.NONE; }
            public HoverBuilder positionTopLeft() { this.positionMode = Widgets.PositionMode.TOP_LEFT; return this; }
            public HoverBuilder positionTopCenter() { this.positionMode = Widgets.PositionMode.TOP_CENTER; return this; }
            public HoverBuilder positionTopRight() { this.positionMode = Widgets.PositionMode.TOP_RIGHT; return this; }
            public HoverBuilder positionMiddleLeft() { this.positionMode = Widgets.PositionMode.MIDDLE_LEFT; return this; }
            public HoverBuilder positionCenter() { this.positionMode = Widgets.PositionMode.CENTER; return this; }
            public HoverBuilder positionMiddleRight() { this.positionMode = Widgets.PositionMode.MIDDLE_RIGHT; return this; }
            public HoverBuilder positionBottomLeft() { this.positionMode = Widgets.PositionMode.BOTTOM_LEFT; return this; }
            public HoverBuilder positionBottomCenter() { this.positionMode = Widgets.PositionMode.BOTTOM_CENTER; return this; }
            public HoverBuilder positionBottomRight() { this.positionMode = Widgets.PositionMode.BOTTOM_RIGHT; return this; }
            public HoverTooltipWidget finalizePosition(int contentWidth, int contentHeight) {
                int[] pos = Positioning.resolve(this.positionMode, contentWidth, contentHeight, 0, 0, this.width, this.height, this.x, this.y);
                int nx = pos[0];
                int ny = pos[1];
                return new HoverTooltipWidget(nx, ny, this.width, this.height, this.hoverText);
            }
        }
    }
}
