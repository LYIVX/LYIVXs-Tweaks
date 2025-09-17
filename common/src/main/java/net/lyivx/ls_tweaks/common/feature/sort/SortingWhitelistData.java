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

    // ----- Layout enum we expose to buttons -----
    public enum Layout { HORIZONTAL, VERTICAL }

    // ----- JSON model (backward compat) -----
    public static final class IdGroup {
        public final List<String> menu_ids;
        public final List<String> screen_classes;
        IdGroup(List<String> m, List<String> s) {
            this.menu_ids = m == null ? List.of() : m;
            this.screen_classes = s == null ? List.of() : s;
        }
    }
    public static final class Layouts {
        public final IdGroup horizontal;
        public final IdGroup vertical;
        Layouts(IdGroup h, IdGroup v) { this.horizontal = h; this.vertical = v; }
    }
    public static final class Model {
        // legacy fields
        public final List<String> menu_ids;
        public final List<String> screen_classes;
        // new layouts object
        public final Layouts layouts;
        Model(List<String> m, List<String> s, Layouts l) { this.menu_ids = m; this.screen_classes = s; this.layouts = l; }

        // Codecs
        static final Codec<IdGroup> GROUP_CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.listOf().optionalFieldOf("menu_ids", List.of()).forGetter(g -> g.menu_ids),
                Codec.STRING.listOf().optionalFieldOf("screen_classes", List.of()).forGetter(g -> g.screen_classes)
        ).apply(i, IdGroup::new));

        static final Codec<Layouts> LAYOUTS_CODEC = RecordCodecBuilder.create(i -> i.group(
                GROUP_CODEC.optionalFieldOf("horizontal", new IdGroup(List.of(), List.of())).forGetter(l -> l.horizontal),
                GROUP_CODEC.optionalFieldOf("vertical",   new IdGroup(List.of(), List.of())).forGetter(l -> l.vertical)
        ).apply(i, Layouts::new));

        static final Codec<Model> CODEC = RecordCodecBuilder.create(i -> i.group(
                // legacy
                Codec.STRING.listOf().optionalFieldOf("menu_ids", List.of()).forGetter(m -> m.menu_ids == null ? List.of() : m.menu_ids),
                Codec.STRING.listOf().optionalFieldOf("screen_classes", List.of()).forGetter(m -> m.screen_classes == null ? List.of() : m.screen_classes),
                // new
                LAYOUTS_CODEC.optionalFieldOf("layouts", new Layouts(new IdGroup(List.of(), List.of()), new IdGroup(List.of(), List.of()))).forGetter(m -> m.layouts == null ? new Layouts(new IdGroup(List.of(), List.of()), new IdGroup(List.of(), List.of())) : m.layouts)
        ).apply(i, Model::new));
    }

    // Use the model codec
    public static final Codec<Model> CODEC = Model.CODEC;

    // ----- Server-authoritative sets (client receives via S2C) -----
    private static volatile Set<String> ALLOWED_MENU_IDS = new LinkedHashSet<>();
    private static volatile Set<String> ALLOWED_SCREENS  = new LinkedHashSet<>();
    private static volatile Set<String> VERT_MENU_IDS    = new LinkedHashSet<>();
    private static volatile Set<String> VERT_SCREENS     = new LinkedHashSet<>();

    private static volatile MinecraftServer SERVER = null;

    public SortingWhitelistData() {
        super(CODEC, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<ResourceLocation, Model> objects, ResourceManager rm, ProfilerFiller pf) {
        // temp unions
        LinkedHashSet<String> menusAll   = new LinkedHashSet<>();
        LinkedHashSet<String> screensAll = new LinkedHashSet<>();
        LinkedHashSet<String> menusVert  = new LinkedHashSet<>();
        LinkedHashSet<String> screensVert= new LinkedHashSet<>();

        for (Model m : objects.values()) {
            // legacy -> treated as HORIZONTAL allowlist
            if (m.menu_ids != null)       m.menu_ids.forEach(s -> { if (!s.isBlank()) menusAll.add(s.trim()); });
            if (m.screen_classes != null) m.screen_classes.forEach(s -> { if (!s.isBlank()) screensAll.add(s.trim()); });

            if (m.layouts != null) {
                // horizontal group contributes to "allowed"
                if (m.layouts.horizontal != null) {
                    m.layouts.horizontal.menu_ids.forEach(s -> { if (!s.isBlank()) menusAll.add(s.trim()); });
                    m.layouts.horizontal.screen_classes.forEach(s -> { if (!s.isBlank()) screensAll.add(s.trim()); });
                }
                // vertical group contributes to "allowed" AND vertical-sets
                if (m.layouts.vertical != null) {
                    m.layouts.vertical.menu_ids.forEach(s -> {
                        if (!s.isBlank()) { s = s.trim(); menusAll.add(s); menusVert.add(s); }
                    });
                    m.layouts.vertical.screen_classes.forEach(s -> {
                        if (!s.isBlank()) { s = s.trim(); screensAll.add(s); screensVert.add(s); }
                    });
                }
            }
        }

        ALLOWED_MENU_IDS = menusAll;
        ALLOWED_SCREENS  = screensAll;
        VERT_MENU_IDS    = menusVert;
        VERT_SCREENS     = screensVert;

        // broadcast after /reload
        if (SERVER != null) {
            var payload = QolNet.WhitelistSyncS2CPayload.from(ALLOWED_MENU_IDS, ALLOWED_SCREENS, VERT_MENU_IDS, VERT_SCREENS);
            for (ServerPlayer p : SERVER.getPlayerList().getPlayers()) NetworkManager.sendToPlayer(p, payload);
        }
    }

    /** Call during common init. */
    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new SortingWhitelistData(), RELOAD_ID);

        LifecycleEvent.SERVER_STARTED.register(server -> SERVER = server);
        LifecycleEvent.SERVER_STOPPED.register(server -> SERVER = null);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (!(player instanceof ServerPlayer sp)) return;
            var payload = QolNet.WhitelistSyncS2CPayload.from(ALLOWED_MENU_IDS, ALLOWED_SCREENS, VERT_MENU_IDS, VERT_SCREENS);
            NetworkManager.sendToPlayer(sp, payload);
        });
    }

    // ----- Public accessors -----
    public static Set<String> allowedMenuIds()       { return ALLOWED_MENU_IDS; }
    public static Set<String> allowedScreenClasses() { return ALLOWED_SCREENS; }
    public static boolean isVerticalMenu(String rl)  { return VERT_MENU_IDS.contains(rl); }
    public static boolean isVerticalScreen(String cn){ return VERT_SCREENS.contains(cn); }

    // A helper your buttons can use
    public static Layout layoutFor(String menuIdOrScreenClass) {
        return (VERT_MENU_IDS.contains(menuIdOrScreenClass) || VERT_SCREENS.contains(menuIdOrScreenClass))
                ? Layout.VERTICAL : Layout.HORIZONTAL;
    }

    // Called on client when S2C whitelist sync arrives
    public static void setClientWhitelist(Set<String> allowedIds, Set<String> allowedScreens,
                                          Set<String> verticalIds, Set<String> verticalScreens) {
        ALLOWED_MENU_IDS = new LinkedHashSet<>(allowedIds);
        ALLOWED_SCREENS  = new LinkedHashSet<>(allowedScreens);
        VERT_MENU_IDS    = new LinkedHashSet<>(verticalIds);
        VERT_SCREENS     = new LinkedHashSet<>(verticalScreens);
    }
}