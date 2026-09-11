package org.rwp.lanternRiddles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.rwp.lanternRiddles.Listener.BuildPurposeCommandExecutor;
import org.rwp.lanternRiddles.Listener.PlayerInteractEventListener;
import org.rwp.lanternRiddles.Listener.PostCommandExecutor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class LanternRiddles extends JavaPlugin {

    public static puzzle[] puzzles;
    public static String[] buildinglistList;
    @Override
    public void onEnable() {
        // Plugin startup logic

        File puzzlesFile = new File(getDataFolder(), "Puzzles.yml");
        if (!puzzlesFile.exists()) {
            // 如果文件不存在，从资源文件夹复制
            saveResource("Puzzles.yml", false);
        }
        File buildinglist = new File(getDataFolder(), "buildinglist.yml");
        if (!buildinglist.exists()) {
            saveResource("buildinglist.yml", false);
        }
        

        FileConfiguration riddlesFile = YamlConfiguration.loadConfiguration(puzzlesFile);
        List<Map<?, ?>> puzzlesList = riddlesFile.getMapList("puzzles");
        puzzles = new puzzle[puzzlesList.size()];
        
        for (int i = 0; i < puzzlesList.size(); i++) {
            Map<?, ?> puzzleMap = puzzlesList.get(i);
            String question = (String) puzzleMap.get("question");
            List<String> optionsList = (List<String>) puzzleMap.get("options");
            if (optionsList == null) {
                getLogger().warning("谜题 " + question + " 缺少选项");
                continue;
            }
            String[] options = optionsList.toArray(new String[0]);
            byte answerIndex = puzzleMap.containsKey("answer") ? Byte.parseByte(puzzleMap.get("answer").toString()) : 0;
            String id = (String) puzzleMap.get("id");
            boolean selected = puzzleMap.containsKey("selected") ? Boolean.parseBoolean(puzzleMap.get("selected").toString()) : false;
            
            // 处理 pride 部分
            Map<?, ?> prideMap = (Map<?, ?>) puzzleMap.get("pride");
            int luck = 0;
            int coin = 0;
            if (prideMap != null) {
                luck = prideMap.containsKey("luck") ? Integer.parseInt(prideMap.get("luck").toString()) : 0;
                coin = prideMap.containsKey("redpacket") ? Integer.parseInt(prideMap.get("redpacket").toString()) : 0;
            }
            Pride pride = new Pride(luck, coin);
            
            puzzles[i] = new puzzle(question, options, (byte) answerIndex, id, selected, pride);
        }
        
        FileConfiguration buildinglistFile = YamlConfiguration.loadConfiguration(buildinglist);
        buildinglistList = buildinglistFile.getStringList("buildinglist").toArray(new String[0]);
        
        getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), this);
        getCommand("post").setExecutor(new PostCommandExecutor());
        getCommand("buildpurpose").setExecutor(new BuildPurposeCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // 确保数据文件夹存在
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File puzzlesFile = new File(getDataFolder(), "Puzzles.yml");
        FileConfiguration config = new YamlConfiguration();

        // 转换 puzzles 数组为 YML 结构
        List<Map<String, Object>> puzzlesList = new ArrayList<>();

        for (puzzle p : puzzles) {
            if (p == null) continue; // 跳过 null 元素

            Map<String, Object> puzzleMap = new HashMap<>();
            puzzleMap.put("question", p.question);
            puzzleMap.put("options", Arrays.asList(p.options)); // 数组转 List
            puzzleMap.put("answer", p.answer);
            puzzleMap.put("id", p.ID);
            puzzleMap.put("selected", p.selected);

            // 处理嵌套的 pride 对象
            Map<String, Object> prideMap = new HashMap<>();
            if (p.pride != null) {
                prideMap.put("luck", p.pride.luck);
                prideMap.put("redpacket", p.pride.redpacket); // 对应配置文件中的 redpacket 字段
            } else {
                prideMap.put("luck", 0);
                prideMap.put("redpacket", 0);
            }
            puzzleMap.put("pride", prideMap);

            puzzlesList.add(puzzleMap);
        }

        // 将转换后的数据设置到配置文件
        config.set("puzzles", puzzlesList);

        // 保存配置文件
        try {
            config.save(puzzlesFile);
            getLogger().info("Puzzles.yml 保存成功！");
        } catch (IOException e) {
            getLogger().severe("保存 Puzzles.yml 时出错：" + e.getMessage());
            e.printStackTrace();
        }
    }
}