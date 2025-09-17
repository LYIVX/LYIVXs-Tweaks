package net.lyivx.ls_tweaks.common.feature.sort;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

public final class CategoryResolver {
    // Optional helpful tags (if present)
    private static final TagKey<Item> LOGS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));
    private static final TagKey<Item> PLANKS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "planks"));
    private static final TagKey<Item> WOOL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "wool"));
    private static final TagKey<Item> STONE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "stone_crafting_materials"));
    private static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "ores"));
    private static final TagKey<Item> RAILS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "rails"));

    /**
     * Lower numbers sort earlier.
     * 0 Blocks (logs/planks/stone first), 1 Redstone-ish (rails placeholder), 2 Food, 3 Tools/Weapons/Armor,
     * 4 Misc
     */
    public static int categoryPriority(ItemStack st) {
        Item it = st.getItem();

        // 0) Blocks first (then sub-buckets by common tags)
        if (it instanceof BlockItem bi) {
            if (st.is(LOGS)) return 0;
            if (st.is(PLANKS)) return 1;
            if (st.is(WOOL)) return 2;
            if (st.is(STONE)) return 3;
            return 4; // other blocks
        }

        // 1) "Redstone-y" odds & ends (placeholder with rails as example)
        if (st.is(RAILS)) return 10;

        // 2) Food
        if (st.has(DataComponents.FOOD)) return 20;

        // 3) Tools/Weapons/Armor (damageable or known classes)
        if (st.getMaxDamage() > 0
                || it instanceof SwordItem
                || it instanceof DiggerItem
                || it instanceof BowItem
                || it instanceof CrossbowItem
                || it instanceof TridentItem
                || it instanceof ShieldItem
                || it instanceof ArmorItem
        ) {
            return 30;
        }

        // 4) Everything else
        return 40;
    }
}