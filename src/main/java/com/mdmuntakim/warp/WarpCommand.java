package com.mdmuntakim.warp;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * /warp &lt;name&gt;, /warp create &lt;name&gt;, /warp del &lt;name&gt;, /warp list and
 * the /tpa request family: /tpa &lt;player&gt; [reason], /tpa accept &lt;player&gt;,
 * /tpa deny &lt;player&gt; and /tpa cancel &lt;player&gt;.
 */
public final class WarpCommand implements CommandExecutor, TabCompleter {

    private static final NamedTextColor ACCENT = NamedTextColor.GOLD;
    private static final NamedTextColor MUTED = NamedTextColor.GRAY;
    private static final NamedTextColor GREEN = NamedTextColor.GREEN;
    private static final NamedTextColor RED = NamedTextColor.RED;
    private static final NamedTextColor AQUA = NamedTextColor.AQUA;

    private final WarpPlugin plugin;

    public WarpCommand(WarpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tpa")) {
            return onTpa(sender, label, args);
        }
        return onWarp(sender, label, args);
    }

    // ------------------------------------------------------------------
    // /warp
    // ------------------------------------------------------------------

    private boolean onWarp(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            return help(sender, label);
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> {
                return create(sender, label, args);
            }
            case "del" -> {
                return del(sender, label, args);
            }
            case "list" -> {
                return list(sender, label);
            }
            case "help" -> {
                return help(sender, label);
            }
            default -> {
                return warp(sender, label, args);
            }
        }
    }

    private boolean warp(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.use")) {
            send(sender, "You do not have permission to use warps.", RED);
            return true;
        }
        if (!(sender instanceof Player player)) {
            send(sender, "Only players can use warps.", RED);
            return true;
        }
        String name = Warp.normalize(args[0]);
        Warp warp = plugin.warps().get(name);
        if (warp == null) {
            send(sender, "Unknown warp: " + name + ". Use /" + label + " list.", RED);
            return true;
        }
        Location target = new Location(Bukkit.getWorld(warp.world()), warp.x(), warp.y(), warp.z(), warp.yaw(), warp.pitch());
        if (target.getWorld() == null) {
            send(sender, "The warp " + name + " is not on this server.", RED);
            return true;
        }
        player.teleport(target);
        send(sender, "Teleported to warp " + name + ".", GREEN);
        return true;
    }

    private boolean create(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.create")) {
            send(sender, "You do not have permission to create warps.", RED);
            return true;
        }
        if (args.length < 2) {
            send(sender, "Usage: /" + label + " create <name>", RED);
            return true;
        }
        if (!(sender instanceof Player player)) {
            send(sender, "Only players can create warps.", RED);
            return true;
        }
        Location location = player.getLocation();
        Warp warp;
        try {
            warp = Warp.at(args[1], player.getName(), player.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        } catch (IllegalArgumentException exception) {
            send(sender, exception.getMessage(), RED);
            return true;
        }
        plugin.warps().save(warp);
        plugin.saveWarps();
        send(sender, "Created warp " + warp.name() + " at " + warp.locationSummary() + ".", GREEN);
        return true;
    }

    private boolean del(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.delete")) {
            send(sender, "You do not have permission to delete warps.", RED);
            return true;
        }
        if (args.length < 2) {
            send(sender, "Usage: /" + label + " del <name>", RED);
            return true;
        }
        String name = Warp.normalize(args[1]);
        if (!plugin.warps().has(name)) {
            send(sender, "Unknown warp: " + name + ".", RED);
            return true;
        }
        plugin.warps().delete(name);
        plugin.saveWarps();
        send(sender, "Deleted warp " + name + ".", GREEN);
        return true;
    }

    private boolean list(CommandSender sender, String label) {
        if (!sender.hasPermission("warp.command")) {
            send(sender, "You do not have permission to list warps.", RED);
            return true;
        }
        List<Warp> warps = plugin.warps().all();
        if (warps.isEmpty()) {
            send(sender, "No warps have been created yet.", MUTED);
            return true;
        }
        send(sender, "Warps (" + warps.size() + ")", ACCENT);
        for (Warp warp : warps) {
            send(sender, "  " + warp.name() + " - " + warp.owner() + " (" + warp.locationSummary() + ")", MUTED);
        }
        return true;
    }

    private boolean help(CommandSender sender, String label) {
        send(sender, "Warp commands", ACCENT);
        send(sender, "  /" + label + " <name> - teleport to a warp", MUTED);
        send(sender, "  /" + label + " create <name> - save the current position as a warp", MUTED);
        send(sender, "  /" + label + " del <name> - delete a warp", MUTED);
        send(sender, "  /" + label + " list - list the saved warps", MUTED);
        send(sender, "Teleport requests:", ACCENT);
        send(sender, "  /tpa <player> [reason] - ask a player to teleport to you", MUTED);
        send(sender, "  /tpa accept <player> - accept a request", MUTED);
        send(sender, "  /tpa deny <player> - deny a request", MUTED);
        send(sender, "  /tpa cancel <player> - cancel your own request", MUTED);
        return true;
    }

    // ------------------------------------------------------------------
    // /tpa
    // ------------------------------------------------------------------

    private boolean onTpa(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "Usage: /" + label + " <player> [reason] | /" + label
                    + " accept <player> | /" + label + " deny <player> | /" + label + " cancel <player>", RED);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "accept" -> {
                return tpaAccept(sender, label, args);
            }
            case "deny" -> {
                return tpaDeny(sender, label, args);
            }
            case "cancel" -> {
                return tpaCancel(sender, label, args);
            }
            case "help" -> {
                return help(sender, label);
            }
            default -> {
                return tpaRequest(sender, label, args);
            }
        }
    }

    private boolean tpaRequest(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.tpa")) {
            send(sender, "You do not have permission to send teleport requests.", RED);
            return true;
        }
        if (!(sender instanceof Player requester)) {
            send(sender, "Only players can send teleport requests.", RED);
            return true;
        }
        if (args.length < 1) {
            send(sender, "Usage: /" + label + " <player> [reason]", RED);
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            send(sender, "Player not found: " + args[0], RED);
            return true;
        }
        if (target.getUniqueId().equals(requester.getUniqueId())) {
            send(sender, "You cannot send a teleport request to yourself.", RED);
            return true;
        }
        if (!plugin.teleport().outgoing(requester.getUniqueId(), System.currentTimeMillis()).isEmpty()) {
            send(sender, "You already have a pending request. Cancel it with /" + label + " cancel " + target.getName() + ".", RED);
            return true;
        }
        String reason = args.length > 1 ? args[1] : "";
        plugin.teleport().send(requester.getUniqueId(), target.getUniqueId(), requester.getName(),
                target.getName(), reason, System.currentTimeMillis());
        target.sendMessage(Component.text(requester.getName() + " wants to teleport to you"
                        + (reason.isEmpty() ? "" : " (" + reason + ")") + ". Use /" + label + " accept "
                        + requester.getName() + " to accept.", AQUA));
        requester.sendMessage(Component.text("Teleport request sent to " + target.getName() + ".", GREEN));
        return true;
    }

    private boolean tpaAccept(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.tpa")) {
            send(sender, "You do not have permission to accept teleport requests.", RED);
            return true;
        }
        if (!(sender instanceof Player acceptor)) {
            send(sender, "Only players can accept teleport requests.", RED);
            return true;
        }
        if (args.length < 2) {
            send(sender, "Usage: /" + label + " accept <player>", RED);
            return true;
        }
        Player requester = Bukkit.getPlayerExact(args[1]);
        if (requester == null) {
            send(sender, "Player not found: " + args[1], RED);
            return true;
        }
        PendingRequest request = plugin.teleport().take(requester.getUniqueId(), acceptor.getUniqueId(), System.currentTimeMillis());
        if (request == null) {
            send(acceptor, "No pending teleport request from " + requester.getName() + ".", RED);
            return true;
        }
        requester.teleport(acceptor.getLocation());
        requester.sendMessage(Component.text("You were teleported to " + acceptor.getName() + ".", GREEN));
        acceptor.sendMessage(Component.text("Teleport request of " + requester.getName() + " accepted.", GREEN));
        return true;
    }

    private boolean tpaDeny(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.tpa")) {
            send(sender, "You do not have permission to deny teleport requests.", RED);
            return true;
        }
        if (!(sender instanceof Player acceptor)) {
            send(sender, "Only players can deny teleport requests.", RED);
            return true;
        }
        if (args.length < 2) {
            send(sender, "Usage: /" + label + " deny <player>", RED);
            return true;
        }
        Player requester = Bukkit.getPlayerExact(args[1]);
        if (requester == null) {
            send(sender, "Player not found: " + args[1], RED);
            return true;
        }
        PendingRequest request = plugin.teleport().take(requester.getUniqueId(), acceptor.getUniqueId(), System.currentTimeMillis());
        if (request == null) {
            send(acceptor, "No pending teleport request from " + requester.getName() + ".", RED);
            return true;
        }
        requester.sendMessage(Component.text("Your teleport request to " + acceptor.getName() + " was denied.", RED));
        return true;
    }

    private boolean tpaCancel(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("warp.tpa")) {
            send(sender, "You do not have permission to cancel teleport requests.", RED);
            return true;
        }
        if (!(sender instanceof Player requester)) {
            send(sender, "Only players can cancel teleport requests.", RED);
            return true;
        }
        if (args.length < 2) {
            send(sender, "Usage: /" + label + " cancel <player>", RED);
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            send(sender, "Player not found: " + args[1], RED);
            return true;
        }
        if (plugin.teleport().cancel(requester.getUniqueId(), target.getUniqueId())) {
            send(sender, "Cancelled your teleport request to " + target.getName() + ".", GREEN);
        } else {
            send(sender, "You had no pending teleport request to " + target.getName() + ".", MUTED);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private void send(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(Component.text(message, color));
    }

    private void send(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    // ------------------------------------------------------------------
    // tab completion
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("tpa")) {
            return tpaTab(sender, args);
        }
        return warpTab(sender, args);
    }

    private List<String> tpaTab(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("accept") || sub.equals("deny") || sub.equals("cancel") || sub.equals("help")) {
            if (args.length < 2) {
                return playerNames();
            }
            return filter(playerNames(), args[1]);
        }
        return filter(playerNames(), args[0]);
    }

    private List<String> warpTab(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("create")) {
            return List.of();
        }
        if (sub.equals("del")) {
            List<String> names = plugin.warps().all().stream().map(Warp::name).sorted().toList();
            if (args.length < 2) {
                return names;
            }
            return filter(names, args[1]);
        }
        if (sub.equals("list") || sub.equals("help")) {
            return List.of();
        }
        List<String> names = plugin.warps().all().stream().map(Warp::name).sorted().toList();
        return filter(names, args[0]);
    }

    private List<String> playerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .sorted()
                .toList();
    }
}
