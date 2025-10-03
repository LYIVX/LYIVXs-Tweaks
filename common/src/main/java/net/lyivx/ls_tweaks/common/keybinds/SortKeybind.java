package net.lyivx.ls_tweaks.common.keybinds;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.minecraft.client.KeyMapping;

public class SortKeybind {
    public static KeyMapping SORT_KEY;

    public static void register() {
        int keyCode = ConfigProvider.keybindSortKey();
        SORT_KEY = new KeyMapping("key.ls_tweaks.sort_inventory", keyCode, "category.ls_tweaks");
        KeyMappingRegistry.register(SORT_KEY);
    }
}
