package com.oiken.veinminer.network;

import com.oiken.veinminer.VeinMiner;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client → server when the player presses the toggle keybind.
 * Carries a single boolean: whether vein mining is now enabled.
 */
public record TogglePacket(boolean enabled) implements CustomPacketPayload {

    public static final Type<TogglePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(VeinMiner.MODID, "toggle")
    );

    public static final StreamCodec<ByteBuf, TogglePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    TogglePacket::enabled,
                    TogglePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
