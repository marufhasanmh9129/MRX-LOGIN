package com.mrxlogin.command;

import com.mrxlogin.MRXLoginPlugin;
import com.mrxlogin.security.PasswordHasher;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Map;

public final class AuthCommand implements CommandExecutor {
    private final MRXLoginPlugin plugin;
    public AuthCommand(MRXLoginPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { plugin.messages().send(sender, "players-only"); return true; }
        if (p.hasPermission("mrxlogin.bypass")) { plugin.messages().send(p, "bypass-authenticated"); return true; }
        String name = command.getName().toLowerCase();
        try {
            switch (name) {
                case "register" -> {
                    if (!plugin.getConfig().getBoolean("registration.enabled", true)) { plugin.messages().send(p, "registration-disabled"); return true; }
                    boolean confirm=plugin.getConfig().getBoolean("registration.require-confirmation",true);
                    if ((confirm && args.length != 2) || (!confirm && args.length != 1 && args.length != 2)) { plugin.messages().send(p, confirm ? "usage-register" : "usage-register-no-confirmation"); return true; }
                    if (plugin.store().exists(p.getUniqueId())) { plugin.messages().send(p, "already-registered"); return true; }
                    if (args.length == 2 && !args[0].equals(args[1])) { plugin.messages().send(p, "password-mismatch"); return true; }
                    int min = plugin.getConfig().getInt("password.minimum-length", 8), max = plugin.getConfig().getInt("password.maximum-length", 64);
                    if (args[0].length() < min) { plugin.messages().send(p, "password-too-short", Map.of("min", String.valueOf(min))); return true; }
                    if (args[0].length() > max) { plugin.messages().send(p, "password-too-long", Map.of("max", String.valueOf(max))); return true; }
                    if (plugin.getConfig().getBoolean("password.block-weak-passwords", true) && isWeak(args[0])) { plugin.messages().send(p, "weak-password"); return true; }
                    if (plugin.getConfig().getBoolean("registration.limit-per-ip", true) && p.getAddress() != null) {
                        int limit=plugin.getConfig().getInt("registration.max-accounts-per-ip",3);
                        if(limit>0 && plugin.store().countByIp(p.getAddress().getAddress().getHostAddress())>=limit) { plugin.messages().send(p,"account-ip-limit"); return true; }
                    }
                    char[] pw = args[0].toCharArray();
                    try { plugin.store().create(p.getUniqueId(), p.getName(), PasswordHasher.hash(pw, plugin.getConfig().getInt("password.iterations", 210000)), p.getAddress() == null ? "unknown" : p.getAddress().getAddress().getHostAddress()); }
                    finally { Arrays.fill(pw, '\0'); }
                    plugin.listener().authenticate(p.getUniqueId());
                    plugin.messages().send(p, "register-success");
                }
                case "login" -> {
                    if (plugin.listener().isTemporarilyBlocked(p.getUniqueId())) { plugin.messages().send(p,"temporarily-blocked"); return true; }
                    if (args.length != 1) { plugin.messages().send(p, "usage-login"); return true; }
                    if (plugin.listener().isAuthenticated(p.getUniqueId())) { plugin.messages().send(p, "already-authenticated"); return true; }
                    var hash = plugin.store().passwordHash(p.getUniqueId());
                    if (hash.isEmpty()) { plugin.messages().send(p, "not-registered"); return true; }
                    char[] pw = args[0].toCharArray(); boolean ok;
                    try { ok = PasswordHasher.verify(pw, hash.get()); } finally { Arrays.fill(pw, '\0'); }
                    if (!ok) {
                        boolean blocked=plugin.listener().recordFailedLogin(p.getUniqueId());
                        if(blocked) { plugin.messages().send(p,"temporarily-blocked"); p.kickPlayer(plugin.messages().get("temporarily-blocked-kick")); }
                        else plugin.messages().send(p, "incorrect-password");
                        return true;
                    }
                    plugin.listener().authenticate(p.getUniqueId());
                    plugin.messages().send(p, "login-success");
                }
                case "logout" -> {
                    if (!plugin.listener().isAuthenticated(p.getUniqueId())) { plugin.messages().send(p, "not-authenticated"); return true; }
                    plugin.listener().unauthenticate(p.getUniqueId());
                    plugin.messages().send(p, "logout-success");
                }
                case "changepassword" -> {
                    if (!plugin.listener().isAuthenticated(p.getUniqueId())) { plugin.messages().send(p,"not-authenticated"); return true; }
                    if (args.length != 2) { plugin.messages().send(p, "usage-changepassword"); return true; }
                    var hash = plugin.store().passwordHash(p.getUniqueId());
                    char[] oldPw = args[0].toCharArray(); boolean valid;
                    try { valid = hash.isPresent() && PasswordHasher.verify(oldPw, hash.get()); } finally { Arrays.fill(oldPw, '\0'); }
                    if (!valid) { plugin.messages().send(p, "incorrect-password"); return true; }
                    int min = plugin.getConfig().getInt("password.minimum-length", 8), max = plugin.getConfig().getInt("password.maximum-length", 64);
                    if (args[1].length() < min) { plugin.messages().send(p, "password-too-short", Map.of("min", String.valueOf(min))); return true; }
                    if (args[1].length() > max) { plugin.messages().send(p, "password-too-long", Map.of("max", String.valueOf(max))); return true; }
                    if (plugin.getConfig().getBoolean("password.block-weak-passwords", true) && isWeak(args[1])) { plugin.messages().send(p, "weak-password"); return true; }
                    char[] pw = args[1].toCharArray();
                    try { plugin.store().updatePassword(p.getUniqueId(), PasswordHasher.hash(pw, plugin.getConfig().getInt("password.iterations", 210000))); }
                    finally { Arrays.fill(pw, '\0'); }
                    plugin.messages().send(p, "password-changed");
                }
                default -> { return false; }
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Authentication operation failed: " + ex.getClass().getSimpleName());
            plugin.messages().send(p, "internal-error");
        }
        return true;
    }

    private static boolean isWeak(String value) {
        String v=value.toLowerCase(java.util.Locale.ROOT);
        return java.util.Set.of("password","password123","12345678","123456789","qwerty123","minecraft","admin123","letmein123").contains(v)
                || v.chars().distinct().count() <= 2;
    }
}
