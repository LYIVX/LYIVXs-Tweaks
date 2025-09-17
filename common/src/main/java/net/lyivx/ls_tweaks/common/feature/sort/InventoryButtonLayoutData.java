package net.lyivx.ls_tweaks.common.feature.sort;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.ReloadListenerRegistry;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Datapack-driven inventory button layout overrides (ONLY affects the "Inventory" button set).
 *
 * Folder: data/<ns>/ls_tweaks/inventory_button_layout/*.json
 *
 * Schema (simple):
 * {
 *   "vertical": {
 *     "menu_ids": ["minecraft:furnace", "minecraft:brewing_stand"],
 *     "screen_classes": ["net.minecraft.client.gui.screens.inventory.BrewingStandScreen"]
 *   }
 * }
 *
 * Any container/screen listed under "vertical" will render the INVENTORY buttons vertically
 * when that container is open. Everything else remains horizontal.
 */
public final class InventoryButtonLayoutData extends SimpleJsonResourceReloadListener<InventoryButtonLayoutData.Model> {
    public static final String FOLDER = "ls_tweaks/inventory_button_layout";
    private static final ResourceLocation RELOAD_ID =
            ResourceLocation.fromNamespaceAndPath("ls_tweaks", "inventory_button_layout");

    public static final class IdGroup {
        public final List<String> menu_ids;
        public final List<String> screen_classes;
        public IdGroup(List<String> m, List<String> s) {
            this.menu_ids = m == null ? List.of() : m;
            this.screen_classes = s == null ? List.of() : s;
        }
    }
    public static final class Model {
        public final IdGroup vertical;
        public Model(IdGroup v) { this.vertical = v == null ? new IdGroup(List.of(), List.of()) : v; }

        static final Codec<IdGroup> GROUP = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.listOf().optionalFieldOf("menu_ids", List.of()).forGetter(g -> g.menu_ids),
                Codec.STRING.listOf().optionalFieldOf("screen_classes", List.of()).forGetter(g -> g.screen_classes)
        ).apply(i, IdGroup::new));

        static final Codec<Model> CODEC = RecordCodecBuilder.create(i -> i.group(
                GROUP.optionalFieldOf("vertical", new IdGroup(List.of(), List.of())).forGetter(m -> m.vertical)
        ).apply(i, Model::new));
    }
    public static final Codec<Model> CODEC = Model.CODEC;

    private static volatile Set<String> VERTICAL_MENU_IDS = new LinkedHashSet<>();
    private static volatile Set<String> VERTICAL_SCREENS  = new LinkedHashSet<>();

    private static volatile MinecraftServer SERVER = null;

    public InventoryButtonLayoutData() {
        super(CODEC, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<ResourceLocation, Model> objects, ResourceManager rm, ProfilerFiller pf) {
        LinkedHashSet<String> vIds  = new LinkedHashSet<>();
        LinkedHashSet<String> vScrs = new LinkedHashSet<>();

        for (Model m : objects.values()) {
            if (m.vertical != null) {
                m.vertical.menu_ids.forEach(s -> { if (!s.isBlank()) vIds.add(s.trim()); });
                m.vertical.screen_classes.forEach(s -> { if (!s.isBlank()) vScrs.add(s.trim()); });
            }
        }

        VERTICAL_MENU_IDS = vIds;
        VERTICAL_SCREENS  = vScrs;

        // broadcast after /reload
        if (SERVER != null) {
            var payload = QolNet.InvLayoutSyncS2CPayload.from(VERTICAL_MENU_IDS, VERTICAL_SCREENS);
            for (ServerPlayer p : SERVER.getPlayerList().getPlayers()) NetworkManager.sendToPlayer(p, payload);
        }
    }

    /** Call during common init. */
    public static void register() {
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new InventoryButtonLayoutData(), RELOAD_ID);

        LifecycleEvent.SERVER_STARTED.register(server -> SERVER = server);
        LifecycleEvent.SERVER_STOPPED.register(server -> SERVER = null);

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (!(player instanceof ServerPlayer sp)) return;
            var payload = QolNet.InvLayoutSyncS2CPayload.from(VERTICAL_MENU_IDS, VERTICAL_SCREENS);
            NetworkManager.sendToPlayer(sp, payload);
        });
    }

    /** True if the given menuId OR screen class is flagged vertical for INVENTORY buttons. */
    public static boolean inventoryButtonsVerticalFor(String menuIdOrScreenClass) {
        return VERTICAL_MENU_IDS.contains(menuIdOrScreenClass) || VERTICAL_SCREENS.contains(menuIdOrScreenClass);
    }

    /** Client applies S2C sync here. */
    public static void setClientVertical(Set<String> vIds, Set<String> vScreens) {
        VERTICAL_MENU_IDS = new LinkedHashSet<>(vIds);
        VERTICAL_SCREENS  = new LinkedHashSet<>(vScreens);
    }
}