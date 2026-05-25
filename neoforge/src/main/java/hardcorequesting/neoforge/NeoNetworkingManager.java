package hardcorequesting.neoforge;

import com.google.common.collect.Maps;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NeoNetworkingManager implements NetworkManager {
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> S2C = Maps.newHashMap();
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> C2S = Maps.newHashMap();

    static void handleServerBound(HQMPacketPayload payload, IPayloadContext context) {
        dispatch(payload, context, C2S, false);
    }

    static void handleClientBound(HQMPacketPayload payload, IPayloadContext context) {
        dispatch(payload, context, S2C, true);
    }

    private static void dispatch(HQMPacketPayload payload, IPayloadContext context,
            Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> map, boolean isClient) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
        ResourceLocation id = buf.readResourceLocation();
        BiConsumer<PacketContext, FriendlyByteBuf> handler = map.get(id);
        if (handler == null) return;
        Player player = context.player();
        handler.accept(new PacketContext() {
            @Override public Player getPlayer() { return player; }
            @Override public Consumer<Runnable> getTaskQueue() { return r -> context.enqueueWork(r); }
            @Override public boolean isClient() { return isClient; }
        }, buf);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        S2C.put(id, consumer);
    }

    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        C2S.put(id, consumer);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToServer(makePayload(id, buf));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        PacketDistributor.sendToPlayer(player, makePayload(id, buf));
    }

    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buf) {
        return new ClientboundCustomPayloadPacket(makePayload(id, buf));
    }

    static HQMPacketPayload makePayload(ResourceLocation id, FriendlyByteBuf data) {
        FriendlyByteBuf combined = new FriendlyByteBuf(Unpooled.buffer());
        combined.writeResourceLocation(id);
        combined.writeBytes(data);
        byte[] bytes = ByteBufUtil.getBytes(combined);
        combined.release();
        return new HQMPacketPayload(bytes);
    }
}
