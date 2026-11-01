package com.mrxlogin.command;

import com.mrxlogin.MRXLoginPlugin;
import org.bukkit.command.*;
import java.util.List;
import java.util.Map;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final MRXLoginPlugin plugin;
    public AdminCommand(MRXLoginPlugin plugin) { this.plugin=plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length==0||args[0].equalsIgnoreCase("help")) { plugin.messages().send(sender,"admin-help"); return true; }
        if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("mrxlogin.admin.reload")) { plugin.messages().send(sender,"no-permission"); return true; }
            plugin.messages().send(sender,plugin.reloadPluginSettings()?"reload-success":"reload-failed"); return true;
        }
        if(!sender.hasPermission("mrxlogin.admin")) { plugin.messages().send(sender,"no-permission"); return true; }
        switch(args[0].toLowerCase()) {
            case "version","info" -> plugin.messages().send(sender,"admin-version",Map.of("version",plugin.getDescription().getVersion()));
            case "status" -> plugin.messages().send(sender,"admin-status",Map.of("online",String.valueOf(plugin.getServer().getOnlinePlayers().size()),"storage","SQLite","mode",plugin.getConfig().getString("authentication.mode","AUTHME")));
            case "debug" -> plugin.messages().send(sender,plugin.getConfig().getBoolean("debug.enabled",false)?"debug-enabled":"debug-disabled");
            case "admin" -> accountAdmin(sender,args);
            default -> plugin.messages().send(sender,"admin-unknown-subcommand");
        }
        return true;
    }
    private void accountAdmin(CommandSender sender,String[] args) {
        if(!sender.hasPermission("mrxlogin.admin.accounts")) { plugin.messages().send(sender,"no-permission"); return; }
        if(args.length==3&&args[1].equalsIgnoreCase("unregister")) {
            try { boolean removed=plugin.store().deleteByName(args[2]); plugin.messages().send(sender,removed?"admin-account-removed":"admin-account-not-found",Map.of("player",args[2])); }
            catch(Exception ex) { plugin.getLogger().warning("Admin account removal failed: "+ex.getClass().getSimpleName()); plugin.messages().send(sender,"internal-error"); }
        } else plugin.messages().send(sender,"admin-account-usage");
    }
    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args) {
        if(args.length==1) return List.of("help","reload","info","version","status","debug","admin").stream().filter(s->s.startsWith(args[0].toLowerCase())).toList();
        if(args.length==2&&args[0].equalsIgnoreCase("admin")) return List.of("unregister").stream().filter(s->s.startsWith(args[1].toLowerCase())).toList();
        return List.of();
    }
}
