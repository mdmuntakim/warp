package com.mdmuntakim.warp;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * One pending /tpa request: the requester wants to be teleported to the target.
 */
public record PendingRequest(
        UUID requester,
        UUID target,
        String requesterName,
        String targetName,
        String reason,
        long sentAt,
        long expiresAt) {

    public static PendingRequest of(UUID requester, UUID target, String requesterName, String targetName,
                                   String reason, long now, long expirySeconds) {
        long expiresAt = now + TimeUnit.SECONDS.toMillis(Math.max(1L, expirySeconds));
        return new PendingRequest(requester, target, requesterName, targetName, reason, now, expiresAt);
    }

    public boolean expired(long now) {
        return now >= expiresAt;
    }
}
