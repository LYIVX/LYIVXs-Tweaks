package net.lyivx.ls_tweaks.common.network;

import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.QolConfig;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.feature.refill.RefillCommon;
import net.lyivx.ls_tweaks.common.feature.sort.SortingCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class QolNet {
    public static final ResourceLocation ID_REFILL = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "refill_request");
    public static final ResourceLocation ID_SORT = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "sort_request");
    public static final ResourceLocation ID_WL_SYNC  = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "whitelist_sync");

    public record RefillC2SPayload(int hotbarSlot, ItemStack signature, boolean strict)
            implements CustomPacketPayload {
        public static final Type<RefillC2SPayload> TYPE = new Type<>(ID_REFILL);

        public static final StreamCodec<RegistryFriendlyByteBuf, RefillC2SPayload> STREAM_CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.hotbarSlot);
                    buf.writeBoolean(p.strict);
                    ItemStack.STREAM_CODEC.encode(buf, p.signature);
                }, buf -> {
                    int slot = buf.readVarInt();
                    boolean strict = buf.readBoolean();
                    ItemStack sig = ItemStack.STREAM_CODEC.decode(buf);
                    return new RefillC2SPayload(slot, sig, strict);
                });

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /* ---------------- Sort C2S ---------------- */

    public record SortC2SPayload(Target target, Mode mode, boolean mergeBeforeSort, boolean unstackablesLast)
            implements CustomPacketPayload {
        public enum Target { PLAYER, CONTAINER }
        public enum Mode   { DEFAULT, ALPHA_ASC, ALPHA_DESC }

        public static final Type<SortC2SPayload> TYPE = new Type<>(ID_SORT);

        public static final StreamCodec<RegistryFriendlyByteBuf, SortC2SPayload> STREAM_CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeEnum(p.target);
                    buf.writeEnum(p.mode);
                    buf.writeBoolean(p.mergeBeforeSort);
                    buf.writeBoolean(p.unstackablesLast);
                }, buf -> new SortC2SPayload(
                        buf.readEnum(Target.class),
                        buf.readEnum(Mode.class),
                        buf.readBoolean(),
                        buf.readBoolean()
                ));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record WhitelistSyncS2CPayload(List<ResourceLocation> menuIds, List<String> screenClasses)
            implements CustomPacketPayload {
        public static final Type<WhitelistSyncS2CPayload> TYPE = new Type<>(ID_WL_SYNC);

        public static final StreamCodec<RegistryFriendlyByteBuf, WhitelistSyncS2CPayload> STREAM_CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.menuIds.size());
                    for (var id : p.menuIds) buf.writeResourceLocation(id);
                    buf.writeVarInt(p.screenClasses.size());
                    for (var s : p.screenClasses) buf.writeUtf(s);
                }, buf -> {
                    int n = buf.readVarInt();
                    List<ResourceLocation> ids = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) ids.add(buf.readResourceLocation());
                    int m = buf.readVarInt();
                    List<String> cls = new ArrayList<>(m);
                    for (int i = 0; i < m; i++) cls.add(buf.readUtf());
                    return new WhitelistSyncS2CPayload(ids, cls);
                });

        public static WhitelistSyncS2CPayload from(java.util.Set<String> menuIds, java.util.Set<String> screens) {
            java.util.List<ResourceLocation> ids = new java.util.ArrayList<>(menuIds.size());
            for (String s : menuIds) ids.add(parseRL(s));
            return new WhitelistSyncS2CPayload(ids, new java.util.ArrayList<>(screens));
        }

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // helper
    private static ResourceLocation parseRL(String s) {
        int i = s.indexOf(':');
        if (i <= 0) {
            // fallback to minecraft if namespace missing
            return ResourceLocation.fromNamespaceAndPath("minecraft", s);
        }
        return ResourceLocation.fromNamespaceAndPath(s.substring(0, i), s.substring(i + 1));
    }

    /* ---------------- Registration ---------------- */

    public static void register() {
        // Refill
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                RefillC2SPayload.TYPE,
                RefillC2SPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ServerPlayer sp = (ServerPlayer) ctx.getPlayer();
                    if (sp != null) RefillCommon.onRefillRequest(sp, payload);
                }
        );

        // Sorting
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                SortC2SPayload.TYPE,
                SortC2SPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ServerPlayer sp = (ServerPlayer) ctx.getPlayer();
                    if (sp != null) SortingCommon.onSortRequest(sp, payload);
                }
        );

        // S2C: whitelist sync
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                WhitelistSyncS2CPayload.TYPE,
                WhitelistSyncS2CPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    // apply on client: replace allowed sets in QolConfig
                    var allowedIds = new java.util.LinkedHashSet<String>();
                    for (var id : payload.menuIds()) allowedIds.add(id.toString());
                    var allowedScreens = new java.util.LinkedHashSet<>(payload.screenClasses());

                    var s = QolConfig.sorting();
                    var merged = new QolConfig.Sorting(
                            s.enabled, s.mergeBeforeSort, s.unstackablesLast, s.allowContainerSorting,
                            allowedIds, allowedScreens
                    );
                    QolConfig.setSorting(merged);
                }
        );
    }

    private QolNet() {}
}