package org.rwp.aprilFool;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class AprilFool extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        saveDefaultConfig();
        ConfigurationSection config = getConfig();
        this.getServer().getPluginManager().registerEvents(new TNTListener(), this);
        this.getServer().getPluginManager().registerEvents(new MoveListener(
                Short.parseShort(String.valueOf(config.get("replace_distance"))),
                Long.parseLong(String.valueOf(config.get("cold_down")))),
                this);
        this.getServer().getPluginManager().registerEvents(new PlaceBlockListener(), this);


        Bukkit.getScheduler().runTaskTimerAsynchronously(AprilFool.this, () -> {
            long now = System.currentTimeMillis();

            MoveListener.replaced.entrySet().removeIf(entry -> {
                if (entry.getValue().time <= now - 3000) {
                    return true; // 删除
                }
                return false;
            });
        }, 0, 6000);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
