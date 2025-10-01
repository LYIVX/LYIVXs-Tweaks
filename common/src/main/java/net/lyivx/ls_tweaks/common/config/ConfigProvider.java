package net.lyivx.ls_tweaks.common.config;

import com.google.gson.JsonObject;
import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.lyivx.ls_core.common.config.ILyivxConfigProvider;
import net.lyivx.ls_tweaks.LYIVXs_tweaks;

/**
 * LS Tweaks provider for LYIVX's Core config system.
 * Discovered via META-INF/services.
 */
public class ConfigProvider implements ILyivxConfigProvider {
    public static final String PROVIDER_ID = LYIVXs_tweaks.MOD_ID;
    private static CustomConfigSpec configSpecInstance = null;

    public ConfigProvider() {
        // Required empty constructor for ServiceLoader
    }

    /** Public accessor so the rest of the mod can read config directly. */
    public static CustomConfigSpec getConfigSpec() {
        if (configSpecInstance == null) {
            LYIVXs_tweaks.LOGGER.error("Furnection ConfigProvider accessed before configSpec was registered!");
            // Return null and handle it in getters, or implement a dummy spec
            return null;
        }
        return configSpecInstance;
    }

    /* ========= small enums ========= */
    public enum SortMode {
        CATEGORY("Category"),
        A_TO_Z("A To Z"),
        Z_TO_A("Z To A");

        private final String displayName;

        SortMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            // Use the display name for user-facing strings if needed,
            // but use .name() for saving/loading the enum constant name.
            return this.displayName;
        }

        // Optional: Add a method to get by name safely, defaulting if needed
        public static SortMode safeValueOf(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException | NullPointerException e) {
                return CATEGORY; // Default value
            }
        }
    }


    // KeyOption moved into KeybindButton; keep methods but switch to that enum
    /* ========= Refill helpers ========= */
    public static boolean refillEnabled()                { var s = getConfigSpec(); return s != null && s.getBoolean("refill_enabled", true); }
    public static boolean refillStrictNbt()              { var s = getConfigSpec(); return s != null && s.getBoolean("refill_strictNbt", true); }
    

    /* ========= Sorting helpers ========= */
    public static boolean sortingEnabled()               { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_enabled", true); }
    public static boolean uiShowSortingButtons()         { var s = getConfigSpec(); return s != null && s.getBoolean("ui_show_sorting_buttons", true); }
    public static boolean sortingMinimizedDisplay()         { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_minimizedDisplay", false); }
    /** Default action the main sort button/key uses when no explicit mode is chosen. */
    public static SortMode defaultSortMode() {
        var s = getConfigSpec();
        return s != null ? s.getEnum("sorting_default_sort_mode", SortMode.CATEGORY, SortMode.class) : SortMode.CATEGORY;
    }
    public static boolean sortingMergeBeforeSort()       { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_mergeBeforeSort", true); }
    public static boolean sortingUnstackablesLast()      { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_unstackablesLast", true); }
    public static boolean sortingAllowContainerSorting() { var s = getConfigSpec(); return s != null && s.getBoolean("sorting_allowContainerSorting", true); }

    /* ========= UI / UX helpers ========= */
    public static boolean uiShowTooltips()       { var s = getConfigSpec(); return s != null && s.getBoolean("ui_show_tooltips", true); }
    public static int     uiButtonYOffset()      { var s = getConfigSpec(); return s != null ? s.getInt("ui_button_y_offset", 0) : 0; }

    /* ========= Keybind helpers ========= */
    public static int keybindSortKey() {
        var s = getConfigSpec();
        if (s == null) return org.lwjgl.glfw.GLFW.GLFW_KEY_R;
        KeybindButton.KeyOption opt = s.getEnum("keybind_sort_key", KeybindButton.KeyOption.R, KeybindButton.KeyOption.class);
        return opt != null ? opt.code() : org.lwjgl.glfw.GLFW.GLFW_KEY_R;
    }
    public static int keybindOpenConfigKey() {
        var s = getConfigSpec();
        if (s == null) return org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
        KeybindButton.KeyOption opt = s.getEnum("keybind_open_config_key", KeybindButton.KeyOption.UNKNOWN, KeybindButton.KeyOption.class);
        return opt != null ? opt.code() : org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
    }

    // === Quick Stack helpers ===
    public static boolean quickStackEnabled() { var s = getConfigSpec(); return s != null && s.getBoolean("quickstack_enabled", true); }
    public static boolean quickStackAllowContainerActions() { var s = getConfigSpec(); return s != null && s.getBoolean("quickstack_allowContainerActions", true); }
    public static boolean uiShowQuickStackButtons() { var s = getConfigSpec(); return s != null && s.getBoolean("ui_show_quickstack_buttons", true); }
    
    /* ========= Slot Lock helpers ========= */
    public static boolean slotLockEnabled() { var s = getConfigSpec(); return s != null && s.getBoolean("slotlock_enabled", true); }
    public static boolean slotLockShowIcons() { var s = getConfigSpec(); return s != null && s.getBoolean("slotlock_showIcons", true); }
    public static boolean slotLockStopQuickMoves() { var s = getConfigSpec(); return s != null && s.getBoolean("slotlock_stopQuickMoves", false); }
    // Quick-move drag upgrade
    public static boolean quickMoveDragEnabled() { var s = getConfigSpec(); return s != null && s.getBoolean("quickmove_drag_enabled", true); }

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
        

        // Sorting
        o.addProperty("sorting_enabled", true);
        o.addProperty("sorting_mergeBeforeSort", true);
        o.addProperty("sorting_unstackablesLast", true);
        o.addProperty("sorting_allowContainerSorting", true);
        o.addProperty("sorting_minimizedDisplay", false);
        o.addProperty("sorting_default_sort_mode", SortMode.CATEGORY.name());

        // UI/UX
        o.addProperty("ui_show_sorting_buttons", true);
        o.addProperty("ui_show_tooltips", true);
        o.addProperty("ui_button_y_offset", 0);

        // Quick Stack
        o.addProperty("quickstack_enabled", true);
        o.addProperty("quickstack_allowContainerActions", true);
        o.addProperty("ui_show_quickstack_buttons", true);
        
        // Slot Lock
        o.addProperty("slotlock_enabled", true);
        o.addProperty("slotlock_showIcons", true);
        o.addProperty("slotlock_stopQuickMoves", false);
        // Quick-Move drag feature (Shift-drag to quick-move multiple)
        o.addProperty("quickmove_drag_enabled", true);

        // Keybinds (configurable keys)
        o.addProperty("keybind_sort_key", KeybindButton.KeyOption.R.name());
        o.addProperty("keybind_open_config_key", KeybindButton.KeyOption.UNKNOWN.name());


        return o;
    }

    @Override
    public void registerConfigSpec(CustomConfigSpec spec) {
        ConfigProvider.configSpecInstance = spec;
    }

    @Override
    public void addConfigOptions(ModConfigScreen screen, CustomOptionsList list) {
        if (screen != null && list != null) {
            ClientConfigHelper.addConfigOptions(this, screen, list);
        }
    }

    @Override
    public void resetConfigDefaults() {
        CustomConfigSpec spec = ConfigProvider.configSpecInstance;
        if (spec != null) {
            JsonObject defaults = getDefaultConfig();
            defaults.entrySet().forEach(entry -> {
                spec.set(entry.getKey(), entry.getValue());
            });
        }
    }
}