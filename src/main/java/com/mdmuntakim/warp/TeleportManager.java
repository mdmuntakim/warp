package com.mdmuntakim.warp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the pending /tpa requests of the server. A request is "I want to come
 * to you"; the target accepts or denies it, the requester is then teleported.
 * Requests expire on their own so nobody is stuck waiting forever.
 */
public final class TeleportManager {

    private final long expirySeconds;
    private final Map<UUID, PendingRequest> outgoing = new ConcurrentHashMap<>();

    public TeleportManager(long expirySeconds) {
        this.expirySeconds = expirySeconds;
    }

    public long expirySeconds() {
        return expirySeconds;
    }

    /**
     * Registers a new request from the requester to the target.
     */
    public PendingRequest send(UUID requester, UUID target, String requesterName, String targetName,
                               String reason, long now) {
        PendingRequest request = PendingRequest.of(requester, target, requesterName, targetName, reason, now, expirySeconds);
        outgoing.put(requester, request);
        return request;
    }

    /**
     * Every live request that is addressed to the acceptor.
     */
    public List<PendingRequest> incoming(UUID acceptor, long now) {
        List<PendingRequest> found = new ArrayList<>();
        for (PendingRequest request : outgoing.values()) {
            if (request.target().equals(acceptor) && !request.expired(now)) {
                found.add(request);
            }
        }
        found.sort((left, right) -> Long.compare(left.sentAt(), right.sentAt()));
        return List.copyOf(found);
    }

    /**
     * Removes and returns the pending request between the requester and the
     * acceptor. Used by both /tpa accept and /tpa deny: accepting hands the
     * request over to the teleport, denying just drops it.
     */
    public PendingRequest take(UUID requester, UUID acceptor, long now) {
        PendingRequest current = outgoing.get(requester);
        if (current == null || !current.target().equals(acceptor)) {
            return null;
        }
        if (current.expired(now)) {
            outgoing.remove(requester);
            return null;
        }
        return outgoing.remove(requester);
    }

    public boolean cancel(UUID requester, UUID target) {
        PendingRequest current = outgoing.get(requester);
        if (current != null && current.target().equals(target)) {
            outgoing.remove(requester);
            return true;
        }
        return false;
    }

    public List<PendingRequest> outgoing(UUID requester, long now) {
        PendingRequest current = outgoing.get(requester);
        if (current == null) {
            return List.of();
        }
        List<PendingRequest> found = new ArrayList<>();
        if (!current.expired(now)) {
            found.add(current);
        }
        return List.copyOf(found);
    }

    public void clear() {
        outgoing.clear();
    }

    /**
     * How many teleport requests are currently pending.
     */
    public int pendingCount(long now) {
        int count = 0;
        for (PendingRequest request : outgoing.values()) {
            if (!request.expired(now)) {
                count++;
            }
        }
        return count;
    }
}
