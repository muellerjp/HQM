package hardcorequesting.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HQMPacketPayload(byte[] bytes) implements CustomPacketPayload {
    public static final Type<HQMPacketPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("hardcorequesting", "packet"));

    public static final StreamCodec<FriendlyByteBuf, HQMPacketPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.bytes().length);
                buf.writeBytes(payload.bytes());
            },
            buf -> {
                int len = buf.readVarInt();
                byte[] data = new byte[len];
                buf.readBytes(data);
                return new HQMPacketPayload(data);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
