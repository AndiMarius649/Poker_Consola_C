package com.andimarius.chain.network;

import com.andimarius.chain.client.ClientChainState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Update incremental pentru un singur jucător.
 */
public record LinkUpdateS2CPacket(UUID playerId, Optional<UUID> partnerId) {

    public static void encode(LinkUpdateS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeBoolean(packet.partnerId.isPresent());
        packet.partnerId.ifPresent(buffer::writeUUID);
    }

    public static LinkUpdateS2CPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        Optional<UUID> partnerId = buffer.readBoolean() ? Optional.of(buffer.readUUID()) : Optional.empty();
        return new LinkUpdateS2CPacket(playerId, partnerId);
    }

    public static void handle(LinkUpdateS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientChainState.applyUpdate(packet.playerId, packet.partnerId));
        context.setPacketHandled(true);
    }
}
