package net.lyivx.ls_tweaks.common.config;

import com.google.gson.JsonObject;
import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.lyivx.ls_core.common.config.ILyivxConfigProvider;
import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.minecraft.client.OptionInstance;

/**
 * LS Tweaks provider for LYIVX's Core config system.
 * Discovered via META-INF/services.
 */
public class ConfigProvider implements ILyivxConfigProvider {
    public static final String PROVIDER_ID = LYIVXs_tweaks.MOD_ID;

    // single source of truth
    private static CustomConfigSpec SPEC = null;

    /** Public accessor so the rest of the mod can read config directly. */
    public static CustomConfigSpec getConfigSpec() { return SPEC; }

    /* ========= small enums ========= */
    public enum SortMode { CATEGORY, A_TO_Z, Z_TO_A }

    /* ========= Refill helpers ========= */
    public static boolean refillEnabled()                { var s = getConfigSpec(); return s != null && s.getBoolean("refill_enabled", true); }
    public static boolean refillStrictNbt()              { var s = getConfigSpec(); return s != null && s.getBoolean("refill_strictNbt", true); }
    public static boolean refillIncludeFood()            { var s = getConfigSpec(); return s != null && s.getBoolean("refill_includeFood", false); }
    public static boolean refillIncludeProjectiles()     { var s = getConfigSpec(); return s != null && s.getBoolean("refill_includeProjectiles", true); }
    public static boolean refillWorkOnContainerScreens() { var s = getConfigSpec(); return s != null && s.getBoolean("refill_workOnContainerScreens", false); }

    /* ========= Sorting helpers ========= */
    public static boolean sortingEnabled()               { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_enabled", true); }
    public static boolean sortingMergeBeforeSort()       { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_mergeBeforeSort", true); }
    public static boolean sortingUnstackablesLast()      { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_unstackablesLast", true); }
    public static boolean sortingAllowContainerSorting() { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_allowContainerSorting", true); }

    /** Default action the main sort button/key uses when no explicit mode is chosen. */
    public static SortMode defaultSortMode() {
        var s = getConfigSpec();
        return s != null ? s.getEnum("default_sort_mode", SortMode.CATEGORY, SortMode.class) : SortMode.CATEGORY;
    }

    /* ========= UI / UX helpers ========= */
    public static boolean uiShowSortingButtons() { var s = getConfigSpec(); return s != null && s.getBoolean("ui_show_sort_buttons", true); }
    public static boolean uiShowTooltips()       { var s = getConfigSpec(); return s != null && s.getBoolean("ui_show_tooltips", true); }
    public static int     uiButtonYOffset()      { var s = getConfigSpec(); return s != null ? s.getInt("ui_button_y_offset", 0) : 0; }

    // --------------------------------------------------------------------------------------------
    // ILyivxConfigProvider
    // --------------------------------------------------------------------------------------------

    @Override public String getModId() { return PROVIDER_ID; }
    @Override public String getModName() { return "LYIVX's Tweaks"; }
    @Override public String getConfigCategoryName() { return "LYIVX's Tweaks"; }

    @Override
    public JsonObject getDefaultConfig() {
        JsonObject o = new JsonObject();

        // Refill
        o.addProperty("refill_enabled", true);
        o.addProperty("refill_strictNbt", true);
        o.addProperty("refill_includeFood", false);
        o.addProperty("refill_includeProjectiles", true);
        o.addProperty("refill_workOnContainerScreens", false);

        // Sorting
        o.addProperty("sorting_enabled", true);
        o.addProperty("sorting_mergeBeforeSort", true);
        o.addProperty("sorting_unstackablesLast", true);
        o.addProperty("sorting_allowContainerSorting", true);
        o.addProperty("default_sort_mode", SortMode.CATEGORY.name());

        // UI/UX
        o.addProperty("ui_show_sort_buttons", true);
        o.addProperty("ui_show_tooltips", true);
        o.addProperty("ui_button_y_offset", 0);

        return o;
    }

    @Override
    public void registerConfigSpec(CustomConfigSpec customConfigSpec) {
        SPEC = customConfigSpec;
        // seed defaults on first run
        JsonObject d = getDefaultConfig();
        d.entrySet().forEach(e -> SPEC.set(e.getKey(), e.getValue()));
    }

    @Override
    public void addConfigOptions(ModConfigScreen screen, CustomOptionsList list) {
        if (screen != null && list != null) {
            ClientConfigHelper.addConfigOptions(this, screen, list);
        }
    }

    @Override
    public void resetConfigDefaults() {
        if (SPEC == null) {
            LYIVXs_tweaks.LOGGER.warn("ConfigProvider.resetConfigDefaults called before SPEC was registered.");
            return;
        }
        JsonObject d = getDefaultConfig();
        d.entrySet().forEach(e -> SPEC.set(e.getKey(), e.getValue()));
    }
}