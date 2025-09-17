package net.lyivx.ls_tweaks.common.feature.sort;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import dev.architectury.event.events.common.PlayerEvent;
import net.lyivx.ls_tweaks.common.network.QolNet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Datapack-driven whitelist:
 *   data/<namespace>/ls_tweaks/sorting_whitelist/*.json
 *
 * JSON shape:
 * { "menu_ids": [...], "screen_classes": [...] }
 *
 * Union-merges across all files. Server stores sets and syncs to clients
 * (on join and after datapack reload).
 */
public final class SortingWhitelistData extends SimpleJsonResourceReloadListener<SortingWhitelistData.Model> {
    public static final String FOLDER = "ls_tweaks/sorting_whitelist";
    private static final ResourceLocation RELOAD_ID =
            ResourceLocation.fromNamespaceAndPath("ls_tweaks", "sorting_whitelist");

    public static final Codec<Model> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.listOf().optionalFieldOf("menu_ids", List.of()).forGetter(Model::menu_ids),
            Codec.STRING.listOf().optionalFieldOf("screen_classes", List.of()).forGetter(Model::screen_classes)
    ).apply(inst, Model::new));

    // Server-authoritative (client receives via S2C and writes here too)
    private static volatile Set<String> ALLOWED_MENU_IDS = new LinkedHashSet<>();
    private static volatile Set<String> ALLOWED_SCREENS  = new LinkedHashSet<>();

    private static volatile MinecraftServer SERVER = null;

    public SortingWhitelistData() {
        super(CODEC, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<ResourceLocation, Model> objects, ResourceManager rm, ProfilerFiller pf) {
        LinkedHashSet<String> menus   = new LinkedHashSet<>();
        LinkedHashSet<String> screens = new LinkedHashSet<>();

        for (Model m : objects.values()) {
            if (m.menu_ids() != null)       m.menu_ids().forEach(s -> { if (!s.isBlank()) menus.add(s.trim()); });
            if (m.screen_classes() != null) m.screen_classes().forEach(s -> { if (!s.isBlank()) screens.add(s.trim()); });
        }

        ALLOWED_MENU_IDS = menus;
        ALLOWED_SCREENS  = screens;

        // Broadcast to all online players after /reload
        if (SERVER != null) {
            var payload = QolNet.WhitelistSyncS2CPayload.from(ALLOWED_MENU_IDS, ALLOWED_SCREENS);
            for (ServerPlayer p : SERVER.getPlayerList().getPlayers()) {
                NetworkManager.sendToPlayer(p, payload);
            }
        }
    }

    /** Call during common init. */
    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new SortingWhitelistData(), RELOAD_ID);

        LifecycleEvent.SERVER_STARTED.register(server -> SERVER = server);
        LifecycleEvent.SERVER_STOPPED.register(server -> SERVER = null);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (!(player instanceof ServerPlayer sp)) return;
            var payload = QolNet.WhitelistSyncS2CPayload.from(ALLOWED_MENU_IDS, ALLOWED_SCREENS);
            NetworkManager.sendToPlayer(sp, payload);
        });
    }

    // ----- Public accessors for other code (client & server) -----
    public static Set<String> allowedMenuIds()      { return ALLOWED_MENU_IDS; }
    public static Set<String> allowedScreenClasses(){ return ALLOWED_SCREENS;  }

    // Called on client when S2C whitelist sync arrives
    public static void setClientWhitelist(Set<String> menuIds, Set<String> screens) {
        ALLOWED_MENU_IDS = new LinkedHashSet<>(menuIds);
        ALLOWED_SCREENS  = new LinkedHashSet<>(screens);
    }

    // ----- JSON model -----
    public static final class Model {
        private final List<String> menu_ids;
        private final List<String> screen_classes;
        public Model(List<String> menu_ids, List<String> screen_classes) {
            this.menu_ids = (menu_ids == null) ? List.of() : new ArrayList<>(menu_ids);
            this.screen_classes = (screen_classes == null) ? List.of() : new ArrayList<>(screen_classes);
        }
        public List<String> menu_ids()       { return menu_ids; }
        public List<String> screen_classes() { return screen_classes; }
    }
}