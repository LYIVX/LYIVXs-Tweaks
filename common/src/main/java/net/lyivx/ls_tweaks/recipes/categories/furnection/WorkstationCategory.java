package net.lyivx.ls_tweaks.recipes.categories.furnection;

import net.lyivx.ls_furniture.common.recipes.WorkstationRecipe;
import net.lyivx.ls_furniture.registry.ModItems;
import net.lyivx.ls_tweaks.api.recipes.*;
import net.lyivx.ls_tweaks.recipes.util.DisplayUtil;
import net.lyivx.ls_tweaks.recipes.wrappers.WorkstationRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.lang.reflect.Constructor;

/** Simple cooking category base */
public abstract class WorkstationCategory implements RecipeBackedCategory<WorkstationRecipe> {
    private final ResourceLocation id;
    private final ResourceLocation icon;
    private final RecipeType<WorkstationRecipe> type;

    protected WorkstationCategory(ResourceLocation id, ResourceLocation icon, RecipeType<WorkstationRecipe> type) {
        this.id = id; 
        this.icon = icon;
        this.type = type;
    }

    @Override public ResourceLocation id() { return id; }
    @Override public String titleTranslationKey() { return "ls_furniture.category." + id.getPath(); }
    @Override public ResourceLocation iconTexture() { return icon; }
    @Override public ItemStack iconItem() { return ModItems.WORKSTATION.get().getDefaultInstance(); }
    
    protected abstract ItemStack getIconItem();
    
    @Override public Class<WorkstationRecipe> recipeClass() { return WorkstationRecipe.class; }
    @Override public RecipeType<WorkstationRecipe> recipeType() { return type; }
    
    @Override public int sortOrder() {
        return 6;
    }

    @Override
    public RecipeDisplay buildDisplay(WorkstationRecipe recipe) {
        // Build grouped outputs for same input and inputCount using a scrollable grid
        ItemStack in = WorkstationRecipeHelper.getFirstInputItem(recipe);
        if (!in.isEmpty()) in.setCount(Math.max(1, recipe.getInputCount()));
        java.util.List<ItemStack> outputs = enumerateOutputsFor(recipe);
        // No dedupe here; holder-aware path handles canonical selection

        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        // Place input at Top-Center using builder positioning helper (offsets derived from x/y)
        builder.addSlot(Widgets.AddSlot.input(0, 0, in, 0).positionTopCenter());

        // Use grouped outputs computed above
        int columns = 5;
        int rows = Math.max(1, (outputs.size() + columns - 1) / columns);
        int visibleRows = Math.min(rows, 4);
        var grid = Widgets.AddGrid.items(0, 24, columns, rows, Widgets.AddSlot.output(0, 0, outputs, 0)).hideEmptySlots();
        var scroll = Widgets.AddScroll.grid(0, 24, visibleRows, grid);
        builder.addSlot(scroll);
        int effColsA = (grid instanceof Widgets.AddGrid.GridBuilder gbA) ? gbA.getEffectiveColumns() : columns;
        int effRowsA = (grid instanceof Widgets.AddGrid.GridBuilder gbA2) ? gbA2.getEffectiveRows() : rows;
        int viewRowsA = Math.min(visibleRows, effRowsA);
        builder.contentSize(effColsA * 26, 24 + viewRowsA * 26);
        return builder.build();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeDisplay buildDisplay(RecipeHolder<? extends Recipe<?>> holder) {
        return buildDisplayWithHolder((RecipeHolder) holder);
    }

    public RecipeDisplay buildDisplayWithHolder(RecipeHolder<Recipe<?>> holder) {
        Recipe<?> recipe = holder.value();
        ItemStack in = ItemStack.EMPTY;

        if (recipe instanceof WorkstationRecipe workstationRecipe1) {
            // Use helper to get input items reliably
            in = WorkstationRecipeHelper.getFirstInputItem(workstationRecipe1);
            if (!in.isEmpty()) in.setCount(Math.max(1, workstationRecipe1.getInputCount()));

        }

        ItemStack out = (recipe instanceof WorkstationRecipe workstationRecipe) ? assembleCookingOutput(workstationRecipe) : DisplayUtil.firstOutput(holder);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();


        // Header
        builder.addSlot(Widgets.AddSlot.input(0, 0, in, 0).positionTopCenter());
        // Group outputs for same input/inputCount and deduplicate cards (per-frame)
        if (recipe instanceof WorkstationRecipe wr) {
            java.util.List<ItemStack> outputs = enumerateOutputsFor(wr);
            // Per-tick dedupe by input family + count so usages collapse to one card, but results still show
            try {
                var mc = net.minecraft.client.Minecraft.getInstance();
                long tick = (mc != null && mc.level != null) ? mc.level.getGameTime() : 0L;
                if (DEDUPE_TICK != tick) { DEDUPE_SEEN.clear(); DEDUPE_TICK = tick; }
                ItemStack seedFirst = wr.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
                String inKey = String.valueOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(seedFirst.getItem()));
                String groupKey = inKey + "#" + wr.getInputCount();
                if (DEDUPE_SEEN.contains(groupKey)) { return null; }
                DEDUPE_SEEN.add(groupKey);
            } catch (Throwable ignored) {}

            int columns = 6;
            int rows = Math.max(1, (outputs.size() + columns - 1) / columns);
            int maxVisibleRows = Math.min(rows, 3);
            var grid = Widgets.AddGrid.items(0, 0, columns, rows, Widgets.AddSlot.output(0, 0, outputs, 0)).hideEmptySlots().positionBottomCenter();
            var scroll = Widgets.AddScroll.grid(0, 0, maxVisibleRows, grid).positionBottomCenter();

            int effCols = (grid instanceof Widgets.AddGrid.GridBuilder gb) ? gb.getEffectiveColumns() : columns;
            int effRows = (grid instanceof Widgets.AddGrid.GridBuilder gb2) ? gb2.getEffectiveRows() : rows;

            if (rows <= maxVisibleRows) {
                builder.addSlot(grid);
                builder.contentSize(contentWidth() + effCols * 26, contentHeight() + effRows * 26);
            } else {
                builder.addSlot(scroll);
                builder.contentSize((contentWidth() + effCols * 25) - 2, contentHeight() + Math.min(maxVisibleRows, effRows) * 26);
            }
        } else {
            builder.addSlot(Widgets.AddSlot.output(50, 4, out, 0));
            builder.contentSize(contentWidth(), contentHeight());
        }

        return builder.build();
    }

