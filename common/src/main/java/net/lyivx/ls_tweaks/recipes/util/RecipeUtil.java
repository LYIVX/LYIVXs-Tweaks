package net.lyivx.ls_tweaks.recipes.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.core.RegistryAccess;

public final class RecipeUtil {
    private static RecipeManager CLIENT_BUILT_MANAGER;
    public static ItemStack resultOf(Recipe<?> recipe) {
        try {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                // Try new signature: getResultItem(RegistryAccess)
                try {
                    java.lang.reflect.Method m = recipe.getClass().getMethod("getResultItem", Class.forName("net.minecraft.core.RegistryAccess"));
                    Object res = m.invoke(recipe, level.registryAccess());
                    if (res instanceof ItemStack s) return s;
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        // Fallback older signature: getResultItem()
        try {
            java.lang.reflect.Method m = recipe.getClass().getMethod("getResultItem");
            Object res = m.invoke(recipe);
            if (res instanceof ItemStack s) return s;
        } catch (Throwable ignored) {}
        // Fallback: any no-arg method that returns ItemStack and name hints a result
        try {
            for (java.lang.reflect.Method m : recipe.getClass().getMethods()) {
                if (m.getParameterCount() != 0) continue;
                if (!ItemStack.class.isAssignableFrom(m.getReturnType())) continue;
                String n = m.getName().toLowerCase();
                if (!(n.contains("result") || n.contains("output") || n.contains("product"))) continue;
                Object res = m.invoke(recipe);
                if (res instanceof ItemStack s && !s.isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        // Fallback: any public field that is an ItemStack and name hints a result
        try {
            for (java.lang.reflect.Field f : recipe.getClass().getFields()) {
                if (!ItemStack.class.isAssignableFrom(f.getType())) continue;
                String n = f.getName().toLowerCase();
                if (!(n.contains("result") || n.contains("output") || n.contains("product"))) continue;
                Object val = f.get(recipe);
                if (val instanceof ItemStack s && !s.isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> ingredientsFirstStacks(Recipe<?> recipe) {
        List<ItemStack> out = new ArrayList<>();
        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Starting for " + recipe.getClass().getSimpleName());
        
        // Try getIngredients method first
        try {
            java.lang.reflect.Method m = recipe.getClass().getMethod("getIngredients");
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Found getIngredients method");
            Object list = m.invoke(recipe);
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getIngredients returned: " + (list != null ? list.getClass().getSimpleName() : "null"));
            if (list instanceof java.util.List<?> ls) {
                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: List size: " + ls.size());
                for (int i = 0; i < ls.size(); i++) {
                    Object o = ls.get(i);
                    System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: List[" + i + "] = " + (o != null ? o.getClass().getSimpleName() : "null"));
                    if (o instanceof Ingredient ing) {
                        try {
                            // Try different method signatures for getItems()
                            ItemStack[] arr = null;
                            
                            // Try getItems() with no parameters first
                            try {
                                arr = (ItemStack[])Ingredient.class.getMethod("getItems").invoke(ing);
                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems() (no params) returned array of length " + (arr != null ? arr.length : 0));
                            } catch (Throwable t1) {
                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems() (no params) failed: " + t1.getMessage());
                                
                                // Try getItems() with RegistryAccess parameter
                                try {
                                    RegistryAccess access = getClientRegistryAccess();
                                    if (access != null) {
                                        arr = (ItemStack[])Ingredient.class.getMethod("getItems", RegistryAccess.class).invoke(ing, access);
                                        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems(RegistryAccess) returned array of length " + (arr != null ? arr.length : 0));
                                    }
                                } catch (Throwable t2) {
                                    System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems(RegistryAccess) failed: " + t2.getMessage());
                                    
                                    // Try getItems() with HolderLookup.Provider parameter
                                    try {
                                        RegistryAccess access = getClientRegistryAccess();
                                        if (access != null) {
                                            arr = (ItemStack[])Ingredient.class.getMethod("getItems", Class.forName("net.minecraft.core.HolderLookup$Provider")).invoke(ing, access);
                                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems(HolderLookup.Provider) returned array of length " + (arr != null ? arr.length : 0));
                                        }
                                    } catch (Throwable t3) {
                                        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getItems(HolderLookup.Provider) failed: " + t3.getMessage());
                                        
                                        // Try the new items() method that returns a Stream
                                        try {
                                            Object stream = Ingredient.class.getMethod("items").invoke(ing);
                                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: items() returned: " + (stream != null ? stream.getClass().getSimpleName() : "null"));
                                            if (stream instanceof java.util.stream.Stream<?> s) {
                                                // First, let's see what's actually in the stream
                                                List<Object> rawItems = s.collect(java.util.stream.Collectors.toList());
                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: items() stream raw size: " + rawItems.size());
                                                for (int j = 0; j < Math.min(rawItems.size(), 5); j++) {
                                                    Object obj = rawItems.get(j);
                                                    System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: stream[" + j + "] = " + (obj != null ? obj.getClass().getSimpleName() + " (" + obj + ")" : "null"));
                                                }
                                                
                                                // Now try to convert to ItemStacks
                                                List<ItemStack> streamItems = rawItems.stream().map(obj -> {
                                                    if (obj instanceof ItemStack itemStack) {
                                                        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Found ItemStack: " + itemStack.getHoverName().getString());
                                                        return itemStack;
                                                    } else if (obj != null) {
                                                        // Try to convert Reference or other objects to ItemStack
                                                        try {
                                                            // Check if it's a Reference object
                                                            if (obj.getClass().getSimpleName().equals("Reference")) {
                                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Found Reference: " + obj);
                                                                
                                                                // Try to get the ResourceKey from the Reference
                                                                java.lang.reflect.Method keyMethod = obj.getClass().getMethod("key");
                                                                Object key = keyMethod.invoke(obj);
                                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Reference key: " + key);
                                                                
                                                                // Try to get the location from the key
                                                                java.lang.reflect.Method locationMethod = key.getClass().getMethod("location");
                                                                Object location = locationMethod.invoke(key);
                                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Key location: " + location);
                                                                
                                                                if (location instanceof ResourceLocation rl) {
                                                                    // Try to get the item from the registry
                                                                    try {
                                                                        RegistryAccess access = Minecraft.getInstance().level.registryAccess();
                                                                        java.lang.reflect.Method registryMethod = access.getClass().getMethod("lookupOrThrow", Class.forName("net.minecraft.resources.ResourceKey"));
                                                                        Object registry = registryMethod.invoke(access, Registries.ITEM);
                                                                        java.lang.reflect.Method getMethod = registry.getClass().getMethod("get", ResourceLocation.class);
                                                                        Object itemOptional = getMethod.invoke(registry, rl);
                                                                        if (itemOptional != null) {
                                                                            // Handle Optional<Item>
                                                                            if (itemOptional instanceof java.util.Optional<?> opt) {
                                                                                if (opt.isPresent()) {
                                                                                    Object item = opt.get();
                                                                                    if (item instanceof net.minecraft.world.item.Item) {
                                                                                        ItemStack stack = new ItemStack((net.minecraft.world.item.Item) item);
                                                                                        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                                        return stack;
                                                                                    } else if (item.getClass().getSimpleName().equals("Reference")) {
                                                                                        // The registry returned a Reference, need to get the actual Item
                                                                                        try {
                                                                                            java.lang.reflect.Method valueMethod = item.getClass().getMethod("value");
                                                                                            Object actualItem = valueMethod.invoke(item);
                                                                                            if (actualItem instanceof net.minecraft.world.item.Item) {
                                                                                                ItemStack stack = new ItemStack((net.minecraft.world.item.Item) actualItem);
                                                                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                                                return stack;
                                                                                            }
                                                                                        } catch (Throwable t) {
                                                                                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Failed to get value from Reference: " + t.getMessage());
                                                                                        }
                                                                                    }
                                                                                }
                                                                            } else if (itemOptional instanceof net.minecraft.world.item.Item) {
                                                                                ItemStack stack = new ItemStack((net.minecraft.world.item.Item) itemOptional);
                                                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                                return stack;
                                                                            }
                                                                        }
                                                                    } catch (Throwable t) {
                                                                        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Failed to get item from registry: " + t.getMessage());
                                                                    }
                                                                }
                                                            }
                                                        } catch (Throwable t) {
                                                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Failed to convert object to ItemStack: " + t.getMessage());
                                                        }
                                                    }
                                                    return ItemStack.EMPTY;
                                                }).filter(itemStack -> !itemStack.isEmpty()).collect(java.util.stream.Collectors.toList());
                                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: items() stream collected " + streamItems.size() + " items");
                                                if (!streamItems.isEmpty()) {
                                                    arr = streamItems.toArray(new ItemStack[0]);
                                                    System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: items() stream converted to array of length " + arr.length);
                                                }
                                            }
                                        } catch (Throwable t4) {
                                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: items() failed: " + t4.getMessage());
                                        }
                                    }
                                }
                            }
                            
                            if (arr != null && arr.length > 0) {
                                System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: First stack: " + arr[0].getHoverName().getString());
                                out.add(arr[0]);
                            }
                        } catch (Throwable t) {
                            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Error calling getItems(): " + t.getMessage());
                        }
                    }
                }
            }
        } catch (Throwable t) {
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Error with getIngredients: " + t.getMessage());
        }
        
        if (!out.isEmpty()) {
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: getIngredients path found " + out.size() + " ingredients for " + recipe.getClass().getSimpleName());
            return out;
        }
        
        // Fallback for 1.21.4: traverse fields/getters to find Ingredient or collections of Ingredient
        System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Trying deep collection fallback");
        try {
            collectIngredientStacksDeep(recipe, out, new java.util.IdentityHashMap<>());
        } catch (Throwable t) {
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: Error in deep collection: " + t.getMessage());
        }
        
        if (!out.isEmpty()) {
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: deep collection found " + out.size() + " ingredients for " + recipe.getClass().getSimpleName());
        } else {
            System.out.println("[LS Tweaks][RV] ingredientsFirstStacks: NO ingredients found for " + recipe.getClass().getSimpleName());
        }
        return out;
    }

    /** Returns the raw display objects for a recipe, if the API is available. */
    public static java.util.List<?> displaysOf(Recipe<?> recipe) {
        try {
            java.lang.reflect.Method m = recipe.getClass().getMethod("display");
            Object o = m.invoke(recipe);
            if (o instanceof java.util.List<?> ls) return ls;
        } catch (Throwable ignored) {}
        return java.util.List.of();
    }

    /** Conservative extraction of output stacks to avoid freezes. */
    public static java.util.List<ItemStack> outputsFrom(Recipe<?> recipe) {
        java.util.List<ItemStack> stacks = new ArrayList<>();
        ItemStack fallback = resultOf(recipe);
        if (!fallback.isEmpty()) stacks.add(fallback);
        return stacks;
    }

    /** Conservative extraction of input stacks to avoid freezes. */
    public static java.util.List<ItemStack> inputsFrom(Recipe<?> recipe) {
        java.util.List<ItemStack> stacks = new ArrayList<>();
        // 1) Fast path via server-synchronized display entries
        try {
            net.minecraft.world.item.crafting.RecipeHolder<?> holder = findHolderForRecipe(recipe);
            if (holder != null) {
                java.util.List<Object> entries = serverDisplayEntriesFor(holder);
                for (Object e : entries) {
                    stacks.addAll(stacksFromServerDisplayEntry(e, false));
                }
                if (!stacks.isEmpty()) {
                    System.out.println("[LS Tweaks][RV] inputsFrom: server display path found " + stacks.size() + " inputs for " + recipe.getClass().getSimpleName());
                    return stacks;
                }
            }
        } catch (Throwable ignored) {}
        // 2) Fallback: first item of each Ingredient
        stacks.addAll(ingredientsFirstStacks(recipe));
        if (!stacks.isEmpty()) {
            System.out.println("[LS Tweaks][RV] inputsFrom: fallback path found " + stacks.size() + " inputs for " + recipe.getClass().getSimpleName());
        } else {
            System.out.println("[LS Tweaks][RV] inputsFrom: NO inputs found for " + recipe.getClass().getSimpleName());
        }
        return stacks;
    }

    // Integrated-server display extraction helpers
    public static java.util.List<Object> serverDisplayEntriesFor(net.minecraft.world.item.crafting.RecipeHolder<?> holder) {
        java.util.List<Object> out = new java.util.ArrayList<>();
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            Object server = mc.getSingleplayerServer();
            if (server == null) return out;
            net.minecraft.world.item.crafting.RecipeManager rm = null;
            try {
                java.lang.reflect.Method m = server.getClass().getMethod("getRecipeManager");
                Object r = m.invoke(server);
                if (r instanceof net.minecraft.world.item.crafting.RecipeManager rmm) rm = rmm;
            } catch (Throwable ignored) {}
            if (rm == null) return out;
            java.util.List<Object> list = new java.util.ArrayList<>();
            java.util.function.Consumer<Object> cons = list::add;
            try {
                // rm.listDisplaysForRecipe(ResourceKey, Consumer)
                java.lang.reflect.Method m = net.minecraft.world.item.crafting.RecipeManager.class.getMethod("listDisplaysForRecipe", Class.forName("net.minecraft.resources.ResourceKey"), java.util.function.Consumer.class);
                java.lang.reflect.Method idM = holder.getClass().getMethod("id");
                Object key = idM.invoke(holder);
                m.invoke(rm, key, cons);
            } catch (Throwable ignored) {}
            out.addAll(list);
        } catch (Throwable ignored) {}
        return out;
    }

    public static java.util.List<ItemStack> stacksFromServerDisplayEntry(Object entry, boolean outputs) {
        java.util.List<ItemStack> out = new java.util.ArrayList<>();
        if (entry == null) return out;
        // First, handle known 1.21.4 API: RecipeDisplayEntry
        try {
            String clsName = entry.getClass().getName();
            if (clsName.equals("net.minecraft.world.item.crafting.display.RecipeDisplayEntry") || clsName.endsWith(".display.RecipeDisplayEntry")) {
                if (outputs) {
                    try {
                        // Use SlotDisplayContext.fromLevel(level) for result resolution (faster than EMPTY traversal)
                        Class<?> sdc = Class.forName("net.minecraft.world.item.crafting.display.SlotDisplayContext");
                        java.lang.reflect.Method fromLevel = sdc.getMethod("fromLevel", Class.forName("net.minecraft.world.level.Level"));
                        Object ctx = fromLevel.invoke(null, Minecraft.getInstance().level);
                        java.lang.reflect.Method ctxMap = sdc.getMethod("context");
                        Object contextMap = ctxMap.invoke(ctx);
                        java.lang.reflect.Method resultItems = entry.getClass().getMethod("resultItems", Class.forName("net.minecraft.util.context.ContextMap"));
                        Object list = resultItems.invoke(entry, contextMap);
                        if (list instanceof java.util.List<?> ls) {
                            for (Object o : ls) if (o instanceof ItemStack s && !s.isEmpty()) out.add(s);
                            if (!out.isEmpty()) return out;
                        }
                    } catch (Throwable ignored) {}
                } else {
                    try {
                        java.lang.reflect.Method reqM = entry.getClass().getMethod("craftingRequirements");
                        Object opt = reqM.invoke(entry);
                        if (opt instanceof java.util.Optional<?> o && o.isPresent()) {
                            Object list = o.get();
                            if (list instanceof java.util.List<?> ls) {
                                for (Object ingObj : ls) {
                                    if (!(ingObj instanceof net.minecraft.world.item.crafting.Ingredient ing)) continue;
                                    try {
                                        // Try different method signatures for getItems()
                                        ItemStack[] arr = null;
                                        
                                        // Try getItems() with no parameters first
                                        try {
                                            arr = (ItemStack[])net.minecraft.world.item.crafting.Ingredient.class.getMethod("getItems").invoke(ing);
                                        } catch (Throwable t1) {
                                            // Try getItems() with RegistryAccess parameter
                                            try {
                                                RegistryAccess access = getClientRegistryAccess();
                                                if (access != null) {
                                                    arr = (ItemStack[])net.minecraft.world.item.crafting.Ingredient.class.getMethod("getItems", RegistryAccess.class).invoke(ing, access);
                                                }
                                            } catch (Throwable t2) {
                                        // Try getItems() with HolderLookup.Provider parameter
                                        try {
                                            RegistryAccess access = getClientRegistryAccess();
                                            if (access != null) {
                                                arr = (ItemStack[])net.minecraft.world.item.crafting.Ingredient.class.getMethod("getItems", Class.forName("net.minecraft.core.HolderLookup$Provider")).invoke(ing, access);
                                            }
                                        } catch (Throwable t3) {
                                            // Try the new items() method that returns a Stream
                                            try {
                                                Object stream = net.minecraft.world.item.crafting.Ingredient.class.getMethod("items").invoke(ing);
                                                if (stream instanceof java.util.stream.Stream<?> s) {
                                                    List<ItemStack> streamItems = s.map(obj -> {
                                                        if (obj instanceof ItemStack itemStack) return itemStack;
                                                        return ItemStack.EMPTY;
                                                    }).filter(itemStack -> !itemStack.isEmpty()).collect(java.util.stream.Collectors.toList());
                                                    if (!streamItems.isEmpty()) {
                                                        arr = streamItems.toArray(new ItemStack[0]);
                                                    }
                                                }
                                            } catch (Throwable t4) {
                                                // All methods failed
                                            }
                                        }
                                            }
                                        }
                                        
                                        if (arr != null) for (ItemStack s : arr) if (s != null && !s.isEmpty()) out.add(s);
                                    } catch (Throwable ignored) {}
                                }
                                if (!out.isEmpty()) return out;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        // Fallback: try to pull the inner display object and heuristically traverse it
        Object display = entry;
        try {
            try { java.lang.reflect.Field f = entry.getClass().getDeclaredField("display"); f.setAccessible(true); display = f.get(entry); } catch (Throwable ignored) {}
            if (display == null) {
                try { java.lang.reflect.Method m = entry.getClass().getMethod("getDisplay"); display = m.invoke(entry); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        collectStacksByName(display, out, outputs);
        return out;
    }

    private static void collectStacksByName(Object display, java.util.List<ItemStack> out, boolean outputs) {
        if (display == null) return;
        java.util.Set<Object> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        java.util.Deque<Object> stack = new java.util.ArrayDeque<>();
        stack.push(display);
        while (!stack.isEmpty()) {
            Object cur = stack.pop();
            if (cur == null || seen.contains(cur)) continue;
            seen.add(cur);

            if (cur instanceof ItemStack s) {
                if (!s.isEmpty()) out.add(s);
                continue;
            }
            if (cur instanceof Ingredient ing) {
                try {
                    // Try different method signatures for getItems()
                    ItemStack[] arr = null;
                    
                    // Try getItems() with no parameters first
                    try {
                        arr = (ItemStack[])Ingredient.class.getMethod("getItems").invoke(ing);
                    } catch (Throwable t1) {
                        // Try getItems() with RegistryAccess parameter
                        try {
                            RegistryAccess access = getClientRegistryAccess();
                            if (access != null) {
                                arr = (ItemStack[])Ingredient.class.getMethod("getItems", RegistryAccess.class).invoke(ing, access);
                            }
                        } catch (Throwable t2) {
                        // Try getItems() with HolderLookup.Provider parameter
                        try {
                            RegistryAccess access = getClientRegistryAccess();
                            if (access != null) {
                                arr = (ItemStack[])Ingredient.class.getMethod("getItems", Class.forName("net.minecraft.core.HolderLookup$Provider")).invoke(ing, access);
                            }
                        } catch (Throwable t3) {
                            // Try the new items() method that returns a Stream
                            try {
                                Object stream = Ingredient.class.getMethod("items").invoke(ing);
                                if (stream instanceof java.util.stream.Stream<?> s) {
                                    List<ItemStack> streamItems = s.map(obj -> {
                                        if (obj instanceof ItemStack itemStack) return itemStack;
                                        return ItemStack.EMPTY;
                                    }).filter(itemStack -> !itemStack.isEmpty()).collect(java.util.stream.Collectors.toList());
                                    if (!streamItems.isEmpty()) {
                                        arr = streamItems.toArray(new ItemStack[0]);
                                    }
                                }
                            } catch (Throwable t4) {
                                // All methods failed
                            }
                        }
                        }
                    }
                    
                    if (arr != null) for (ItemStack s : arr) if (s != null && !s.isEmpty()) out.add(s);
                } catch (Throwable ignored) {}
                continue;
            }
            if (cur instanceof java.util.Collection<?> coll) {
                for (Object o : coll) stack.push(o);
                continue;
            }
            if (cur.getClass().isArray()) {
                int len = java.lang.reflect.Array.getLength(cur);
                for (int i = 0; i < len; i++) stack.push(java.lang.reflect.Array.get(cur, i));
                continue;
            }

            // Heuristic: traverse getters whose names match outputs/inputs cues
            for (java.lang.reflect.Method m : cur.getClass().getMethods()) {
                if (m.getParameterCount() != 0) continue;
                String n = m.getName();
                if (n.equals("getClass")) continue;
                boolean nameMatches = outputs ? (n.contains("result") || n.contains("output") || n.contains("outputs") || n.contains("results"))
                                              : (n.contains("ingredient") || n.contains("input") || n.contains("inputs") || n.contains("ingredients"));
                if (!nameMatches) continue;
                try {
                    Object v = m.invoke(cur);
                    if (v != null) stack.push(v);
                } catch (Throwable ignored) {}
            }

            // Also traverse fields with matching names
            for (java.lang.reflect.Field f : cur.getClass().getFields()) {
                String n = f.getName();
                boolean nameMatches = outputs ? (n.contains("result") || n.contains("output")) : (n.contains("ingredient") || n.contains("input"));
                if (!nameMatches) continue;
                try {
                    Object v = f.get(cur);
                    if (v != null) stack.push(v);
                } catch (Throwable ignored) {}
            }
        }
    }

    public static ResourceLocation typeIdOf(Recipe<?> recipe) {
        try {
            // Preferred: holder key
            Object type = recipe.getType();
            // Fast path: BuiltInRegistries lookup
            try {
                net.minecraft.resources.ResourceLocation rl = net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.getKey((RecipeType<?>) type);
                if (rl != null) return rl;
            } catch (Throwable ignored) {}
            try {
                java.lang.reflect.Method keyM = type.getClass().getMethod("key");
                Object holder = keyM.invoke(type);
                if (holder != null) {
                    java.lang.reflect.Method locM = holder.getClass().getMethod("location");
                    Object loc = locM.invoke(holder);
                    if (loc instanceof ResourceLocation rl) return rl;
                }
            } catch (Throwable ignored) {}
            // Fallback: type.toString() (many RecipeType impls return their path)
            try {
                String ts = String.valueOf(type);
                if (ts != null && !ts.isEmpty() && !"null".equals(ts)) {
                    return ResourceLocation.withDefaultNamespace(ts);
                }
            } catch (Throwable ignored) {}
            // Fallback: registry lookup
            var access = getClientRegistryAccess();
            if (access != null) {
                try {
                    java.lang.reflect.Method regM = access.getClass().getMethod("registry", Class.forName("net.minecraft.resources.ResourceKey"));
                    Object opt = regM.invoke(access, Registries.RECIPE_TYPE);
                    if (opt instanceof java.util.Optional<?> o) {
                        Object regObj = o.orElse(null);
                        if (regObj instanceof Registry<?> r) {
                            @SuppressWarnings("unchecked")
                            Registry<RecipeType<?>> reg = (Registry<RecipeType<?>>) r;
                            ResourceLocation rl = reg.getKey((RecipeType<?>)type);
                            if (rl != null) return rl;
                        }
                    }
                } catch (Throwable ignored) {}
            }
            // Last-resort: derive from serializer id
            try {
                Object ser = recipe.getSerializer();
                net.minecraft.resources.ResourceLocation srl = net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER.getKey((net.minecraft.world.item.crafting.RecipeSerializer<?>) ser);
                if (srl != null) {
                    String path = srl.getPath();
                    String ns = srl.getNamespace();
                    if (path.startsWith("crafting")) {
                        return ResourceLocation.fromNamespaceAndPath(ns, "crafting");
                    }
                    return ResourceLocation.fromNamespaceAndPath(ns, path);
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return ResourceLocation.fromNamespaceAndPath("unknown", "unknown");
    }

    private static void collectIngredientStacksDeep(Object root, java.util.List<ItemStack> out, java.util.Map<Object, Boolean> seen) {
        if (root == null) return;
        if (seen.containsKey(root)) return;
        seen.put(root, Boolean.TRUE);
        
        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Examining " + root.getClass().getSimpleName());
        
        if (root instanceof Ingredient ing) {
            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Ingredient");
            try {
                // First, let's see what methods are available on Ingredient
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Available methods on Ingredient:");
                for (java.lang.reflect.Method m : Ingredient.class.getMethods()) {
                    if (m.getName().toLowerCase().contains("item") || m.getName().toLowerCase().contains("stack")) {
                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Method: " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ") -> " + m.getReturnType().getSimpleName());
                    }
                }
                
                // Try different method signatures for getItems()
                ItemStack[] arr = null;
                
                // Try getItems() with no parameters first
                try {
                    arr = (ItemStack[])Ingredient.class.getMethod("getItems").invoke(ing);
                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems() (no params) returned array of length " + (arr != null ? arr.length : 0));
                } catch (Throwable t1) {
                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems() (no params) failed: " + t1.getMessage());
                    
                    // Try getItems() with RegistryAccess parameter
                    try {
                        RegistryAccess access = getClientRegistryAccess();
                        if (access != null) {
                            arr = (ItemStack[])Ingredient.class.getMethod("getItems", RegistryAccess.class).invoke(ing, access);
                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(RegistryAccess) returned array of length " + (arr != null ? arr.length : 0));
                        }
                    } catch (Throwable t2) {
                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(RegistryAccess) failed: " + t2.getMessage());
                        
                        // Try getItems() with HolderLookup.Provider parameter
                        try {
                            RegistryAccess access = getClientRegistryAccess();
                            if (access != null) {
                                arr = (ItemStack[])Ingredient.class.getMethod("getItems", Class.forName("net.minecraft.core.HolderLookup$Provider")).invoke(ing, access);
                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(HolderLookup.Provider) returned array of length " + (arr != null ? arr.length : 0));
                            }
                        } catch (Throwable t3) {
                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(HolderLookup.Provider) failed: " + t3.getMessage());
                            
                            // Try the new items() method that returns a Stream
                            try {
                                Object stream = Ingredient.class.getMethod("items").invoke(ing);
                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: items() returned: " + (stream != null ? stream.getClass().getSimpleName() : "null"));
                                
                                // Check what methods are available on the returned object
                                if (stream != null) {
                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Available methods on " + stream.getClass().getSimpleName() + ":");
                                    for (java.lang.reflect.Method m : stream.getClass().getMethods()) {
                                        if (m.getName().toLowerCase().contains("item") || m.getName().toLowerCase().contains("stack") || m.getName().toLowerCase().contains("get") || m.getName().toLowerCase().contains("to")) {
                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Method: " + m.getName() + "(" + java.util.Arrays.toString(m.getParameterTypes()) + ") -> " + m.getReturnType().getSimpleName());
                                        }
                                    }
                                }
                                
                                if (stream instanceof java.util.stream.Stream<?> s) {
                                    // First, let's see what's actually in the stream
                                    List<Object> rawItems = s.collect(java.util.stream.Collectors.toList());
                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: items() stream raw size: " + rawItems.size());
                                    for (int i = 0; i < Math.min(rawItems.size(), 5); i++) {
                                        Object obj = rawItems.get(i);
                                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: stream[" + i + "] = " + (obj != null ? obj.getClass().getSimpleName() + " (" + obj + ")" : "null"));
                                    }
                                    
                                    // Now try to convert to ItemStacks
                                    List<ItemStack> streamItems = rawItems.stream().map(obj -> {
                                        if (obj instanceof ItemStack itemStack) {
                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found ItemStack: " + itemStack.getHoverName().getString());
                                            return itemStack;
                                        } else if (obj != null) {
                                            // Try to convert Reference or other objects to ItemStack
                                            try {
                                                // Check if it's a Reference object
                                                if (obj.getClass().getSimpleName().equals("Reference")) {
                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Reference: " + obj);
                                                    
                                                    // Try to get the ResourceKey from the Reference
                                                    java.lang.reflect.Method keyMethod = obj.getClass().getMethod("key");
                                                    Object key = keyMethod.invoke(obj);
                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Reference key: " + key);
                                                    
                                                    // Try to get the location from the key
                                                    java.lang.reflect.Method locationMethod = key.getClass().getMethod("location");
                                                    Object location = locationMethod.invoke(key);
                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Key location: " + location);
                                                    
                                                    if (location instanceof ResourceLocation rl) {
                                                        // Try to get the item from the registry
                                                        try {
                                                            RegistryAccess access = Minecraft.getInstance().level.registryAccess();
                                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Got registry access: " + access.getClass().getSimpleName());
                                                            java.lang.reflect.Method registryMethod = access.getClass().getMethod("lookupOrThrow", Class.forName("net.minecraft.resources.ResourceKey"));
                                                            Object registry = registryMethod.invoke(access, Registries.ITEM);
                                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Got registry: " + registry.getClass().getSimpleName());
                                                            java.lang.reflect.Method getMethod = registry.getClass().getMethod("get", ResourceLocation.class);
                                                            Object itemOptional = getMethod.invoke(registry, rl);
                                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Got itemOptional: " + (itemOptional != null ? itemOptional.getClass().getSimpleName() : "null"));
                                                            if (itemOptional != null) {
                                                                // Handle Optional<Item>
                                                                if (itemOptional instanceof java.util.Optional<?> opt) {
                                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Optional, isPresent: " + opt.isPresent());
                                                                    if (opt.isPresent()) {
                                                                        Object item = opt.get();
                                                                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Got item from Optional: " + item.getClass().getSimpleName());
                                                                        if (item instanceof net.minecraft.world.item.Item) {
                                                                            ItemStack stack = new ItemStack((net.minecraft.world.item.Item) item);
                                                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                            return stack;
                                                                        } else if (item.getClass().getSimpleName().equals("Reference")) {
                                                                            // The registry returned a Reference, need to get the actual Item
                                                                            try {
                                                                                java.lang.reflect.Method valueMethod = item.getClass().getMethod("value");
                                                                                Object actualItem = valueMethod.invoke(item);
                                                                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Got actual item from Reference: " + actualItem.getClass().getSimpleName());
                                                                                if (actualItem instanceof net.minecraft.world.item.Item) {
                                                                                    ItemStack stack = new ItemStack((net.minecraft.world.item.Item) actualItem);
                                                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                                    return stack;
                                                                                }
                                                                            } catch (Throwable t) {
                                                                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Failed to get value from Reference: " + t.getMessage());
                                                                            }
                                                                        }
                                                                    }
                                                                } else if (itemOptional instanceof net.minecraft.world.item.Item) {
                                                                    ItemStack stack = new ItemStack((net.minecraft.world.item.Item) itemOptional);
                                                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Converted Reference to ItemStack: " + stack.getHoverName().getString());
                                                                    return stack;
                                                                }
                                                            }
                                                        } catch (Throwable t) {
                                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Failed to get item from registry: " + t.getMessage());
                                                        }
                                                    }
                                                }
                                            } catch (Throwable t) {
                                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Failed to convert object to ItemStack: " + t.getMessage());
                                            }
                                        }
                                        return ItemStack.EMPTY;
                                    }).filter(itemStack -> !itemStack.isEmpty()).collect(java.util.stream.Collectors.toList());
                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: items() stream collected " + streamItems.size() + " items");
                                    if (!streamItems.isEmpty()) {
                                        arr = streamItems.toArray(new ItemStack[0]);
                                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: items() stream converted to array of length " + arr.length);
                                    }
                                } else {
                                    // Try to convert the object to a stream or extract items directly
                                    try {
                                        // Try to call toStream() or similar method
                                        java.lang.reflect.Method toStream = stream.getClass().getMethod("toStream");
                                        Object streamObj = toStream.invoke(stream);
                                        if (streamObj instanceof java.util.stream.Stream<?> s) {
                                            List<ItemStack> streamItems = s.map(obj -> {
                                                if (obj instanceof ItemStack itemStack) return itemStack;
                                                return ItemStack.EMPTY;
                                            }).filter(itemStack -> !itemStack.isEmpty()).collect(java.util.stream.Collectors.toList());
                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: toStream() collected " + streamItems.size() + " items");
                                            if (!streamItems.isEmpty()) {
                                                arr = streamItems.toArray(new ItemStack[0]);
                                            }
                                        }
                                    } catch (Throwable t5) {
                                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: toStream() failed: " + t5.getMessage());
                                        
                                        // Try to get items directly from the object
                                        try {
                                            java.lang.reflect.Method getItems = stream.getClass().getMethod("getItems");
                                            Object itemsObj = getItems.invoke(stream);
                                            if (itemsObj instanceof ItemStack[] items) {
                                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems() returned array of length " + items.length);
                                                arr = items;
                                            }
                                        } catch (Throwable t6) {
                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems() on Head failed: " + t6.getMessage());
                                        }
                                    }
                                }
                            } catch (Throwable t4) {
                                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: items() failed: " + t4.getMessage());
                                
                                // Try getMatchingStacks() - older method name
                                try {
                                    arr = (ItemStack[])Ingredient.class.getMethod("getMatchingStacks").invoke(ing);
                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getMatchingStacks() returned array of length " + (arr != null ? arr.length : 0));
                                } catch (Throwable t5) {
                                    System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getMatchingStacks() failed: " + t5.getMessage());
                                    
                                    // Try getItems() with different parameter types
                                    try {
                                        // Try with Level parameter
                                        Level level = Minecraft.getInstance().level;
                                        if (level != null) {
                                            arr = (ItemStack[])Ingredient.class.getMethod("getItems", Level.class).invoke(ing, level);
                                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(Level) returned array of length " + (arr != null ? arr.length : 0));
                                        }
                                    } catch (Throwable t6) {
                                        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: getItems(Level) failed: " + t6.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (arr != null) {
                    for (ItemStack s : arr) {
                        if (s != null && !s.isEmpty()) {
                            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Adding stack: " + s.getHoverName().getString());
                            out.add(s);
                        }
                    }
                }
            } catch (Throwable t) {
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Error with Ingredient methods: " + t.getMessage());
            }
            return;
        }
        Class<?> cls = root.getClass();
        if (root instanceof java.util.Optional<?> opt) {
            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Optional");
            opt.ifPresent(v -> collectIngredientStacksDeep(v, out, seen));
            return;
        }
        if (root instanceof java.util.Collection<?> coll) {
            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Collection of size " + coll.size());
            for (Object v : coll) collectIngredientStacksDeep(v, out, seen);
            return;
        }
        if (cls.isArray()) {
            int len = java.lang.reflect.Array.getLength(root);
            System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found Array of length " + len);
            for (int i = 0; i < len; i++) collectIngredientStacksDeep(java.lang.reflect.Array.get(root, i), out, seen);
            return;
        }
        // Try getters that look like ingredient/input providers
        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Checking methods for ingredient/input patterns");
        for (java.lang.reflect.Method m : cls.getMethods()) {
            try {
                if (m.getParameterCount() != 0) continue;
                String n = m.getName().toLowerCase();
                if (!(n.contains("ingredient") || n.contains("input") || n.contains("ingredients") || n.contains("inputs"))) continue;
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found matching method: " + m.getName());
                Object v = m.invoke(root);
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Method " + m.getName() + " returned: " + (v != null ? v.getClass().getSimpleName() : "null"));
                collectIngredientStacksDeep(v, out, seen);
            } catch (Throwable t) {
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Error calling method " + m.getName() + ": " + t.getMessage());
            }
        }
        // Try fields matching ingredient/input
        System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Checking fields for ingredient/input patterns");
        for (java.lang.reflect.Field f : cls.getFields()) {
            try {
                String n = f.getName().toLowerCase();
                if (!(n.contains("ingredient") || n.contains("input"))) continue;
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Found matching field: " + f.getName());
                Object v = f.get(root);
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Field " + f.getName() + " value: " + (v != null ? v.getClass().getSimpleName() : "null"));
                collectIngredientStacksDeep(v, out, seen);
            } catch (Throwable t) {
                System.out.println("[LS Tweaks][RV] collectIngredientStacksDeep: Error accessing field " + f.getName() + ": " + t.getMessage());
            }
        }
    }

    public static RecipeManager getClientRecipeManager() {
        // 0) If running integrated server (singleplayer), use the server's RecipeManager
        try {
            Object server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                for (java.lang.reflect.Method m : server.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                        Object rm = m.invoke(server);
                        if (rm instanceof RecipeManager r) return r;
                    }
                }
                // Try ServerResources path: server.getServerResources().getRecipeManager()
                try {
                    Object srvRes = server.getClass().getMethod("getServerResources").invoke(server);
                    if (srvRes != null) {
                        for (java.lang.reflect.Method m : srvRes.getClass().getMethods()) {
                            if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                                Object rm = m.invoke(srvRes);
                                if (rm instanceof RecipeManager r) return r;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        // 1) Try standard connection method/field
        try {
            Object conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                // direct method by common names
                String[] methodNames = {"getRecipeManager", "recipeManager"};
                for (String mn : methodNames) {
                    try {
                        java.lang.reflect.Method m = conn.getClass().getMethod(mn);
                        if (RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                            Object rm = m.invoke(conn);
                            if (rm instanceof RecipeManager r) return r;
                        }
                    } catch (Throwable ignored) {}
                }
                // scan for any zero-arg method returning RecipeManager
                try {
                    for (java.lang.reflect.Method m : conn.getClass().getMethods()) {
                        if (m.getParameterCount() != 0) continue;
                        if (RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                            Object rm = m.invoke(conn);
                            if (rm instanceof RecipeManager r) return r;
                        }
                    }
                } catch (Throwable ignored) {}
                // fields by name/type
                try {
                    for (java.lang.reflect.Field f : conn.getClass().getDeclaredFields()) {
                        f.setAccessible(true);
                        if (RecipeManager.class.isAssignableFrom(f.getType())) {
                            Object rm = f.get(conn);
                            if (rm instanceof RecipeManager r) return r;
                        }
                        String n = f.getName().toLowerCase();
                        if (n.contains("recipe") && !RecipeManager.class.isAssignableFrom(f.getType())) {
                            Object v = f.get(conn);
                            if (v != null && RecipeManager.class.isAssignableFrom(v.getClass())) return (RecipeManager)v;
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        // 2) Try gameMode connection path (REI pattern)
        try {
            Object gm = Minecraft.getInstance().gameMode;
            if (gm != null) {
                for (java.lang.reflect.Field f : gm.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object v = f.get(gm);
                    if (v == null) continue;
                    // scan this nested connection-like object
                    for (java.lang.reflect.Method m : v.getClass().getMethods()) {
                        if (m.getParameterCount() != 0) continue;
                        if (RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                            Object rm = m.invoke(v);
                            if (rm instanceof RecipeManager r) return r;
                        }
                    }
                    for (java.lang.reflect.Field cf : v.getClass().getDeclaredFields()) {
                        cf.setAccessible(true);
                        Object rv = cf.get(v);
                        if (rv instanceof RecipeManager r) return r;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 3) Try level method/field (older patterns)
        try {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                try {
                    java.lang.reflect.Method m = level.getClass().getMethod("getRecipeManager");
                    Object rm = m.invoke(level);
                    if (rm instanceof RecipeManager r) return r;
                } catch (Throwable ignored) {}
                try {
                    java.lang.reflect.Field f = level.getClass().getDeclaredField("recipeManager");
                    f.setAccessible(true);
                    Object rm = f.get(level);
                    if (rm instanceof RecipeManager r) return r;
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        System.out.println("[LS Tweaks][RV] ERROR: Could not locate client RecipeManager (connection=" + (Minecraft.getInstance().getConnection()==null?"null":Minecraft.getInstance().getConnection().getClass().getName()) + ")");
        // 4) Build a client-side RecipeManager by scanning resources (mirrors server loader)
        try {
            if (CLIENT_BUILT_MANAGER != null) return CLIENT_BUILT_MANAGER;
            var mc = Minecraft.getInstance();
            Object rm = mc.getResourceManager();
            RegistryAccess access = getClientRegistryAccess();
            if (rm != null && access != null) {
                // new RecipeManager(HolderLookup.Provider)
                RecipeManager built = new RecipeManager((net.minecraft.core.HolderLookup.Provider)access);
                // Call prepare+apply
                Object profiler = Class.forName("net.minecraft.util.profiling.InactiveProfiler").getField("INSTANCE").get(null);
                java.lang.reflect.Method prepare = RecipeManager.class.getDeclaredMethod("prepare", Class.forName("net.minecraft.server.packs.resources.ResourceManager"), Class.forName("net.minecraft.util.profiling.ProfilerFiller"));
                Object recipeMap = prepare.invoke(built, rm, profiler);
                java.lang.reflect.Method apply = RecipeManager.class.getDeclaredMethod("apply", Class.forName("net.minecraft.world.item.crafting.RecipeMap"), Class.forName("net.minecraft.server.packs.resources.ResourceManager"), Class.forName("net.minecraft.util.profiling.ProfilerFiller"));
                apply.invoke(built, recipeMap, rm, profiler);
                CLIENT_BUILT_MANAGER = built;
                System.out.println("[LS Tweaks][RV] Built client RecipeManager from resources");
                return CLIENT_BUILT_MANAGER;
            }
        } catch (Throwable t) {
            System.out.println("[LS Tweaks][RV] ERROR building client RecipeManager: " + t);
        }
        return null;
    }

    /** Obtain client RegistryAccess following JEI/REI patterns. */
    public static RegistryAccess getClientRegistryAccess() {
        try {
            if (Minecraft.getInstance().level != null) return Minecraft.getInstance().level.registryAccess();
        } catch (Throwable ignored) {}
        try {
            Object conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                try {
                    java.lang.reflect.Method m = conn.getClass().getMethod("registryAccess");
                    Object ra = m.invoke(conn);
                    if (ra instanceof RegistryAccess r) return r;
                } catch (Throwable ignored) {}
                for (java.lang.reflect.Method m : conn.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && RegistryAccess.class.isAssignableFrom(m.getReturnType())) {
                        Object ra = m.invoke(conn);
                        if (ra instanceof RegistryAccess r) return r;
                    }
                }
            }
        } catch (Throwable ignored) {}
        try {
            Object gm = Minecraft.getInstance().gameMode;
            if (gm != null) {
                for (java.lang.reflect.Field f : gm.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object v = f.get(gm);
                    if (v == null) continue;
                    for (java.lang.reflect.Method m : v.getClass().getMethods()) {
                        if (m.getParameterCount() == 0 && RegistryAccess.class.isAssignableFrom(m.getReturnType())) {
                            Object ra = m.invoke(v);
                            if (ra instanceof RegistryAccess r) return r;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** Fallback: read recipes directly from RegistryAccess (1.21.4). */
    public static java.util.List<Object> fetchRecipesFromRegistryAccess() {
        RegistryAccess access = getClientRegistryAccess();
        if (access == null) {
            System.out.println("[LS Tweaks][RV] WARN: RegistryAccess is null");
            return java.util.List.of();
        }
        try {
            // Grab Registries.RECIPE key
            Class<?> regs = Class.forName("net.minecraft.core.registries.Registries");
            java.lang.reflect.Field fRecipe = regs.getField("RECIPE");
            Object recipeKey = fRecipe.get(null); // ResourceKey

            // Try access.registryOrThrow(ResourceKey)
            try {
                java.lang.reflect.Method regOr = access.getClass().getMethod("registryOrThrow", recipeKey.getClass());
                Object registry = regOr.invoke(access, recipeKey);
                java.util.List<Object> values = collectRegistryValues(registry);
                if (!values.isEmpty()) return values;
            } catch (Throwable ignored) {}

            // Try access.registry(ResourceKey) -> Optional<Registry>
            try {
                java.lang.reflect.Method regM = access.getClass().getMethod("registry", recipeKey.getClass());
                Object opt = regM.invoke(access, recipeKey);
                if (opt instanceof java.util.Optional<?> o) {
                    Object registry = o.orElse(null);
                    if (registry != null) {
                        java.util.List<Object> values = collectRegistryValues(registry);
                        if (!values.isEmpty()) return values;
                    }
                }
            } catch (Throwable ignored) {}

            // Try access.lookupOrThrow(ResourceKey) -> RegistryLookup, then list elements
            try {
                java.lang.reflect.Method lookup = access.getClass().getMethod("lookupOrThrow", recipeKey.getClass());
                Object regLookup = lookup.invoke(access, recipeKey);
                // methods: listElements(), listEntries()
                for (String mn : new String[]{"listElements", "listEntries"}) {
                    try {
                        java.lang.reflect.Method lm = regLookup.getClass().getMethod(mn);
                        Object list = lm.invoke(regLookup);
                        if (list instanceof java.util.List<?> ls && !ls.isEmpty()) {
                            java.util.List<Object> out = new java.util.ArrayList<>();
                            for (Object holder : ls) {
                                try {
                                    java.lang.reflect.Method vm = holder.getClass().getMethod("value");
                                    Object v = vm.invoke(holder);
                                    if (v != null) out.add(v);
                                } catch (Throwable ignored) {}
                            }
                            return out;
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable t) {
            System.out.println("[LS Tweaks][RV] ERROR: fetchRecipesFromRegistryAccess failed: " + t);
        }
        return java.util.List.of();
    }

    // Removed JSON scan helper; replaced with building a RecipeManager via prepare/apply

    private static java.util.List<Object> collectRegistryValues(Object registry) {
        java.util.List<Object> out = new java.util.ArrayList<>();
        if (registry == null) return out;
        try {
            if (registry instanceof Iterable<?> it) {
                for (Object e : it) out.add(extractRegistryValue(e));
                return out;
            }
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method stream = registry.getClass().getMethod("stream");
            Object s = stream.invoke(registry);
            if (s instanceof java.util.stream.Stream<?> st) {
                st.forEach(e -> out.add(extractRegistryValue(e)));
                return out;
            }
        } catch (Throwable ignored) {}
        try {
            java.lang.reflect.Method valuesM = registry.getClass().getMethod("entrySet");
            Object set = valuesM.invoke(registry);
            if (set instanceof java.util.Set<?> es) {
                for (Object entry : es) {
                    try {
                        Object v = entry.getClass().getMethod("getValue").invoke(entry);
                        out.add(extractRegistryValue(v));
                    } catch (Throwable ignored) {}
                }
                return out;
            }
        } catch (Throwable ignored) {}
        return out;
    }

    private static Object extractRegistryValue(Object e) {
        if (e == null) return null;
        try {
            // Holder wrappers often provide value()
            java.lang.reflect.Method vm = e.getClass().getMethod("value");
            return vm.invoke(e);
        } catch (Throwable ignored) {}
        return e;
    }

    /** Locate the RecipeHolder for a given Recipe instance using the client's RecipeManager. */
    public static net.minecraft.world.item.crafting.RecipeHolder<?> findHolderForRecipe(Recipe<?> recipe) {
        try {
            RecipeManager mgr = getClientRecipeManager();
            if (mgr == null) return null;
            for (net.minecraft.world.item.crafting.RecipeHolder<?> h : mgr.getRecipes()) {
                if (h != null && h.value() == recipe) return h;
                if (h != null && recipe.equals(h.value())) return h;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Removed heavy deep traversal; previous version could stall the render thread.

    private RecipeUtil() {}
}


