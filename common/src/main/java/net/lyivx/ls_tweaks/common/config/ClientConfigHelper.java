package net.lyivx.ls_tweaks.common.config;

import com.mojang.serialization.Codec;
import net.lyivx.ls_core.client.screens.ModConfigScreen;
import net.lyivx.ls_core.client.screens.widgets.CustomOptionsList;
import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

/**
 * Client-only UI bindings for LYIVX's Core config screen.
 * Minimal booleans bound directly to the live CustomConfigSpec.
 */
public final class ClientConfigHelper {
    public static void addConfigOptions(ConfigProvider provider, ModConfigScreen screen, CustomOptionsList list) {
        final CustomConfigSpec spec = ConfigProvider.getConfigSpec();
        if (spec == null) return; // not registered yet

        String modId = provider.getModId();

        // ===== SORTING =====
        list.addTitle(Component.translatable("config.ls_tweaks.title.sorting"));

        OptionInstance<Boolean> sortingEnabled = OptionInstance.createBoolean(
                "config." + modId + ".sorting_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".sorting_enabled.tooltip")),
                spec.getBoolean("sorting_enabled", true),
                v -> spec.setBoolean("sorting_enabled", v)
        );
        OptionInstance<Boolean> showSortingBtns = OptionInstance.createBoolean(
                "config." + modId + ".ui_show_sorting_buttons",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".ui_show_sorting_buttons.tooltip")),
                spec.getBoolean("ui_show_sorting_buttons", true),
                v -> spec.setBoolean("ui_show_sorting_buttons", v)
        );
        OptionInstance<Boolean> sortingMinimizedDisplay = OptionInstance.createBoolean(
                "config." + modId + ".sorting_minimizedDisplay",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".sorting_minimizedDisplay.tooltip")),
                spec.getBoolean("sorting_minimizedDisplay", false),
                v -> spec.setBoolean("sorting_minimizedDisplay", v)
        );

        OptionInstance<ConfigProvider.SortMode> defaultSortMode = new OptionInstance<>(
                "config." + modId + ".sorting_default_sort_mode",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".sorting_default_sort_mode.tooltip")),
                (component, value) -> Component.translatable("config." + modId + ".sorting_default_sort_mode." + value.name().toLowerCase()),
                new OptionInstance.Enum<>(
                        Arrays.asList(ConfigProvider.SortMode.values()),
                        Codec.STRING.xmap(
                                ConfigProvider.SortMode::safeValueOf,
                                Enum::name
                        )
                ),
                spec.getEnum("sorting_default_sort_mode", ConfigProvider.SortMode.CATEGORY, ConfigProvider.SortMode.class),
                (value) -> spec.setEnum("sorting_default_sort_mode", value)
        );

        // Only the 'Enable' is big
        list.addBig(sortingEnabled);
        list.addSmall(showSortingBtns, sortingMinimizedDisplay, defaultSortMode);

        // ===== REFILL =====
        list.addTitle(Component.translatable("config.ls_tweaks.title.refill"));

        OptionInstance<Boolean> refillEnabled = OptionInstance.createBoolean(
                "config." + modId + ".refill_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".refill_enabled.tooltip")),
                spec.getBoolean("refill_enabled", true),
                v -> spec.setBoolean("refill_enabled", v)
        );
        OptionInstance<Boolean> refillStrictNbt = OptionInstance.createBoolean(
                "config." + modId + ".refill_strictNbt",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".refill_strictNbt.tooltip")),
                spec.getBoolean("refill_strictNbt", true),
                v -> spec.setBoolean("refill_strictNbt", v)
        );
        

        list.addBig(refillEnabled);
        list.addSmall(refillStrictNbt);

        // ===== QUICK STACK =====
        list.addTitle(Component.translatable("config.ls_tweaks.title.quickstack"));

        OptionInstance<Boolean> quickStackEnabled = OptionInstance.createBoolean(
                "config." + modId + ".quickstack_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".quickstack_enabled.tooltip")),
                spec.getBoolean("quickstack_enabled", true),
                v -> spec.setBoolean("quickstack_enabled", v)
        );
        OptionInstance<Boolean> quickStackAllowCont = OptionInstance.createBoolean(
                "config." + modId + ".quickstack_allowContainerActions",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".quickstack_allowContainerActions.tooltip")),
                spec.getBoolean("quickstack_allowContainerActions", true),
                v -> spec.setBoolean("quickstack_allowContainerActions", v)
        );
        OptionInstance<Boolean> showQuickStackBtns = OptionInstance.createBoolean(
                "config." + modId + ".ui_show_quickstack_buttons",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".ui_show_quickstack_buttons.tooltip")),
                spec.getBoolean("ui_show_quickstack_buttons", true),
                v -> spec.setBoolean("ui_show_quickstack_buttons", v)
        );

        list.addBig(quickStackEnabled);
        list.addSmall(quickStackAllowCont, showQuickStackBtns);

        // ===== UI (Shared) =====
        list.addTitle(Component.translatable("config.ls_tweaks.title.ui"));

        // Shift-Drag Quick-Move
        OptionInstance<Boolean> quickMoveDrag = OptionInstance.createBoolean(
                "config." + modId + ".quickmove_drag_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".quickmove_drag_enabled.tooltip")),
                spec.getBoolean("quickmove_drag_enabled", true),
                v -> spec.setBoolean("quickmove_drag_enabled", v)
        );
        list.addSmall(quickMoveDrag);

        // Slot Locking
        OptionInstance<Boolean> slotLockEnabled = OptionInstance.createBoolean(
                "config." + modId + ".slotlock_enabled",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".slotlock_enabled.tooltip")),
                spec.getBoolean("slotlock_enabled", true),
                v -> spec.setBoolean("slotlock_enabled", v)
        );
        OptionInstance<Boolean> slotLockShowIcons = OptionInstance.createBoolean(
                "config." + modId + ".slotlock_showIcons",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".slotlock_showIcons.tooltip")),
                spec.getBoolean("slotlock_showIcons", true),
                v -> spec.setBoolean("slotlock_showIcons", v)
        );
        OptionInstance<Boolean> slotLockStopQuickMoves = OptionInstance.createBoolean(
                "config." + modId + ".slotlock_stopQuickMoves",
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + modId + ".slotlock_stopQuickMoves.tooltip")),
                spec.getBoolean("slotlock_stopQuickMoves", false),
                v -> spec.setBoolean("slotlock_stopQuickMoves", v)
        );
        list.addBig(slotLockEnabled);
        list.addSmall(slotLockShowIcons, slotLockStopQuickMoves);

        // ===== KEYBINDS (header only; binding is in Controls) =====
        list.addTitle(Component.translatable("config.ls_tweaks.title.keybinds"));

        // Inline keybind capture buttons
        KeybindButton sortKeyBtn = new KeybindButton(
                0, 0, 180, 20,
                "config.ls_tweaks.keybind_sort_key",
                net.lyivx.ls_tweaks.common.keybinds.SortKeybind.SORT_KEY,
                spec,
                "keybind_sort_key"
        );
        KeybindButton openConfigBtn = new KeybindButton(
                0, 0, 180, 20,
                "config.ls_tweaks.keybind_open_config_key",
                net.lyivx.ls_tweaks.common.keybinds.ConfigKeybind.OPEN_CONFIG,
                spec,
                "keybind_open_config_key"
        );

        list.addSmall(sortKeyBtn, openConfigBtn);
    }

    // (helpers removed; logic moved into KeybindButton)

    public static void openConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(new ModConfigScreen(mc.screen, mc.options, ConfigProvider.PROVIDER_ID));
    }
    private ClientConfigHelper() {}
}
