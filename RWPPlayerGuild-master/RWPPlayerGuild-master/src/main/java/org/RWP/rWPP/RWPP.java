package org.RWP.rWPP;

import org.bukkit.plugin.java.JavaPlugin;

public final class RWPP extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new guildcreatelistener(), this);
        getServer().getPluginManager().registerEvents(new guildUpdateListener(), this);
        getServer().getPluginManager().registerEvents(new guildDeleteListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
