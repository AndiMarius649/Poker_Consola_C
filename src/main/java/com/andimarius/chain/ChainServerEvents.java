package com.andimarius.chain;

import com.andimarius.chain.network.ChainNetwork;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Toată logica de server:
 * - comenzi (/chain bind, /chain unbind)
 * - fizica lanțului (forță de tragere)
 * - cleanup la moarte/deconectare
 */
public class ChainServerEvents {
    private static final double MAX_CHAIN_DISTANCE = 12.0D;
    private static final double FORCE_MULTIPLIER = 0.08D;
    private static final double MAX_FORCE = 0.55D;

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("chain")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("bind")
                                .then(Commands.argument("first", EntityArgument.player())
                                        .then(Commands.argument("second", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer first = EntityArgument.getPlayer(context, "first");
                                                    ServerPlayer second = EntityArgument.getPlayer(context, "second");

                                                    if (first.getUUID().equals(second.getUUID())) {
                                                        context.getSource().sendFailure(Component.literal("Nu poți lega același jucător."));
                                                        return 0;
                                                    }

                                                    ChainManager.bind(first.getUUID(), second.getUUID());
                                                    ChainNetwork.broadcastUpdate(first.getUUID(), Optional.of(second.getUUID()));
                                                    ChainNetwork.broadcastUpdate(second.getUUID(), Optional.of(first.getUUID()));

                                                    context.getSource().sendSuccess(() -> Component.literal(
                                                            "Legătură creată între " + first.getName().getString() + " și " + second.getName().getString()
                                                    ), true);
                                                    return Command.SINGLE_SUCCESS;
                                                })))))
                        .then(Commands.literal("unbind")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            UUID playerId = player.getUUID();
                                            Optional<UUID> partner = ChainManager.getPartner(playerId);

                                            if (partner.isEmpty()) {
                                                context.getSource().sendFailure(Component.literal("Jucătorul nu este legat."));
                                                return 0;
                                            }

                                            UUID partnerId = partner.get();
                                            ChainManager.unbind(playerId);
                                            ChainNetwork.broadcastUpdate(playerId, Optional.empty());
                                            ChainNetwork.broadcastUpdate(partnerId, Optional.empty());

                                            context.getSource().sendSuccess(() -> Component.literal("Legătura a fost eliminată."), true);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(Commands.literal("query")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            Optional<UUID> partner = ChainManager.getPartner(player.getUUID());
                                            if (partner.isEmpty()) {
                                                context.getSource().sendSuccess(() -> Component.literal("Jucătorul nu este legat."), false);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            context.getSource().sendSuccess(() -> Component.literal("Partener UUID: " + partner.get()), false);
                                            return Command.SINGLE_SUCCESS;
                                        })))
        );
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        UUID playerId = player.getUUID();

        Optional<UUID> partnerOpt = ChainManager.getPartner(playerId);
        if (partnerOpt.isEmpty()) {
            return;
        }

        UUID partnerId = partnerOpt.get();

        if (playerId.compareTo(partnerId) >= 0) {
            return;
        }

        ServerPlayer partner = player.server.getPlayerList().getPlayer(partnerId);
        if (partner == null) {
            ChainManager.unbind(playerId);
            ChainNetwork.broadcastUpdate(playerId, Optional.empty());
            ChainNetwork.broadcastUpdate(partnerId, Optional.empty());
            return;
        }

        if (partner.level() != player.level() || !partner.isAlive() || !player.isAlive()) {
            return;
        }

        Vec3 delta = partner.position().subtract(player.position());
        double distance = delta.length();
        if (distance <= MAX_CHAIN_DISTANCE || distance < 0.0001D) {
            return;
        }

        double excess = distance - MAX_CHAIN_DISTANCE;
        double force = Math.min(excess * FORCE_MULTIPLIER, MAX_FORCE);
        Vec3 normalized = delta.normalize();

        Vec3 playerBoost = normalized.scale(force);
        Vec3 partnerBoost = normalized.scale(-force);

        player.setDeltaMovement(player.getDeltaMovement().add(playerBoost));
        partner.setDeltaMovement(partner.getDeltaMovement().add(partnerBoost));

        player.hurtMarked = true;
        partner.hurtMarked = true;
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        cleanupLink(event.getEntity());
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            cleanupLink(player);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Snapshot complet pentru jucătorul care schimbă dimensiunea.
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ChainNetwork.sendSnapshot(serverPlayer, ChainManager.snapshot());
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ChainNetwork.sendSnapshot(serverPlayer, ChainManager.snapshot());
        }
    }

    private void cleanupLink(Player player) {
        UUID id = player.getUUID();
        Optional<UUID> partner = ChainManager.getPartner(id);

        if (partner.isEmpty()) {
            return;
        }

        UUID partnerId = partner.get();
        ChainManager.unbind(id);

        ChainNetwork.broadcastUpdate(id, Optional.empty());
        ChainNetwork.broadcastUpdate(partnerId, Optional.empty());
    }
}
