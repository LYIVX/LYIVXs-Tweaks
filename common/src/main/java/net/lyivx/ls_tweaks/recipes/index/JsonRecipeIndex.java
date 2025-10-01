package net.lyivx.ls_tweaks.recipes.index;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.SlotWidget;
import net.lyivx.ls_tweaks.api.recipes.TextureWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Minimal JSON-based indexer for common recipe types as a reliable fallback. */
public final class JsonRecipeIndex {
    private static final Map<String, List<JsonRecipe>> BY_RESULT = new HashMap<>();
    private static final Map<String, List<JsonRecipe>> BY_INGREDIENT = new HashMap<>();

    public static void rebuild() { rebuildFrom(Minecraft.getInstance().getResourceManager()); }

    public static void rebuildFrom(ResourceManager rm) {
        BY_RESULT.clear();
        BY_INGREDIENT.clear();
        try {
            var resources = rm.listResources("recipes", rl -> rl.getPath().endsWith(".json"));
            int count = 0;
            for (Map.Entry<ResourceLocation, Resource> e : resources.entrySet()) {
                try (var in = e.getValue().open()) {
                    JsonElement el = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    if (!el.isJsonObject()) continue;
                    JsonObject obj = el.getAsJsonObject();
                    String type = getType(obj);
                    if (type == null) continue;
                    JsonRecipe r = parseRecipe(type, obj);
                    if (r == null || r.result.isEmpty()) continue;
                    String outKey = keyOf(r.result);
                    BY_RESULT.computeIfAbsent(outKey, k -> new ArrayList<>()).add(r);
                    for (ItemStack inStack : r.inputs) {
                        if (inStack.isEmpty()) continue;
                        BY_INGREDIENT.computeIfAbsent(keyOf(inStack), k -> new ArrayList<>()).add(r);
                    }
                    count++;
                } catch (Throwable ignored) {}
            }
            System.out.println("[LS Tweaks][RV] JsonRecipeIndex built recipes=" + count + " resultsKeys=" + BY_RESULT.size());
        } catch (Throwable t) {
            System.out.println("[LS Tweaks][RV] JsonRecipeIndex error: " + t);
        }
    }

    public static List<RecipeDisplay> displaysForResult(ItemStack result) {
        List<RecipeDisplay> out = new ArrayList<>();
        var list = BY_RESULT.getOrDefault(keyOf(result), List.of());
        for (JsonRecipe r : list) out.add(toDisplay(r));
        return out;
    }

    public static List<RecipeDisplay> displaysForUsage(ItemStack ingredient) {
        List<RecipeDisplay> out = new ArrayList<>();
        var list = BY_INGREDIENT.getOrDefault(keyOf(ingredient), List.of());
        for (JsonRecipe r : list) out.add(toDisplay(r));
        return out;
    }

