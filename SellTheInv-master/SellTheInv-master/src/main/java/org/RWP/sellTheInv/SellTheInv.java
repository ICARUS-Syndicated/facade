package org.RWP.sellTheInv;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SellTheInv extends JavaPlugin {
    private static FileConfiguration config;
    public static FileConfiguration getconfig(){
        return config;
    }
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getLogger().info("配置文件加载完成！");
        this.getCommand("sellbag").setExecutor(new SellCommand());
        getServer().getPluginManager().registerEvents(new joinGameListener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
