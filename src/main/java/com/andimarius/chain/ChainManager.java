package com.andimarius.chain;

import java.util.*;

/**
 * Manager server-side pentru legături.
 * Reține în memorie perechi bidirecționale (A->B și B->A) pentru acces rapid.
 */
public final class ChainManager {
    private static final Map<UUID, UUID> LINKS = new HashMap<>();

    private ChainManager() {
    }

    public static Map<UUID, UUID> snapshot() {
        return new HashMap<>(LINKS);
    }

    public static Optional<UUID> getPartner(UUID playerId) {
        return Optional.ofNullable(LINKS.get(playerId));
    }

    public static void bind(UUID first, UUID second) {
        if (first.equals(second)) {
            return;
        }

        unbind(first);
        unbind(second);
        LINKS.put(first, second);
        LINKS.put(second, first);
    }

    public static void unbind(UUID playerId) {
        UUID partner = LINKS.remove(playerId);
        if (partner != null) {
            LINKS.remove(partner);
        }
    }

    /**
     * Returnează perechi unice (A,B) fără dublură (B,A), utile la randare/tick.
     */
    public static Set<LinkPair> uniquePairs() {
        Set<LinkPair> pairs = new HashSet<>();

        for (Map.Entry<UUID, UUID> entry : LINKS.entrySet()) {
            UUID a = entry.getKey();
            UUID b = entry.getValue();
            if (a.compareTo(b) < 0) {
                pairs.add(new LinkPair(a, b));
            }
        }

        return pairs;
    }

    public record LinkPair(UUID first, UUID second) {}
}
