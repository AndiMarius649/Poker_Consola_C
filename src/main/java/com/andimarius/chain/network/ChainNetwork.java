package com.andimarius.chain.network;

import com.andimarius.chain.ChainedPlayersMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Canalul de rețea folosit pentru sincronizare server -> client.
 */
public final class ChainNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ChainedPlayersMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;

    private ChainNetwork() {
    }

    public static void register(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(LinkUpdateS2CPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LinkUpdateS2CPacket::encode)
                .decoder(LinkUpdateS2CPacket::decode)
                .consumerMainThread(LinkUpdateS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(LinkSnapshotS2CPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LinkSnapshotS2CPacket::encode)
                .decoder(LinkSnapshotS2CPacket::decode)
                .consumerMainThread(LinkSnapshotS2CPacket::handle)
                .add();
    }

    public static void sendSnapshot(ServerPlayer target, Map<UUID, UUID> links) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new LinkSnapshotS2CPacket(links));
    }

    public static void broadcastUpdate(UUID playerId, Optional<UUID> partnerId) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new LinkUpdateS2CPacket(playerId, partnerId));
    }
}
