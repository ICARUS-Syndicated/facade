package org.RWP.rCON;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class RCON extends JavaPlugin {
    public static Logger logger;
    public static FileConfiguration config;
    public static List<ServerInfo> serverList;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        logger = this.getLogger();
        config = getConfig();
        serverList = loadServers();
        logger.info("RCON插件已加载");
        this.getCommand("rcon").setExecutor(new RCONCommand());
    }

    @Override
    public void onDisable() {

    }
    public List<ServerInfo> loadServers() {
        List<ServerInfo> serverList = new ArrayList<>();

        // 获取配置中的服务器列表部分
        List<Map<?, ?>> servers = RCON.config.getMapList("servers");

        for (Map<?, ?> serverMap : servers) {
            try {
                String name = (String) serverMap.get("name");
                String ip = (String) serverMap.get("ip");

                // 处理不同类型的端口值
                int port;
                Object portObj = serverMap.get("port");
                if (portObj instanceof Integer) {
                    port = (Integer) portObj;
                } else if (portObj instanceof String) {
                    port = Integer.parseInt((String) portObj);
                } else {
                    throw new IllegalArgumentException("无效的端口类型");
                }

                String password = (String) serverMap.get("pwd");

                serverList.add(new ServerInfo(name, ip, port, password));
            } catch (Exception e) {
                RCON.logger.warning("解析服务器配置时出错: " + e.getMessage());
            }
        }

        return serverList;
    }

}
