package com.mrxlogin.config;

import com.mrxlogin.MRXLoginPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;

/** Loads and validates the main configuration without silently accepting unsafe values. */
public final class ConfigManager {
    private final MRXLoginPlugin plugin;
    public ConfigManager(MRXLoginPlugin plugin) { this.plugin = plugin; }

    public void load() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) plugin.saveDefaultConfig();
        plugin.reloadConfig();
        validate(plugin.getConfig());
    }

    private void validate(FileConfiguration c) {
        int min=c.getInt("password.minimum-length",8), max=c.getInt("password.maximum-length",64);
        if(min<8||min>128) throw new IllegalArgumentException("password.minimum-length must be between 8 and 128");
        if(max<min||max>128) throw new IllegalArgumentException("password.maximum-length must be >= minimum-length and <= 128");
        int iterations=c.getInt("password.iterations",210000);
        if(iterations<100000||iterations>2000000) throw new IllegalArgumentException("password.iterations must be between 100000 and 2000000");
        if(c.getLong("protection.login-timeout-seconds",60)<10) throw new IllegalArgumentException("protection.login-timeout-seconds must be at least 10");
        if(c.getInt("protection.max-login-attempts",5)<1) throw new IllegalArgumentException("protection.max-login-attempts must be at least 1");
        if(c.getLong("protection.temporary-ban-seconds",300)<0) throw new IllegalArgumentException("protection.temporary-ban-seconds cannot be negative");
        if(c.getInt("registration.max-accounts-per-ip",3)<1) throw new IllegalArgumentException("registration.max-accounts-per-ip must be at least 1");
        String mode=c.getString("authentication.mode","AUTHME");
        if(!"AUTHME".equalsIgnoreCase(mode)) throw new IllegalArgumentException("Only AUTHME mode is implemented in this Paper build. GUI pre-backend authentication requires a separate proxy/limbo module.");
        String database=c.getString("database.type","SQLITE");
        if(!"SQLITE".equalsIgnoreCase(database)) throw new IllegalArgumentException("Only SQLITE storage is implemented in this build.");
        requireDisabled(c,"authentication.premium.enabled","Premium authentication module is not included");
        requireDisabled(c,"authentication.premium.auto-login","Trusted premium/proxy identity adapter is not included");
        requireDisabled(c,"authentication.session.enabled","Session login module is not included");
        requireDisabled(c,"authentication.bedrock.enabled","Floodgate integration is not included");
        requireDisabled(c,"authentication.two-factor.enabled","TOTP 2FA module is not included");
        requireDisabled(c,"email.enabled","Email verification/recovery workflow is not included; provider helper methods alone do not provide this feature");
        requireDisabled(c,"discord.enabled","Discord verification module is not included");
        requireDisabled(c,"antibot.enabled","AntiBot module is not included");
        requireDisabled(c,"protection.captcha.enabled","Captcha module is not included");
        requireDisabled(c,"proxy.enabled","Velocity/BungeeCord proxy module is not included");
        requireDisabled(c,"folia.enabled","Folia scheduler adapter is not included");
        requireDisabled(c,"spawn.fixes.prevent-void-spawn","Void-spawn fix module is not included");
        requireDisabled(c,"spawn.fixes.prevent-portal-loop","Portal-loop fix module is not included");
        requireDisabled(c,"backup.enabled","Backup/restore module is not included");
        requireDisabled(c,"migration.enabled","Database migration module is not included");
        requireDisabled(c,"spawn.last-location.restore","Last-location restore is not included; only coordinate saving is available");
    }
    private void requireDisabled(FileConfiguration c,String path,String reason) {
        if(c.getBoolean(path,false)) throw new IllegalArgumentException(path+" cannot be enabled: "+reason+".");
    }
}
