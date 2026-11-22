package com.mdmuntakim.warp;

import java.io.File;
import java.io.IOException;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Warp: named warps (/warp) and player teleport requests (/tpa) for Paper 26.3.
 */
public final class WarpPlugin extends JavaPlugin {

    private WarpStore warps = new WarpStore();
    private TeleportManager teleport = new TeleportManager(30);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPlugin();

        PluginCommand warpCommand = getCommand("warp");
        if (warpCommand != null) {
            warpCommand.setExecutor(new WarpCommand(this));
            warpCommand.setTabCompleter(new WarpCommand(this));
        }
        PluginCommand tpaCommand = getCommand("tpa");
        if (tpaCommand != null) {
            tpaCommand.setExecutor(new WarpCommand(this));
            tpaCommand.setTabCompleter(new WarpCommand(this));
        }

        logSummary();
    }

    @Override
    public void onDisable() {
        teleport.clear();
    }

    /** Reloads the configuration, the teleport request lifetime and the warps. */
    public void reloadPlugin() {
        teleport = new TeleportManager(getConfig().getLong("teleport.request-expiry-seconds", 30));
        loadWarps();
    }

    public WarpStore warps() {
        return warps;
    }

    public TeleportManager teleport() {
        return teleport;
    }

    public void saveWarps() {
        File file = new File(getDataFolder(), "warps.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        for (Warp warp : warps.all()) {
            yaml.set(warp.name() + ".owner", warp.owner());
            yaml.set(warp.name() + ".world", warp.world());
            yaml.set(warp.name() + ".x", warp.x());
            yaml.set(warp.name() + ".y", warp.y());
            yaml.set(warp.name() + ".z", warp.z());
            yaml.set(warp.name() + ".yaw", warp.yaw());
            yaml.set(warp.name() + ".pitch", warp.pitch());
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            getLogger().warning("Could not save the warps to " + file + ": " + exception.getMessage());
        }
    }

    private void loadWarps() {
        File file = new File(getDataFolder(), "warps.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            String owner = yaml.getString(key + ".owner");
            String world = yaml.getString(key + ".world");
            double x = yaml.getDouble(key + ".x");
            double y = yaml.getDouble(key + ".y");
            double z = yaml.getDouble(key + ".z");
            float yaw = (float) yaml.getDouble(key + ".yaw");
            float pitch = (float) yaml.getDouble(key + ".pitch");
            if (world == null) {
                continue;
            }
            try {
                warps.save(Warp.at(key, owner, world, x, y, z, yaw, pitch));
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Skipping warp " + key + ": " + exception.getMessage());
            }
        }
    }

    private void logSummary() {
        getLogger().info("Warp store: " + warps.size() + " warps.");
        getLogger().info("Pending teleport requests: " + teleport().pendingCount(System.currentTimeMillis()) + ".");
    }
}
