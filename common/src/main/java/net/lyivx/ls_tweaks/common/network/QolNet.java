package net.lyivx.ls_tweaks.common.network;

import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.refill.RefillCommon;
import net.lyivx.ls_tweaks.common.feature.sort.SortingCommon;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelistData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class QolNet {
    public static final ResourceLocation ID_REFILL  = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "refill_request");
    public static final ResourceLocation ID_SORT    = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "sort_request");
    public static final ResourceLocation ID_WL_SYNC = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "whitelist_sync");

    /* ---------------- Refill C2S ---------------- */

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

    /* ---------------- Whitelist Sync S2C ---------------- */

    public record WhitelistSyncS2CPayload(
            List<ResourceLocation> menuIds,
            List<String> screenClasses,
            List<ResourceLocation> verticalMenuIds,
            List<String> verticalScreenClasses
    ) implements CustomPacketPayload {

        public static final Type<WhitelistSyncS2CPayload> TYPE = new Type<>(ID_WL_SYNC);

        public static final StreamCodec<RegistryFriendlyByteBuf, WhitelistSyncS2CPayload> STREAM_CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.menuIds.size());
                    for (var id : p.menuIds) buf.writeResourceLocation(id);
                    buf.writeVarInt(p.screenClasses.size());
                    for (var s : p.screenClasses) buf.writeUtf(s);
                    buf.writeVarInt(p.verticalMenuIds.size());
                    for (var id : p.verticalMenuIds) buf.writeResourceLocation(id);
                    buf.writeVarInt(p.verticalScreenClasses.size());
                    for (var s : p.verticalScreenClasses) buf.writeUtf(s);
                }, buf -> {
                    int n = buf.readVarInt();
                    List<ResourceLocation> ids = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) ids.add(buf.readResourceLocation());

                    int m = buf.readVarInt();
                    List<String> cls = new ArrayList<>(m);
                    for (int i = 0; i < m; i++) cls.add(buf.readUtf());

                    int nv = buf.readVarInt();
                    List<ResourceLocation> vIds = new ArrayList<>(nv);
                    for (int i = 0; i < nv; i++) vIds.add(buf.readResourceLocation());

                    int mv = buf.readVarInt();
                    List<String> vCls = new ArrayList<>(mv);
                    for (int i = 0; i < mv; i++) vCls.add(buf.readUtf());

                    return new WhitelistSyncS2CPayload(ids, cls, vIds, vCls);
                });

        public static WhitelistSyncS2CPayload from(Set<String> allowedIds, Set<String> allowedScreens,
                                                   Set<String> verticalIds, Set<String> verticalScreens) {
            List<ResourceLocation> ids = new ArrayList<>(allowedIds.size());
            for (String s : allowedIds) ids.add(parseRL(s));
            List<ResourceLocation> vIds = new ArrayList<>(verticalIds.size());
            for (String s : verticalIds) vIds.add(parseRL(s));
            return new WhitelistSyncS2CPayload(ids, new ArrayList<>(allowedScreens), vIds, new ArrayList<>(verticalScreens));
        }

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // helper
    private static ResourceLocation parseRL(String s) {
        int i = s.indexOf(':');
        if (i <= 0) return ResourceLocation.fromNamespaceAndPath("minecraft", s);
        return ResourceLocation.fromNamespaceAndPath(s.substring(0, i), s.substring(i + 1));
    }

    /* ---------------- Registration ---------------- */

    public static void register() {
        // Refill C2S
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                RefillC2SPayload.TYPE,
                RefillC2SPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ServerPlayer sp = (ServerPlayer) ctx.getPlayer();
                    if (sp != null) RefillCommon.onRefillRequest(sp, payload);
                }
        );

        // Sorting C2S
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                SortC2SPayload.TYPE,
                SortC2SPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ServerPlayer sp = (ServerPlayer) ctx.getPlayer();
                    if (sp != null) SortingCommon.onSortRequest(sp, payload);
                }
        );

        // Whitelist Sync S2C → write into SortingWhitelistData's static sets (client side)
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                WhitelistSyncS2CPayload.TYPE,
                WhitelistSyncS2CPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    var ids = new LinkedHashSet<String>();
                    for (var id : payload.menuIds()) ids.add(id.toString());
                    var cls = new LinkedHashSet<>(payload.screenClasses());

                    var vIds = new LinkedHashSet<String>();
                    for (var id : payload.verticalMenuIds()) vIds.add(id.toString());
                    var vCls = new LinkedHashSet<>(payload.verticalScreenClasses());

                    SortingWhitelistData.setClientWhitelist(ids, cls, vIds, vCls);
                }
        );
    }

    private QolNet() {}
}