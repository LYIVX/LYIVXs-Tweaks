package net.lyivx.ls_tweaks.common.config;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class QolConfig {

    /* ---------------- Refill ---------------- */

    public static final class Refill {
        public final boolean enabled;
        public final boolean strictNbt;
        public final boolean includeFood;
        public final boolean includeProjectiles;
        public final boolean workOnContainerScreens;

        public Refill(boolean enabled, boolean strictNbt, boolean includeFood, boolean includeProjectiles, boolean workOnContainerScreens) {
            this.enabled = enabled;
            this.strictNbt = strictNbt;
            this.includeFood = includeFood;
            this.includeProjectiles = includeProjectiles;
            this.workOnContainerScreens = workOnContainerScreens;
        }
    }

    private static Refill REFILL;

    /* ---------------- Sorting ---------------- */

    public static final class Sorting {
        public final boolean enabled;
        public final boolean mergeBeforeSort;
        public final boolean unstackablesLast;
        public final boolean allowContainerSorting;

        public final Set<String> allowedMenuTypeIds;
        public final Set<String> allowedScreenClasses;

        public Sorting(boolean enabled, boolean mergeBeforeSort, boolean unstackablesLast, boolean allowContainerSorting,
                       Set<String> allowedMenuTypeIds, Set<String> allowedScreenClasses) {
            this.enabled = enabled;
            this.mergeBeforeSort = mergeBeforeSort;
            this.unstackablesLast = unstackablesLast;
            this.allowContainerSorting = allowContainerSorting;
            this.allowedMenuTypeIds = allowedMenuTypeIds;
            this.allowedScreenClasses = allowedScreenClasses;
        }
    }
    private static Sorting SORTING;

    /* ---------------- Lifecycle ---------------- */

    public static void bootstrap() {
        REFILL = new Refill(
                true,   // enabled
                true,   // strictNbt
                false,  // includeFood
                true,   // includeProjectiles
                false   // workOnContainerScreens
        );

        SORTING = new Sorting(
                true,   // enabled
                true,   // mergeBeforeSort
                true,   // unstackablesLast
                true,   // allowContainerSorting
                new HashSet<>(List.of(
                        "minecraft:generic_9x1",
                        "minecraft:generic_9x2",
                        "minecraft:generic_9x3",   // barrel/small chests
                        "minecraft:generic_9x4",
                        "minecraft:generic_9x5",
                        "minecraft:generic_9x6",   // double chests
                        "minecraft:generic_3x3",   // dispenser/dropper
                        "minecraft:hopper",
                        "minecraft:shulker_box"
                )),
                new HashSet<>()
        );
    }

    public static Refill refill() { return Objects.requireNonNull(REFILL); }
    public static Sorting sorting() { return Objects.requireNonNull(SORTING); }

    public static void setRefill(Refill r) { REFILL = r; }
    public static void setSorting(Sorting s) { SORTING = s; }
}