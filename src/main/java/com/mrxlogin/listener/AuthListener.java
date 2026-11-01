package com.mrxlogin.listener;

import com.mrxlogin.MRXLoginPlugin;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AuthListener implements Listener {
    private final MRXLoginPlugin plugin;
    private final Set<UUID> authenticated = ConcurrentHashMap.newKeySet();
    private final Map<UUID, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> blockedUntil = new ConcurrentHashMap<>();
    public AuthListener(MRXLoginPlugin plugin) { this.plugin = plugin; }
    public boolean isAuthenticated(UUID id) { return authenticated.contains(id); }
    public void authenticate(UUID id) { authenticated.add(id); failedAttempts.remove(id); blockedUntil.remove(id); }
    public void unauthenticate(UUID id) { authenticated.remove(id); }
    public boolean isTemporarilyBlocked(UUID id) { Long until=blockedUntil.get(id); if (until==null) return false; if (until<=System.currentTimeMillis()) { blockedUntil.remove(id); failedAttempts.remove(id); return false; } return true; }
    public boolean recordFailedLogin(UUID id) {
        int max=Math.max(1,plugin.getConfig().getInt("protection.max-login-attempts",5));
        int attempts=failedAttempts.computeIfAbsent(id,k->new AtomicInteger()).incrementAndGet();
        if (attempts>=max) { long seconds=Math.max(0,plugin.getConfig().getLong("protection.temporary-ban-seconds",300)); if(seconds>0) blockedUntil.put(id,System.currentTimeMillis()+seconds*1000L); return true; }
        return false;
    }
    public void clearFailedLogins(UUID id) { failedAttempts.remove(id); blockedUntil.remove(id); }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        var player=e.getPlayer(); UUID id=player.getUniqueId();
        if (isTemporarilyBlocked(id)) { player.kickPlayer(plugin.messages().get("temporarily-blocked")); return; }
        if (player.hasPermission("mrxlogin.bypass")) { authenticate(id); return; }
        try {
            if (plugin.store().exists(id)) plugin.messages().send(player,"login-required");
            else if (!plugin.getConfig().getBoolean("registration.enabled",true)) { plugin.messages().send(player,"registration-disabled"); player.kickPlayer(plugin.messages().get("registration-disabled")); return; }
            else plugin.messages().send(player,"register-required");
        } catch(Exception ex) { plugin.getLogger().severe("Could not check account state: "+ex.getClass().getSimpleName()); player.kickPlayer(plugin.messages().get("internal-error")); return; }
        if (!player.hasPlayedBefore() && plugin.getConfig().getBoolean("spawn.first-join.enabled",false)) {
            var world=plugin.getServer().getWorld(plugin.getConfig().getString("spawn.first-join.world","world"));
            if(world!=null) player.teleport(new org.bukkit.Location(world,plugin.getConfig().getDouble("spawn.first-join.x",0.5),plugin.getConfig().getDouble("spawn.first-join.y",64),plugin.getConfig().getDouble("spawn.first-join.z",0.5)));
        }
        long timeout=plugin.getConfig().getLong("protection.login-timeout-seconds",60);
        plugin.getServer().getScheduler().runTaskLater(plugin,()->{ if(player.isOnline()&&!isAuthenticated(id)) { plugin.messages().send(player,"login-timeout"); player.kickPlayer(plugin.messages().get("login-timeout-kick")); } },timeout*20L);
    }
    @EventHandler public void onQuit(PlayerQuitEvent e) {
        UUID id=e.getPlayer().getUniqueId(); authenticated.remove(id);
        if(plugin.getConfig().getBoolean("spawn.last-location.save-on-quit",true)) try { var l=e.getPlayer().getLocation(); if(plugin.store().exists(id)) plugin.store().updateLastLocation(id,l.getWorld().getName(),l.getX(),l.getY(),l.getZ()); } catch(Exception ex) { plugin.getLogger().warning("Could not save last location: "+ex.getClass().getSimpleName()); }
    }
    @EventHandler public void onMove(PlayerMoveEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.movement",true)&&e.getTo()!=null&&(e.getFrom().getBlockX()!=e.getTo().getBlockX()||e.getFrom().getBlockY()!=e.getTo().getBlockY()||e.getFrom().getBlockZ()!=e.getTo().getBlockZ())) e.setTo(e.getFrom()); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.block-break",true)) e.setCancelled(true); }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.block-place",true)) e.setCancelled(true); }
    @EventHandler public void onInventoryClick(InventoryClickEvent e) { if(e.getWhoClicked() instanceof org.bukkit.entity.Player p&&!isAuthenticated(p.getUniqueId())&&plugin.getConfig().getBoolean("protection.inventory",true)) e.setCancelled(true); }
    @EventHandler public void onInventoryDrag(InventoryDragEvent e) { if(e.getWhoClicked() instanceof org.bukkit.entity.Player p&&!isAuthenticated(p.getUniqueId())&&plugin.getConfig().getBoolean("protection.inventory",true)) e.setCancelled(true); }
    @EventHandler public void onInteract(PlayerInteractEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.interact",true)) e.setCancelled(true); }
    @EventHandler public void onDamage(EntityDamageEvent e) { if(e.getEntity() instanceof org.bukkit.entity.Player p&&!isAuthenticated(p.getUniqueId())&&plugin.getConfig().getBoolean("protection.damage",true)) e.setCancelled(true); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.inventory",true)) e.setCancelled(true); }
    @EventHandler public void onChat(AsyncPlayerChatEvent e) { if(!isAuthenticated(e.getPlayer().getUniqueId())&&plugin.getConfig().getBoolean("protection.chat",true)) { e.setCancelled(true); plugin.getServer().getScheduler().runTask(plugin,()->plugin.messages().send(e.getPlayer(),"login-required")); } }
    @EventHandler public void onCommand(PlayerCommandPreprocessEvent e) {
        if(isAuthenticated(e.getPlayer().getUniqueId())||e.getPlayer().hasPermission("mrxlogin.bypass")) return;
        String raw=e.getMessage().substring(1).split("\\s+",2)[0].toLowerCase(Locale.ROOT); if(raw.contains(":")) raw=raw.substring(raw.indexOf(':')+1);
        String commandName=raw;
        if(!plugin.getConfig().getStringList("protection.commands-allowlist").stream().map(s->s.toLowerCase(Locale.ROOT)).anyMatch(commandName::equals)) { e.setCancelled(true); plugin.getServer().getScheduler().runTask(plugin,()->plugin.messages().send(e.getPlayer(),"login-required")); }
    }
}
