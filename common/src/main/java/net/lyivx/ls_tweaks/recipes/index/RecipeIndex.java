package net.lyivx.ls_tweaks.recipes.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.lyivx.ls_tweaks.recipes.util.RecipeUtil;

/**
 * Efficient lookups for recipes by result (show recipe) and by ingredient (show usages).
 * Built from the world's RecipeManager. Call rebuild() on world join/data reload.
 */
public final class RecipeIndex {
    private static final Map<String, List<RecipeHolder<?>>> BY_RESULT_ID = new ConcurrentHashMap<>();
    private static final Map<String, List<RecipeHolder<?>>> BY_INGREDIENT_ID = new ConcurrentHashMap<>();
    
    /** Build on-demand; avoid rebuilding on every click. */
    public static void ensureBuilt() {
        if (BY_RESULT_ID.isEmpty() && BY_INGREDIENT_ID.isEmpty()) rebuild();
    }

    public static void rebuild() {
        BY_RESULT_ID.clear();
        BY_INGREDIENT_ID.clear();
        RecipeManager mgr = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.getClientRecipeManager();
        java.util.List<Object> fallbackRecipes = java.util.List.of();
        if (mgr == null) {
            System.out.println("[LS Tweaks][RV] WARN: RecipeManager is null during index rebuild and build-from-resources failed; trying RegistryAccess direct listing");
            fallbackRecipes = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.fetchRecipesFromRegistryAccess();
            if (fallbackRecipes.isEmpty()) return;
        }

        int count = 0;
        final int[] dbg = new int[]{0};
        java.util.function.Consumer<RecipeHolder<?>> indexHolder = holder -> {
            Recipe<?> r = holder.value();
            // Lightweight indexing to avoid freezes: use direct result and ingredients only
            for (ItemStack result : RecipeUtil.outputsFrom(r)) {
                if (!result.isEmpty()) {
                    String key = keyOf(result);
                    BY_RESULT_ID.computeIfAbsent(key, k -> new ArrayList<>()).add(holder);
                }
            }
            for (ItemStack match : RecipeUtil.inputsFrom(r)) {
                if (match.isEmpty()) continue;
                String ikey = keyOf(match);
                BY_INGREDIENT_ID.computeIfAbsent(ikey, k -> new ArrayList<>()).add(holder);
                dbg[0]++;
            }
        };

        if (mgr != null) {
            for (RecipeHolder<?> holder : mgr.getRecipes()) { indexHolder.accept(holder); count++; }
        } else {
            for (Object obj : fallbackRecipes) {
                if (obj instanceof RecipeHolder<?> rh) { indexHolder.accept(rh); count++; }
                else if (obj instanceof Recipe<?> r) {
                    // Reflectively construct a RecipeHolder if possible
                    try {
                        java.lang.reflect.Constructor<?> c = RecipeHolder.class.getConstructors()[0];
                        Object rh = c.newInstance(net.minecraft.resources.ResourceLocation.withDefaultNamespace("synthetic"), r);
                        if (rh instanceof RecipeHolder<?> rhh) indexHolder.accept(rhh);
                        count++;
                    } catch (Throwable ignored) {
                        // As a last resort, skip
                    }
                }
            }
        }
        // Removed JsonRecipeIndex fallback - using working recipe system only
        System.out.println("[LS Tweaks][RV] Index built: recipes=" + count + " resultsKeys=" + BY_RESULT_ID.size() + " ingredientKeys=" + BY_INGREDIENT_ID.size() + " totalIngredients=" + dbg[0]);
    }

    public static List<RecipeHolder<?>> recipesFor(ItemStack result) {
        if (result == null || result.isEmpty()) return List.of();
        return BY_RESULT_ID.getOrDefault(keyOf(result), List.of());
    }

    public static List<RecipeHolder<?>> usagesFor(ItemStack ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return List.of();
        String key = keyOf(ingredient);
        List<RecipeHolder<?>> result = BY_INGREDIENT_ID.getOrDefault(key, List.of());
        System.out.println("[LS Tweaks][RV] usagesFor(" + ingredient.getHoverName().getString() + ") key=" + key + " found=" + result.size() + " totalIngredientKeys=" + BY_INGREDIENT_ID.size());
        return result;
    }
    
    public static List<RecipeHolder<?>> getAllRecipes() {
        List<RecipeHolder<?>> allRecipes = new ArrayList<>();
        allRecipes.addAll(BY_RESULT_ID.values().stream().flatMap(List::stream).distinct().toList());
        allRecipes.addAll(BY_INGREDIENT_ID.values().stream().flatMap(List::stream).distinct().toList());
        return allRecipes.stream().distinct().toList();
    }

    private static String keyOf(ItemStack stack) {
        return stack.getItemHolder().unwrapKey().map(k -> k.location().toString()).orElse(stack.getHoverName().getString());
    }

    private RecipeIndex() {}
}


