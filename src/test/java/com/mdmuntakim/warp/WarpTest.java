package com.mdmuntakim.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WarpTest {

    @Test
    void normalisesTheNameToLowerCase() {
        Warp warp = Warp.at("Home", "Steve", "world", 1.0, 2.0, 3.0, 0.0f, 0.0f);
        assertEquals("home", warp.name());
        assertEquals("steve", warp.owner());
    }

    @Test
    void acceptsUnderscoredNames() {
        Warp warp = Warp.at("cave_1", "steve", "world", 1.0, 2.0, 3.0, 0.0f, 0.0f);
        assertEquals("cave_1", warp.name());
    }

    @Test
    void rejectsTooShortNames() {
        assertThrows(IllegalArgumentException.class, () -> Warp.at("ab", "steve", "world", 1.0, 2.0, 3.0, 0.0f, 0.0f));
    }

    @Test
    void rejectsInvalidNames() {
        assertThrows(IllegalArgumentException.class, () -> Warp.at("Home-1", "steve", "world", 1.0, 2.0, 3.0, 0.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> Warp.at("x", "steve", "world", 1.0, 2.0, 3.0, 0.0f, 0.0f));
    }

    @Test
    void keepsTheCoordinatesOfTheWarp() {
        Warp warp = Warp.at("home", "steve", "world", 12.5, 64.25, -34.5, 90.0f, 10.0f);
        assertEquals(12.5, warp.x(), 1.0E-9);
        assertEquals(64.25, warp.y(), 1.0E-9);
        assertEquals(-34.5, warp.z(), 1.0E-9);
        assertEquals(90.0f, warp.yaw(), 1.0E-6);
        assertEquals(10.0f, warp.pitch(), 1.0E-6);
    }
}
