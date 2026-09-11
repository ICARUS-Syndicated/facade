package org.rwp.giveTask;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GiveTask extends JavaPlugin {
    public static Map<String, List<Integer>> tasklist = new ConcurrentHashMap<>();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        tasklist.put("normal", config.getIntegerList("normal"));
        tasklist.put("advanced", config.getIntegerList("advanced"));
        tasklist.put("rare", config.getIntegerList("rare"));
        tasklist.put("rarer", config.getIntegerList("rarer"));
        tasklist.put("epic", config.getIntegerList("epic"));
        tasklist.put("mythic", config.getIntegerList("mythic"));
        
        getCommand("givetask").setExecutor(new TaskGiver());
        getCommand("apply").setExecutor(new ApplyTask());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
