package org.RWP.changePoints;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChangePoints extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("upoints").setExecutor(new upointsCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