    private static RecipeDisplay toDisplay(JsonRecipe r) {
        List<SlotWidget> slots = new ArrayList<>();
        String t = r.type;
        if (t.endsWith(":crafting_shaped") || t.endsWith(":crafting") || t.endsWith(":crafting_shapeless")) {
            int idx = 0;
            for (int rr = 0; rr < 3; rr++) {
                for (int cc = 0; cc < 3; cc++) {
                    ItemStack st = idx < r.inputs.size() ? r.inputs.get(idx) : ItemStack.EMPTY;
                    slots.add(SlotWidget.input(cc * 18, rr * 18, st, idx));
                    idx++;
                }
            }
            slots.add(SlotWidget.output(110, 18, r.result, 0));
        } else {
            ItemStack in = r.inputs.isEmpty() ? ItemStack.EMPTY : r.inputs.get(0);
            slots.add(SlotWidget.input(0, 18, in, 0));
            slots.add(SlotWidget.output(110, 18, r.result, 0));
        }
        List<TextureWidget> textures = new ArrayList<>();
        TextureWidget arrow = new TextureWidget(78, 20, ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/widgets/arrows.png"), 0, 0, 24, 17, 24, 17);
        textures.add(arrow);
        return new SimpleDisplay(r.result.getHoverName().getString(), slots, textures);
    }

    private static String keyOf(ItemStack s) {
        return s.getItemHolder().unwrapKey().map(k -> k.location().toString()).orElse(s.getHoverName().getString());
    }

    private static String getType(JsonObject obj) {
        if (obj.has("type")) return obj.get("type").getAsString();
        return null;
    }

    private static JsonRecipe parseRecipe(String type, JsonObject obj) {
        ItemStack result = parseResult(obj.getAsJsonObject("result"));
        List<ItemStack> inputs = new ArrayList<>();
        if (type.endsWith(":crafting_shaped") || type.endsWith(":crafting")) {
            if (obj.has("key") && obj.has("pattern")) {
                JsonObject key = obj.getAsJsonObject("key");
                java.util.Map<Character, ItemStack> chToItem = new java.util.HashMap<>();
                for (var e : key.entrySet()) {
                    char ch = e.getKey().charAt(0);
                    JsonObject ingr = e.getValue().getAsJsonObject();
                    ItemStack st = parseIngredientFirstItem(ingr);
                    chToItem.put(ch, st);
                }
                var pattern = obj.getAsJsonArray("pattern");
                for (int i = 0; i < 3; i++) {
                    String row = i < pattern.size() ? pattern.get(i).getAsString() : "";
                    for (int j = 0; j < 3; j++) {
                        char ch = j < row.length() ? row.charAt(j) : ' ';
                        inputs.add(ch == ' ' ? ItemStack.EMPTY : chToItem.getOrDefault(ch, ItemStack.EMPTY));
                    }
                }
            }
        } else if (type.endsWith(":crafting_shapeless")) {
            if (obj.has("ingredients")) {
                var arr = obj.getAsJsonArray("ingredients");
                for (int i = 0; i < arr.size(); i++) {
                    ItemStack st = parseIngredientFirstItem(arr.get(i).getAsJsonObject());
                    inputs.add(st);
                }
            }
        } else if (type.endsWith(":smelting") || type.endsWith(":blasting") || type.endsWith(":smoking") || type.endsWith(":stonecutting")) {
            if (obj.has("ingredient")) {
                ItemStack st = parseIngredientFirstItem(obj.getAsJsonObject("ingredient"));
                if (!st.isEmpty()) inputs.add(st);
            }
        }
        return new JsonRecipe(type, result, inputs);
    }

    private static ItemStack parseResult(JsonObject res) {
        if (res == null) return ItemStack.EMPTY;
        String id = res.has("id") ? res.get("id").getAsString() : (res.has("item") ? res.get("item").getAsString() : null);
        if (id == null) return ItemStack.EMPTY;
        int count = res.has("count") ? res.get("count").getAsInt() : 1;
        Item it = resolveItemById(id);
        return it == null ? ItemStack.EMPTY : new ItemStack(it, count);
    }

    private static ItemStack parseIngredientFirstItem(JsonObject ingr) {
        if (ingr == null) return ItemStack.EMPTY;
        // Prefer "item"; ignore tags for now
        if (ingr.has("item")) {
            Item it = resolveItemById(ingr.get("item").getAsString());
            if (it != null) return new ItemStack(it);
        }
        return ItemStack.EMPTY;
    }

    private static Item resolveItemById(String id) {
        try {
            Object registry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
            ResourceLocation rl = ResourceLocation.parse(id);
            // Try method get(ResourceLocation)
            try {
                Object val = registry.getClass().getMethod("get", ResourceLocation.class).invoke(registry, rl);
                Item it = extractItem(val);
                if (it != null) return it;
            } catch (Throwable ignored) {}
            // Try getOptional(ResourceLocation)
            try {
                Object val = registry.getClass().getMethod("getOptional", ResourceLocation.class).invoke(registry, rl);
                Item it = extractItem(val);
                if (it != null) return it;
            } catch (Throwable ignored) {}
            // Try getHolder(ResourceLocation)
            try {
                Object val = registry.getClass().getMethod("getHolder", ResourceLocation.class).invoke(registry, rl);
                Item it = extractItem(val);
                if (it != null) return it;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return null;
    }

    private static Item extractItem(Object val) {
        if (val == null) return null;
        try {
            if (val instanceof Item) return (Item)val;
            if (val instanceof java.util.Optional<?> o) {
                if (o.isEmpty()) return null;
                Object ref = o.get();
                return extractItem(ref);
            }
            // Holder or Holder.Reference
            try {
                Object inner = val.getClass().getMethod("value").invoke(val);
                if (inner instanceof Item) return (Item)inner;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return null;
    }

    private record JsonRecipe(String type, ItemStack result, List<ItemStack> inputs) {}

    private static final class SimpleDisplay implements RecipeDisplay {
        private final String title; private final List<SlotWidget> slots; private final List<TextureWidget> textures;
        private SimpleDisplay(String title, List<SlotWidget> slots, List<TextureWidget> textures) { this.title = title; this.slots = slots; this.textures = textures; }
        public String titleText() { return title; }
        public java.util.List<SlotWidget> slots() { return slots; }
        public java.util.List<TextureWidget> textures() { return textures; }
    }

    private JsonRecipeIndex() {}
}


