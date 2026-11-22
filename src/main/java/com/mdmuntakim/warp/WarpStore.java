package com.mdmuntakim.warp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In memory store of the named warps. The plugin loads it from warps.yml on
 * start and writes it back on every change.
 */
public final class WarpStore {

    private final Map<String, Warp> warps = new LinkedHashMap<>();

    /**
     * Stores a warp under its name and returns the warp it replaces, if any.
     */
    public Warp save(Warp warp) {
        if (warp == null) {
            throw new IllegalArgumentException("warp");
        }
        return warps.put(warp.name(), warp);
    }

    public Warp get(String name) {
        return warps.get(Warp.normalize(name));
    }

    public boolean has(String name) {
        return get(name) != null;
    }

    public void delete(String name) {
        warps.remove(Warp.normalize(name));
    }

    public List<Warp> all() {
        return List.copyOf(warps.values());
    }

    public int size() {
        return warps.size();
    }

    public void clear() {
        warps.clear();
    }
}
