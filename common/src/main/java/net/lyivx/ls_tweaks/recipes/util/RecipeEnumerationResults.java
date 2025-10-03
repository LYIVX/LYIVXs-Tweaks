package net.lyivx.ls_tweaks.recipes.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

/** Convenience filters over RecipeManager#getRecipes() for result/ingredient queries. */
public final class RecipeEnumerationResults {
    public static List<RecipeHolder<?>> byResult(ItemStack result) {
        List<RecipeHolder<?>> out = new ArrayList<>();
        if (result == null || result.isEmpty()) return out;
        RecipeManager mgr = clientRecipeManager();
        if (mgr == null) return out;
        var mc = Minecraft.getInstance();
        var ra = (mc != null && mc.level != null) ? mc.level.registryAccess() : null;
        for (RecipeHolder<?> rh : mgr.getRecipes()) {
            try {
                Recipe<?> r = rh.value();
                ItemStack produced = ItemStack.EMPTY;
                if (r instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting && ra != null) {
                    var input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY));
                    produced = crafting.assemble(input, ra);
                } else if (r instanceof net.minecraft.world.item.crafting.AbstractCookingRecipe cooking && ra != null) {
                    ItemStack inStack = cooking.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
                    var single = new net.minecraft.world.item.crafting.SingleRecipeInput(inStack);
                    produced = cooking.assemble(single, ra);
                } else if (ra != null) {
                    // Generic: try assemble(SingleRecipeInput, Provider) reflectively for custom types (e.g., Workstation)
                    try {
                        java.lang.reflect.Method asm = r.getClass().getMethod("assemble", net.minecraft.world.item.crafting.SingleRecipeInput.class, net.minecraft.core.HolderLookup.Provider.class);
                        ItemStack first = firstCandidateFromRecipe(r);
                        if (!first.isEmpty()) {
                            var single = new net.minecraft.world.item.crafting.SingleRecipeInput(first);
                            Object res = asm.invoke(r, single, ra);
                            if (res instanceof ItemStack s) produced = s;
                        }
                    } catch (Throwable ignored) {}
                } else {
                    produced = DisplayUtil.firstOutput(rh);
                }
                if (!produced.isEmpty() && (ItemStack.isSameItemSameComponents(produced, result) || produced.getItem() == result.getItem())) {
                    out.add(rh);
                }
            } catch (Throwable ignored) {}
        }
        return out;
    }

    private static ItemStack firstCandidateFromRecipe(Recipe<?> r) {
        // Prefer placementInfo().ingredients()
        try {
            var info = r.placementInfo();
            if (info != null && info.ingredients() != null) {
                for (var ing : info.ingredients()) {
                    if (ing == null) continue;
                    var opt = ing.items().findFirst();
                    if (opt.isPresent()) return new ItemStack(opt.get().value());
                }
            }
        } catch (Throwable ignored) {}
        // Try input() -> Ingredient
        try {
            var m = r.getClass().getMethod("input");
            Object ingObj = m.invoke(r);
            if (ingObj instanceof net.minecraft.world.item.crafting.Ingredient ing) {
                return ing.items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    public static List<RecipeHolder<?>> byIngredient(ItemStack ingredient) {
        List<RecipeHolder<?>> out = new ArrayList<>();
        if (ingredient == null || ingredient.isEmpty()) return out;
        RecipeManager mgr = clientRecipeManager();
        if (mgr == null) return out;
        for (RecipeHolder<?> rh : mgr.getRecipes()) {
            try {
                Recipe<?> r = rh.value();
                var info = r.placementInfo();
                if (info == null || info.ingredients() == null) continue;
                for (Ingredient ing : info.ingredients()) {
                    if (ing != null && ing.test(ingredient)) { out.add(rh); break; }
                }
            } catch (Throwable ignored) {}
        }
        return out;
    }

    private static RecipeManager clientRecipeManager() {
        try {
            var mc = Minecraft.getInstance();
            if (mc == null) return null;
            Object server = mc.getSingleplayerServer();
            if (server != null) {
                try { var m = server.getClass().getMethod("getRecipeManager"); Object rm = m.invoke(server); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
            }
            var conn = mc.getConnection();
            if (conn != null) {
                try { var m = conn.getClass().getMethod("getRecipeManager"); Object rm = m.invoke(conn); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
                for (var m : conn.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                        try { Object rm = m.invoke(conn); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private RecipeEnumerationResults() {}
}


