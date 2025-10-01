package net.lyivx.ls_tweaks.recipeview.config;

import com.mojang.serialization.Codec;
import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

/**
 * Client-only UI bindings for Recipe Viewer config screen.
 */
public final class RecipeViewerClientConfigHelper {
    public static void addConfigOptions(RecipeViewerConfigProvider provider, ModConfigScreen screen, CustomOptionsList list) {
        final CustomConfigSpec spec = RecipeViewerConfigProvider.getConfigSpec();
        if (spec == null) return; // not registered yet

        String modId = provider.getModId();

        // ===== RECIPE VIEWER =====
        list.addTitle(Component.translatable("config.recipe_viewer.title.main"));

        OptionInstance<Boolean> enabled = OptionInstance.createBoolean(
                "config." + modId + ".enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".enabled.tooltip")),
                spec.getBoolean("enabled", true),
                v -> spec.setBoolean("enabled", v)
        );
        OptionInstance<Boolean> showRecipeNames = OptionInstance.createBoolean(
                "config." + modId + ".show_recipe_names",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".show_recipe_names.tooltip")),
                spec.getBoolean("show_recipe_names", false),
                v -> spec.setBoolean("show_recipe_names", v)
        );
        OptionInstance<Boolean> showItemPanelBackground = OptionInstance.createBoolean(
                "config." + modId + ".show_item_panel_background",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".show_item_panel_background.tooltip")),
                spec.getBoolean("show_item_panel_background", false),
                v -> spec.setBoolean("show_item_panel_background", v)
        );
        OptionInstance<Boolean> showSlotBackgrounds = OptionInstance.createBoolean(
                "config." + modId + ".show_slot_backgrounds",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".show_slot_backgrounds.tooltip")),
                spec.getBoolean("show_slot_backgrounds", true),
                v -> spec.setBoolean("show_slot_backgrounds", v)
        );
        OptionInstance<Boolean> tooltipPreferModName = OptionInstance.createBoolean(
                "config." + modId + ".tooltip_prefer_mod_name",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".tooltip_prefer_mod_name.tooltip")),
                spec.getBoolean("tooltip_prefer_mod_name", true),
                v -> spec.setBoolean("tooltip_prefer_mod_name", v)
        );

        OptionInstance<RecipeViewerConfigProvider.LayoutPos> buttonsPos = new OptionInstance<>(
                "config." + modId + ".buttons_pos",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".buttons_pos.tooltip")),
                (component, value) -> Component.translatable("config." + modId + ".buttons_pos." + value.name().toLowerCase()),
                new OptionInstance.Enum<>(
                        Arrays.asList(RecipeViewerConfigProvider.LayoutPos.values()),
                        Codec.STRING.xmap(
                                RecipeViewerConfigProvider.LayoutPos::valueOf,
                                Enum::name
                        )
                ),
                spec.getEnum("buttons_pos", RecipeViewerConfigProvider.LayoutPos.BOTTOM, RecipeViewerConfigProvider.LayoutPos.class),
                (value) -> spec.setEnum("buttons_pos", value)
        );

        OptionInstance<RecipeViewerConfigProvider.LayoutPos> searchPos = new OptionInstance<>(
                "config." + modId + ".search_bar_pos",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".search_bar_pos.tooltip")),
                (component, value) -> Component.translatable("config." + modId + ".search_bar_pos." + value.name().toLowerCase()),
                new OptionInstance.Enum<>(
                        Arrays.asList(RecipeViewerConfigProvider.LayoutPos.values()),
                        Codec.STRING.xmap(
                                RecipeViewerConfigProvider.LayoutPos::valueOf,
                                Enum::name
                        )
                ),
                spec.getEnum("search_bar_pos", RecipeViewerConfigProvider.LayoutPos.BOTTOM, RecipeViewerConfigProvider.LayoutPos.class),
                (value) -> spec.setEnum("search_bar_pos", value)
        );

        OptionInstance<RecipeViewerConfigProvider.FillMode> fillMode = new OptionInstance<>(
                "config." + modId + ".fill_mode",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".fill_mode.tooltip")),
                (component, value) -> Component.translatable("config." + modId + ".fill_mode." + value.name().toLowerCase()),
                new OptionInstance.Enum<>(
                        Arrays.asList(RecipeViewerConfigProvider.FillMode.values()),
                        Codec.STRING.xmap(
                                RecipeViewerConfigProvider.FillMode::valueOf,
                                Enum::name
                        )
                ),
                spec.getEnum("fill_mode", RecipeViewerConfigProvider.FillMode.CONTAINER_SIZE, RecipeViewerConfigProvider.FillMode.class),
                (value) -> spec.setEnum("fill_mode", value)
        );

        list.addBig(enabled);
        list.addSmall(showRecipeNames, showItemPanelBackground, showSlotBackgrounds, tooltipPreferModName, buttonsPos, searchPos, fillMode);
    }

    public static void openConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(new ModConfigScreen(mc.screen, mc.options, RecipeViewerConfigProvider.PROVIDER_ID));
    }

    private RecipeViewerClientConfigHelper() {}
}
