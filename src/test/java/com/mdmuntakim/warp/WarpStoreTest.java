package com.mdmuntakim.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class WarpStoreTest {

    @Test
    void savesAndFindsAWarp() {
        WarpStore store = new WarpStore();
        Warp warp = Warp.at("home", "Steve", "world", 1.0, 64.0, 2.0, 0.0f, 0.0f);
        store.save(warp);
        assertSame(warp, store.get("home"));
        assertTrue(store.has("home"));
        assertEquals(1, store.size());
    }

    @Test
    void unknownWarpIsNull() {
        WarpStore store = new WarpStore();
        assertNull(store.get("nowhere"));
        assertFalse(store.has("nowhere"));
    }

    @Test
    void deletesAWarp() {
        WarpStore store = new WarpStore();
        store.save(Warp.at("home", "Steve", "world", 1.0, 64.0, 2.0, 0.0f, 0.0f));
        store.delete("home");
        assertFalse(store.has("home"));
        assertEquals(0, store.size());
    }

    @Test
    void saveReplacesTheWarpWithTheSameName() {
        WarpStore store = new WarpStore();
        store.save(Warp.at("home", "Steve", "world", 1.0, 64.0, 2.0, 0.0f, 0.0f));
        Warp replacement = Warp.at("HOME", "Alex", "world", 5.0, 70.0, 6.0, 0.0f, 0.0f);
        Warp previous = store.save(replacement);
        assertSame(replacement, store.get("home"));
        assertEquals("home", previous.name());
    }

    @Test
    void listsTheSavedWarps() {
        WarpStore store = new WarpStore();
        store.save(Warp.at("base", "Steve", "world", 1.0, 64.0, 2.0, 0.0f, 0.0f));
        store.save(Warp.at("camp", "Alex", "world", 3.0, 64.0, 4.0, 0.0f, 0.0f));
        List<Warp> all = store.all();
        assertEquals(2, all.size());
        assertEquals("base", all.get(0).name());
        assertEquals("camp", all.get(1).name());
    }

    @Test
    void clearRemovesEveryWarp() {
        WarpStore store = new WarpStore();
        store.save(Warp.at("home", "Steve", "world", 1.0, 64.0, 2.0, 0.0f, 0.0f));
        store.clear();
        assertEquals(0, store.size());
        assertNull(store.get("home"));
    }
}
