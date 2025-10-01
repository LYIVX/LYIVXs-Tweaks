package net.lyivx.ls_tweaks.common.keybinds;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.minecraft.client.KeyMapping;

public final class ConfigKeybind {
    public static KeyMapping OPEN_CONFIG;

    /** Call this ONCE during your client init (e.g., LYIVXs_tweaksClient.init()). */
    public static void register() {
        if (OPEN_CONFIG == null) {
            int keyCode = ConfigProvider.keybindOpenConfigKey();
            OPEN_CONFIG = new KeyMapping(
                    "key.ls_tweaks.open_config",   // translation key
                    keyCode,                         // configurable default
                    "category.ls_tweaks"     // category (add a lang entry)
            );
            KeyMappingRegistry.register(OPEN_CONFIG);
        }

        // Safe idempotent registration (Architectury lets you double-register; we guard with null/consume loop)
        ClientTickEvent.CLIENT_POST.register(client -> {
            // Client instance may be null during early boot
            if (client == null || client.player == null) return;

            // Consume ALL queued presses this tick
            if (OPEN_CONFIG != null) {
                while (OPEN_CONFIG.consumeClick()) {
                    net.lyivx.ls_tweaks.common.config.ClientConfigHelper.openConfigScreen();
                }
            }
        });
    }

    private ConfigKeybind() {}
}
