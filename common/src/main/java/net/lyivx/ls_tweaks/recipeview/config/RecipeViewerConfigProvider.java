package net.lyivx.ls_tweaks.recipeview.config;

import com.google.gson.JsonObject;
import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.lyivx.ls_core.common.config.ILyivxConfigProvider;
import net.lyivx.ls_tweaks.LYIVXs_tweaks;

/**
 * Recipe Viewer provider for LYIVX's Core config system.
 * Separate category for recipe viewer settings.
 */
public class RecipeViewerConfigProvider implements ILyivxConfigProvider {
    public static final String PROVIDER_ID = LYIVXs_tweaks.MOD_ID + "_recipe_viewer";
    private static CustomConfigSpec configSpecInstance = null;

    public RecipeViewerConfigProvider() {
        // Required empty constructor for ServiceLoader
    }

    /** Public accessor so the rest of the mod can read config directly. */
    public static CustomConfigSpec getConfigSpec() {
        if (configSpecInstance == null) {
            LYIVXs_tweaks.LOGGER.error("Recipe Viewer ConfigProvider accessed before configSpec was registered!");
            return null;
        }
        return configSpecInstance;
    }

    /* ========= Recipe Viewer UI ========= */
    public enum LayoutPos { TOP, BOTTOM }
    public enum FillMode { FULL_SIDE, CONTAINER_SIZE }

    public static boolean enabled() { var s = getConfigSpec(); return s != null && s.getBoolean("enabled", true); }
    public static LayoutPos buttonsPos() { var s = getConfigSpec(); return s != null ? s.getEnum("buttons_pos", LayoutPos.BOTTOM, LayoutPos.class) : LayoutPos.BOTTOM; }
    public static LayoutPos searchBarPos() { var s = getConfigSpec(); return s != null ? s.getEnum("search_bar_pos", LayoutPos.BOTTOM, LayoutPos.class) : LayoutPos.BOTTOM; }
    public static FillMode fillMode() { var s = getConfigSpec(); return s != null ? s.getEnum("fill_mode", FillMode.CONTAINER_SIZE, FillMode.class) : FillMode.CONTAINER_SIZE; }
    public static int fixedColumns() { var s = getConfigSpec(); return s != null ? s.getInt("fixed_columns", 4) : 4; }
    public static int fixedRows() { var s = getConfigSpec(); return s != null ? s.getInt("fixed_rows", 7) : 7; }
    public static boolean showRecipeNames() { var s = getConfigSpec(); return s != null && s.getBoolean("show_recipe_names", false); }
    public static boolean showItemPanelBackground() { var s = getConfigSpec(); return s != null && s.getBoolean("show_item_panel_background", false); }
    public static boolean showSlotBackgrounds() { var s = getConfigSpec(); return s != null && s.getBoolean("show_slot_backgrounds", true); }
    /** If true, tooltips show the mod's display name; if false, show the mod id/namespace. */
    public static boolean tooltipPreferModName() { var s = getConfigSpec(); return s != null ? s.getBoolean("tooltip_prefer_mod_name", true) : true; }

    // --------------------------------------------------------------------------------------------
    // ILyivxConfigProvider
    // --------------------------------------------------------------------------------------------

    @Override public String getModId() { return PROVIDER_ID; }
    @Override public String getModName() { return "Recipe Viewer"; }
    @Override public String getConfigCategoryName() { return "Recipe Viewer"; }

    @Override
    public JsonObject getDefaultConfig() {
        JsonObject o = new JsonObject();

        // Recipe Viewer UI
        o.addProperty("enabled", true);
        o.addProperty("buttons_pos", LayoutPos.BOTTOM.name());
        o.addProperty("search_bar_pos", LayoutPos.BOTTOM.name());
        o.addProperty("fill_mode", FillMode.CONTAINER_SIZE.name());
        o.addProperty("fixed_columns", 4);
        o.addProperty("fixed_rows", 7);
        o.addProperty("show_recipe_names", false);
        o.addProperty("show_item_panel_background", false);
        o.addProperty("show_slot_backgrounds", true);
        o.addProperty("tooltip_prefer_mod_name", true);

        return o;
    }

    @Override
    public void registerConfigSpec(CustomConfigSpec spec) {
        RecipeViewerConfigProvider.configSpecInstance = spec;
    }

    @Override
    public void addConfigOptions(ModConfigScreen screen, CustomOptionsList list) {
        if (screen != null && list != null) {
            RecipeViewerClientConfigHelper.addConfigOptions(this, screen, list);
        }
    }

    @Override
    public void resetConfigDefaults() {
        CustomConfigSpec spec = RecipeViewerConfigProvider.configSpecInstance;
        if (spec != null) {
            JsonObject defaults = getDefaultConfig();
            defaults.entrySet().forEach(entry -> {
                spec.set(entry.getKey(), entry.getValue());
            });
        }
    }
}
