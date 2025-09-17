package net.lyivx.ls_tweaks.common.config;

import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.minecraft.client.OptionInstance;

/**
 * Client-only UI bindings for LYIVX's Core config screen.
 * Minimal booleans bound directly to the live CustomConfigSpec.
 */
public final class ClientConfigHelper {
    private ClientConfigHelper() {}

    public static void addConfigOptions(ConfigProvider provider, ModConfigScreen screen, CustomOptionsList list) {
        final CustomConfigSpec spec = ConfigProvider.getConfigSpec();
        if (spec == null) return; // not registered yet

        final String key = "config." + ConfigProvider.PROVIDER_ID + ".";

        // ----- Refill -----
        var refillEnabled = OptionInstance.createBoolean(
                key + "refill_enabled",
                spec.getBoolean("refill_enabled", true),
                v -> spec.setBoolean("refill_enabled", v)
        );
        var refillStrict = OptionInstance.createBoolean(
                key + "refill_strictNbt",
                spec.getBoolean("refill_strictNbt", true),
                v -> spec.setBoolean("refill_strictNbt", v)
        );
        var refillFood = OptionInstance.createBoolean(
                key + "refill_includeFood",
                spec.getBoolean("refill_includeFood", false),
                v -> spec.setBoolean("refill_includeFood", v)
        );
        var refillProj = OptionInstance.createBoolean(
                key + "refill_includeProjectiles",
                spec.getBoolean("refill_includeProjectiles", true),
                v -> spec.setBoolean("refill_includeProjectiles", v)
        );
        var refillContainers = OptionInstance.createBoolean(
                key + "refill_workOnContainerScreens",
                spec.getBoolean("refill_workOnContainerScreens", false),
                v -> spec.setBoolean("refill_workOnContainerScreens", v)
        );

        list.addSmall(refillEnabled, refillStrict);
        list.addSmall(refillFood, refillProj);
        list.addSmall(refillContainers);

        // ----- Sorting -----
        var sortEnabled = OptionInstance.createBoolean(
                key + "sorting_enabled",
                spec.getBoolean("sorting_enabled", true),
                v -> spec.setBoolean("sorting_enabled", v)
        );
        var sortMerge = OptionInstance.createBoolean(
                key + "sorting_mergeBeforeSort",
                spec.getBoolean("sorting_mergeBeforeSort", true),
                v -> spec.setBoolean("sorting_mergeBeforeSort", v)
        );
        var sortUnstackLast = OptionInstance.createBoolean(
                key + "sorting_unstackablesLast",
                spec.getBoolean("sorting_unstackablesLast", true),
                v -> spec.setBoolean("sorting_unstackablesLast", v)
        );
        var sortAllowContainers = OptionInstance.createBoolean(
                key + "sorting_allowContainerSorting",
                spec.getBoolean("sorting_allowContainerSorting", true),
                v -> spec.setBoolean("sorting_allowContainerSorting", v)
        );

        list.addSmall(sortEnabled, sortMerge);
        list.addSmall(sortUnstackLast, sortAllowContainers);

        // If/when you add an int or enum widget in Core, slot it here
        // e.g., ui_button_y_offset or default_sort_mode.
    }
}