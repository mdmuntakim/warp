package com.mdmuntakim.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeleportManagerTest {

    private static final long NOW = 1_000L;

    @Test
    void sendRegistersAnOutgoingRequest() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        PendingRequest request = manager.send(requester, target, "Steve", "Alex", "help me", NOW);
        assertEquals(requester, request.requester());
        assertEquals(target, request.target());
        assertEquals("Steve", request.requesterName());
        assertEquals("Alex", request.targetName());
        assertEquals("help me", request.reason());
        assertEquals(NOW + 30_000L, request.expiresAt());
        assertEquals(1, manager.outgoing(requester, NOW).size());
        assertEquals(1, manager.incoming(target, NOW).size());
        assertEquals(1, manager.pendingCount(NOW));
    }

    @Test
    void incomingListsOnlyLiveRequestsAddressedToTheAcceptor() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID acceptor = UUID.randomUUID();
        manager.send(requester, acceptor, "Steve", "Alex", "", NOW);

        List<PendingRequest> incoming = manager.incoming(acceptor, NOW);
        assertEquals(1, incoming.size());
        assertEquals(requester, incoming.get(0).requester());
        assertTrue(manager.incoming(UUID.randomUUID(), NOW).isEmpty());
    }

    @Test
    void expiredRequestsAreNotListed() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID acceptor = UUID.randomUUID();
        manager.send(requester, acceptor, "Steve", "Alex", "", NOW);
        assertTrue(manager.incoming(acceptor, NOW + 30_001L).isEmpty());
        assertTrue(manager.outgoing(requester, NOW + 30_001L).isEmpty());
    }

    @Test
    void takeRemovesTheRequestBetweenRequesterAndAcceptor() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID acceptor = UUID.randomUUID();
        manager.send(requester, acceptor, "Steve", "Alex", "", NOW);

        PendingRequest taken = manager.take(requester, acceptor, NOW);
        assertNotNull(taken);
        assertTrue(manager.incoming(acceptor, NOW).isEmpty());
        assertNull(manager.take(requester, acceptor, NOW));
    }

    @Test
    void takeRefusesRequestsAddressedToAnotherPlayer() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        UUID acceptor = UUID.randomUUID();
        manager.send(requester, target, "Steve", "Alex", "", NOW);

        assertNull(manager.take(requester, acceptor, NOW));
        assertEquals(1, manager.outgoing(requester, NOW).size());
    }

    @Test
    void expiredRequestsCanNotBeTaken() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID acceptor = UUID.randomUUID();
        manager.send(requester, acceptor, "Steve", "Alex", "", NOW);

        assertNull(manager.take(requester, acceptor, NOW + 30_001L));
        assertTrue(manager.outgoing(requester, NOW + 30_001L).isEmpty());
    }

    @Test
    void cancelRemovesTheRequestToTheGivenTarget() {
        TeleportManager manager = new TeleportManager(30);
        UUID requester = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        manager.send(requester, first, "Steve", "Alex", "", NOW);

        manager.cancel(requester, first);
        assertTrue(manager.outgoing(requester, NOW).isEmpty());

        manager.send(requester, second, "Steve", "Alex", "", NOW);
        manager.cancel(requester, first);
        assertEquals(1, manager.outgoing(requester, NOW).size());
    }
}
