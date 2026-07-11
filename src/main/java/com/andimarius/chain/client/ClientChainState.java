package com.andimarius.chain.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Cache local client pentru legături.
 * Este actualizat doar de pachetele venite de pe server.
 */
public final class ClientChainState {
    private static final Map<UUID, UUID> LINKS = new HashMap<>();

    private ClientChainState() {
    }

    public static void applySnapshot(Map<UUID, UUID> links) {
        LINKS.clear();
        LINKS.putAll(links);
    }

    public static void applyUpdate(UUID playerId, Optional<UUID> partnerId) {
        if (partnerId.isPresent()) {
            LINKS.put(playerId, partnerId.get());
        } else {
            UUID partner = LINKS.remove(playerId);
            if (partner != null) {
                LINKS.remove(partner);
            }
        }
    }

    public static Map<UUID, UUID> snapshot() {
        return new HashMap<>(LINKS);
    }
}
