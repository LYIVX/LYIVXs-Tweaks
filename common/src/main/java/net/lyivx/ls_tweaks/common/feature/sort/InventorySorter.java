package net.lyivx.ls_tweaks.common.feature.sort;

import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class InventorySorter {

    public static List<ItemStack> sort(List<ItemStack> input, QolNet.SortC2SPayload.Mode mode, boolean mergeBeforeSort, boolean unstackablesLast) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack st : input) if (!st.isEmpty()) stacks.add(st.copy());

        if (mergeBeforeSort) stacks = mergeStacks(stacks);

        Comparator<ItemStack> cmp = switch (mode) {
            case DEFAULT -> defaultComparator(unstackablesLast);
            case ALPHA_ASC -> alphaComparator(true);
            case ALPHA_DESC -> alphaComparator(false);
        };
        stacks.sort(cmp);

        List<ItemStack> out = new ArrayList<>(input.size());
        int i = 0;
        while (i < stacks.size() && i < input.size()) out.add(stacks.get(i++));
        while (out.size() < input.size()) out.add(ItemStack.EMPTY);
        return out;
    }

    private static Comparator<ItemStack> defaultComparator(boolean unstackablesLast) {
        return (a, b) -> {
            int ca = CategoryResolver.categoryPriority(a);
            int cb = CategoryResolver.categoryPriority(b);
            if (ca != cb) return Integer.compare(ca, cb);

            if (unstackablesLast) {
                boolean ua = a.getMaxStackSize() == 1;
                boolean ub = b.getMaxStackSize() == 1;
                if (ua != ub) return ua ? 1 : -1;
            }

            var ida = BuiltInRegistries.ITEM.getKey(a.getItem());
            var idb = BuiltInRegistries.ITEM.getKey(b.getItem());
            int cmpId = ida.compareTo(idb);
            if (cmpId != 0) return cmpId;

            int ma = a.getMaxDamage(), mb = b.getMaxDamage();
            if (ma > 0 || mb > 0) {
                float ra = ma == 0 ? 0 : (float) a.getDamageValue() / (float) ma;
                float rb = mb == 0 ? 0 : (float) b.getDamageValue() / (float) mb;
                int cmpR = Float.compare(ra, rb);
                if (cmpR != 0) return cmpR;
            }

            return Integer.compare(b.getCount(), a.getCount());
        };
    }

    private static Comparator<ItemStack> alphaComparator(boolean asc) {
        return (a, b) -> {
            String na = nameFor(a);
            String nb = nameFor(b);
            int cmp = na.compareToIgnoreCase(nb);
            if (!asc) cmp = -cmp;
            if (cmp != 0) return cmp;

            // tie: fall back to registry id for stability
            var ida = BuiltInRegistries.ITEM.getKey(a.getItem());
            var idb = BuiltInRegistries.ITEM.getKey(b.getItem());
            cmp = ida.compareTo(idb);
            if (!asc) cmp = -cmp;
            if (cmp != 0) return cmp;

            // final tie: larger stacks first to reduce clutter
            return Integer.compare(b.getCount(), a.getCount());
        };
    }

    private static String nameFor(ItemStack st) {
        // Use the hover/display name from server’s language; fallback to registry id if empty.
        String s = st.getHoverName().getString();
        if (s == null || s.isBlank()) s = BuiltInRegistries.ITEM.getKey(st.getItem()).toString();
        return s;
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> in) {
        Map<StackKey, Integer> counts = new LinkedHashMap<>();
        for (ItemStack st : in) counts.merge(StackKey.of(st), st.getCount(), Integer::sum);
        List<ItemStack> out = new ArrayList<>();
        for (var e : counts.entrySet()) {
            var proto = e.getKey().prototype;
            int total = e.getValue();
            int max = proto.getMaxStackSize();
            while (total > 0) {
                int n = Math.min(total, max);
                ItemStack part = proto.copy();
                part.setCount(n);
                out.add(part);
                total -= n;
            }
        }
        return out;
    }

    private record StackKey(ItemStack prototype) {
        static StackKey of(ItemStack st) { return new StackKey(st.copyWithCount(1)); }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StackKey sk)) return false;
            return ItemStack.isSameItemSameComponents(prototype, sk.prototype);
        }
        @Override public int hashCode() {
            return 31 * BuiltInRegistries.ITEM.getKey(prototype.getItem()).hashCode()
                    + prototype.getComponents().hashCode();
        }
    }
}