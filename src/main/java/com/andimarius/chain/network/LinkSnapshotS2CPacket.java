package com.andimarius.chain.network;

import com.andimarius.chain.client.ClientChainState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Snapshot complet al tuturor legăturilor active.
 */
public record LinkSnapshotS2CPacket(Map<UUID, UUID> links) {

    public static void encode(LinkSnapshotS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.links.size());
        packet.links.forEach((player, partner) -> {
            buffer.writeUUID(player);
            buffer.writeUUID(partner);
        });
    }

    public static LinkSnapshotS2CPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<UUID, UUID> links = new HashMap<>();

        for (int i = 0; i < size; i++) {
            links.put(buffer.readUUID(), buffer.readUUID());
        }

        return new LinkSnapshotS2CPacket(links);
    }

    public static void handle(LinkSnapshotS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientChainState.applySnapshot(packet.links));
        context.setPacketHandled(true);
    }
}
