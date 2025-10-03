package net.lyivx.ls_tweaks.recipes.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Lightweight helpers for item tags (no dependency on heavy RecipeUtil). */
public final class TagsUtil {
    public static List<ResourceLocation> itemTagIds(ItemStack stack) {
        List<ResourceLocation> out = new ArrayList<>();
        if (stack == null || stack.isEmpty()) return out;
        try {
            Holder<Item> holder = stack.getItemHolder();
            if (holder == null) return out;
            out.addAll(holder.tags().map(k -> k.location()).toList());
        } catch (Throwable ignored) {}
        return out;
    }

    public static List<ItemStack> itemsInTag(ResourceLocation tagId) {
        List<ItemStack> out = new ArrayList<>();
        if (tagId == null) return out;
        try {
            TagKey<Item> key = TagKey.create(Registries.ITEM, tagId);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null) continue;
                Holder<Item> h = item.builtInRegistryHolder();
                if (h != null && h.is(key)) out.add(new ItemStack(item));
            }
        } catch (Throwable ignored) {}
        return out;
    }

    public static List<ResourceLocation> allItemTagIds() {
        Set<ResourceLocation> set = new HashSet<>();
        try {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == null) continue;
                Holder<Item> h = item.builtInRegistryHolder();
                if (h == null) continue;
                set.addAll(h.tags().map(k -> k.location()).toList());
            }
        } catch (Throwable ignored) {}
        List<ResourceLocation> out = new ArrayList<>(set);
        out.sort(java.util.Comparator.comparing(ResourceLocation::toString));
        return out;
    }

    private TagsUtil() {}
}


