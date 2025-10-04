package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Holder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Icon widget for rendering an item/block with optional bounds-based scaling.
 */
public class IconWidget {
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final boolean autoScale;
    public final boolean centered;

    public final ItemStack stack;
    public final int recipeJsonIndex;
    private final Supplier<ItemStack> stackSupplier;
    private final List<ItemStack> stackList;
    private final Ingredient cyclingIngredient;
    private final int cyclingSalt;

    public IconWidget(int x, int y, ItemStack stack, int index) {
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;
        this.autoScale = false;
        this.centered = false;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.recipeJsonIndex = Math.max(0, index);
        this.stackSupplier = null;
        this.stackList = null;
        this.cyclingIngredient = null;
        this.cyclingSalt = 0;
    }

    public IconWidget(int x, int y,
                      ItemStack stack, int index,
                      Supplier<ItemStack> stackSupplier,
                      List<ItemStack> stackList,
                      Ingredient cyclingIngredient,
                      int cyclingSalt) {
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;
        this.autoScale = false;
        this.centered = false;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.recipeJsonIndex = Math.max(0, index);
        this.stackSupplier = stackSupplier;
        this.stackList = stackList;
        this.cyclingIngredient = cyclingIngredient;
        this.cyclingSalt = cyclingSalt;
    }

    public IconWidget(int x, int y, int width, int height, boolean centered,
                      ItemStack stack, int index,
                      Supplier<ItemStack> stackSupplier,
                      List<ItemStack> stackList,
                      Ingredient cyclingIngredient,
                      int cyclingSalt) {
        this.x = x;
        this.y = y;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.autoScale = true;
        this.centered = centered;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.recipeJsonIndex = Math.max(0, index);
        this.stackSupplier = stackSupplier;
        this.stackList = stackList;
        this.cyclingIngredient = cyclingIngredient;
        this.cyclingSalt = cyclingSalt;
    }

    public static IconWidget of(int x, int y, ItemStack stack, int index) {
        return new IconWidget(x, y, stack, index);
    }

    public static IconWidget of(int x, int y, int width, int height, ItemStack stack, int index) {
        return new IconWidget(x, y, width, height, false, stack, index, null, null, null, 0);
    }

    public static IconWidget of(int x, int y, int width, int height, Item item, int index) {
        return new IconWidget(x, y, width, height, false, new ItemStack(item), index, null, null, null, 0);
    }

    public static IconWidget of(int x, int y, int width, int height, Holder<Item> holder, int index) {
        return new IconWidget(x, y, width, height, false, new ItemStack(holder.value()), index, null, null, null, 0);
    }

    public static IconWidget of(int x, int y, int width, int height, List<Holder<Item>> holders, int index) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Item> h : holders) stacks.add(new ItemStack(h.value()));
        return new IconWidget(x, y, width, height, false, ItemStack.EMPTY, index, null, stacks, null, 0);
    }

    public static IconWidget of(int x, int y, int width, int height, java.util.stream.Stream<Holder<Item>> holders, int index) {
        List<ItemStack> stacks = holders == null ? java.util.Collections.emptyList() : holders.map(h -> new ItemStack(h.value())).toList();
        return new IconWidget(x, y, width, height, false, ItemStack.EMPTY, index, null, stacks, null, 0);
    }

    public static IconWidget of(int x, int y, int width, int height, Ingredient ingredient, int index, int salt) {
        return new IconWidget(x, y, width, height, false, ItemStack.EMPTY, index, null, null, ingredient, salt);
    }

    public void render(GuiGraphics g, Font font, int baseX, int baseY) {
        int drawX = baseX + this.x;
        int drawY = baseY + this.y;

        ItemStack toRender;
        if (this.stackSupplier != null) {
            try {
                ItemStack supplied = this.stackSupplier.get();
                toRender = supplied == null ? ItemStack.EMPTY : supplied;
            } catch (Throwable t) {
                toRender = ItemStack.EMPTY;
            }
        } else if (this.stackList != null) {
            int idx = Math.max(0, this.recipeJsonIndex);
            toRender = (idx < this.stackList.size() && this.stackList.get(idx) != null) ? this.stackList.get(idx) : ItemStack.EMPTY;
        } else if (this.cyclingIngredient != null) {
            toRender = getCyclingItem(this.cyclingIngredient, this.cyclingSalt);
        } else {
            toRender = this.stack;
        }

        if (this.autoScale) {
            float scaleX = this.width / 16.0f;
            float scaleY = this.height / 16.0f;
            float scale = Math.min(scaleX, scaleY);
            int offsetX = 0;
            int offsetY = 0;
            if (this.centered) {
                int contentW = Math.round(16 * scale);
                int contentH = Math.round(16 * scale);
                offsetX = (this.width - contentW) / 2;
                offsetY = (this.height - contentH) / 2;
            }
            g.pose().pushPose();
            g.pose().translate(drawX + offsetX, drawY + offsetY, 0);
            g.pose().scale(scale, scale, 1.0f);
            g.renderItem(toRender, 0, 0);
            g.renderItemDecorations(font, toRender, 0, 0);
            g.pose().popPose();
        } else {
            g.renderItem(toRender, drawX, drawY);
            g.renderItemDecorations(font, toRender, drawX, drawY);
        }

        // Note: Tooltips for icons are handled by parent containers; no direct hover logic here.
    }

    private static ItemStack getCyclingItem(Ingredient ingredient, int salt) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            long t = (mc != null && mc.level != null) ? mc.level.getGameTime() : 0L;
            var holders = ingredient.items().toList();
            if (holders.isEmpty()) return ItemStack.EMPTY;
            int idx = (int)(((t / 20L) + Math.max(0, salt)) % holders.size());
            return new ItemStack(holders.get(idx).value());
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }
}


