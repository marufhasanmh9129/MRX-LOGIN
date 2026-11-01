package com.mrxlogin.message;

import com.mrxlogin.MRXLoginPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.Map;

/** All user-facing plugin text is loaded from messages.yml. */
public final class MessageManager {
    private final MRXLoginPlugin plugin;
    private YamlConfiguration messages;
    public MessageManager(MRXLoginPlugin plugin) { this.plugin = plugin; }

    public void load() throws java.io.IOException, org.bukkit.configuration.InvalidConfigurationException {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        YamlConfiguration loaded = new YamlConfiguration();
        loaded.load(file);
        if (loaded.getConfigurationSection("messages") == null) throw new IllegalArgumentException("messages.yml must contain a 'messages' section");
        messages = loaded;
    }

    public String get(String key) { return get(key, Map.of()); }
    public String get(String key, Map<String, String> placeholders) {
        String prefix = messages == null ? "" : messages.getString("prefix", "&8[&bMRX-LOGIN&8] ");
        String value = messages == null ? null : messages.getString("messages." + key);
        if (value == null) value = "&cMissing message: " + key;
        if (messages != null && messages.getBoolean("settings.prefix-enabled", true)) value = prefix + value;
        for (var entry : placeholders.entrySet()) value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        return ChatColor.translateAlternateColorCodes('&', value);
    }
    public void send(CommandSender sender, String key) { sender.sendMessage(get(key)); }
    public void send(CommandSender sender, String key, Map<String, String> placeholders) { sender.sendMessage(get(key, placeholders)); }
}
