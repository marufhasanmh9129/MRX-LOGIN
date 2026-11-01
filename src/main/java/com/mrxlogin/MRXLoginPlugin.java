package com.mrxlogin;

import com.mrxlogin.command.AdminCommand;
import com.mrxlogin.command.AuthCommand;
import com.mrxlogin.config.ConfigManager;
import com.mrxlogin.db.AccountStore;
import com.mrxlogin.listener.AuthListener;
import com.mrxlogin.message.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class MRXLoginPlugin extends JavaPlugin {
    private AccountStore store;
    private AuthListener listener;
    private MessageManager messageManager;
    private ConfigManager configManager;

    @Override public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create plugin data folder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            configManager = new ConfigManager(this);
            configManager.load();
            messageManager = new MessageManager(this);
            messageManager.load();
            String databaseName=getConfig().getString("database.sqlite.file","accounts.db");
            File databaseFile=new File(getDataFolder(),databaseName);
            String dataPath=getDataFolder().getCanonicalPath()+File.separator;
            if(!databaseFile.getCanonicalPath().startsWith(dataPath)) throw new IllegalArgumentException("database.sqlite.file must stay inside the plugin data folder");
            store = new AccountStore(databaseFile.toPath(),getConfig().getInt("database.sqlite.busy-timeout-ms",5000));
        } catch (Exception e) {
            getLogger().severe("Startup failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        listener = new AuthListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        AuthCommand auth = new AuthCommand(this);
        for (String name : new String[]{"login", "register", "logout", "changepassword"}) {
            if (getCommand(name) != null) getCommand(name).setExecutor(auth);
        }
        AdminCommand admin = new AdminCommand(this);
        if (getCommand("ml") != null) {
            getCommand("ml").setExecutor(admin);
            getCommand("ml").setTabCompleter(admin);
        }
        getLogger().info("MRX-LOGIN enabled. Current implementation is a Paper authentication foundation; see FEATURE-STATUS.md for unsupported modules.");
    }

    public boolean reloadPluginSettings() {
        try {
            configManager.load();
            messageManager.load();
            return true;
        } catch (Exception e) {
            getLogger().severe("Reload rejected; check config.yml/messages.yml: " + e.getMessage());
            return false;
        }
    }

    @Override public void onDisable() {
        try { if (store != null) store.close(); } catch (Exception e) { getLogger().warning("Database close failed: " + e.getMessage()); }
    }
    public AccountStore store() { return store; }
    public AuthListener listener() { return listener; }
    public MessageManager messages() { return messageManager; }
}