    @Override public int contentWidth() { return 0; }
    @Override public int contentHeight() { return 21; }

    // Per-frame dedupe state for holder-based builds
    private static long DEDUPE_TICK = -1L;
    private static final java.util.Set<String> DEDUPE_SEEN = new java.util.HashSet<>();

    private static ItemStack assembleCookingOutput(WorkstationRecipe recipe) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var ra = (mc != null && mc.level != null) ? mc.level.registryAccess() : null;
            if (ra == null || recipe == null) return ItemStack.EMPTY;
            ItemStack first = recipe.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
            var single = new SingleRecipeInput(first);
            return recipe.assemble(single, ra);
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private java.util.List<ItemStack> enumerateOutputsFor(WorkstationRecipe seed) {
        java.util.List<ItemStack> out = new java.util.ArrayList<>();
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            Object server = mc != null ? mc.getSingleplayerServer() : null;
            if (server == null) return out;
            Object rmObj = null;
            try { var m = server.getClass().getMethod("getRecipeManager"); rmObj = m.invoke(server); } catch (Throwable ignored) {}
            if (!(rmObj instanceof net.minecraft.world.item.crafting.RecipeManager rm)) return out;
            var ra = (mc.level != null) ? mc.level.registryAccess() : null;
            if (ra == null) return out;

            ItemStack seedFirst = seed.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
            for (var holder : rm.getRecipes()) {
                try {
                    var r = holder.value();
                    if (!(r instanceof WorkstationRecipe w)) continue;
                    if (w.getInputCount() != seed.getInputCount()) continue;
                    ItemStack otherFirst = w.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
                    boolean same = (!seedFirst.isEmpty() && w.input().test(seedFirst)) || (!otherFirst.isEmpty() && seed.input().test(otherFirst));
                    if (!same) continue;
                    ItemStack result = assembleCookingOutput(w);
                    if (!result.isEmpty()) out.add(result);
                } catch (Throwable ignored) {}
            }
            // Deduplicate by item id while preserving order
            java.util.LinkedHashMap<String, ItemStack> uniq = new java.util.LinkedHashMap<>();
            for (ItemStack s : out) {
                String key = String.valueOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()));
                uniq.putIfAbsent(key, s);
            }
            out = new java.util.ArrayList<>(uniq.values());
        } catch (Throwable ignored) {}
        return out;
    }
}