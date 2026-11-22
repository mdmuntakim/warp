package com.mdmuntakim.warp;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A named warp: one fixed point in the world, remembered by name and owned by
 * the player who created it. /warp create saves the current position of the
 * sender, /warp &lt;name&gt; walks any player back to it.
 */
public record Warp(
        String name,
        String owner,
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch) {

    /** Warp names are 3 to 32 characters of lowercase letters, numbers or underscores. */
    public static final Pattern NAME = Pattern.compile("[a-z0-9_]{3,32}");

    public Warp {
        if (name == null || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Warp names must be 3 to 32 characters of lowercase letters, numbers or underscores.");
        }
        owner = owner == null ? "" : owner.toLowerCase(Locale.ROOT);
    }

    /**
     * Builds a warp at the given position. The name is normalised to lowercase
     * before it is validated, so /warp create Home and /warp create home are the
     * same warp.
     */
    public static Warp at(String name, String owner, String world, double x, double y, double z, float yaw, float pitch) {
        String key = normalize(name);
        if (!NAME.matcher(key).matches()) {
            throw new IllegalArgumentException("Warp names must be 3 to 32 characters of lowercase letters, numbers or underscores.");
        }
        return new Warp(key, owner, world, x, y, z, yaw, pitch);
    }

    /** Normalises a warp name: trimmed, lower case, underscores instead of spaces. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public String locationSummary() {
        return world + " (" + (int) x + ", " + (int) y + ", " + (int) z + ")";
    }
}
