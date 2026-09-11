package rwp.annieawa.recipeaddon;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import rwp.annieawa.recipeaddon.command.RecipeCommand;

import rwp.annieawa.recipeaddon.config.RecipeLoader;

import java.util.logging.Level;

public final class RecipeAddon extends JavaPlugin {

    private static RecipeAddon instance;
    private RecipeLoader recipeLoader;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("配方插件已启动，正在加载自定义配方...");

        saveDefaultConfig();
        
        Configuration configuration  = getConfig();
        // 初始化配方加载器
        this.recipeLoader = new RecipeLoader(this,configuration.getString("namespace"),configuration.getString("datapackname"));

        // 注册命令
        var command = getCommand("recipeaddon");
        if (command != null) {
            command.setExecutor(new RecipeCommand(this));
        }

        // 初次加载所有配方
        getServer().getPluginManager().registerEvents(new rwp.annieawa.recipeaddon.listener.CraftListener(this), this);
        reloadRecipes();
    }

    @Override
    public void onDisable() {
        if (recipeLoader != null) {
            recipeLoader.removeAllPluginRecipes();
        }
        getLogger().info("配方插件已卸载");
    }

    /**
     * 重载所有配方（先清理再加载）
     */
    public void reloadRecipes() {
        try {
            long start = System.currentTimeMillis();
            recipeLoader.removeAllPluginRecipes();  // 先移除所有本插件配方
            int count = recipeLoader.loadAllRecipes(); // 重新加载
            long time = System.currentTimeMillis() - start;
            getLogger().info("成功重载 " + count + " 个自定义配方，耗时 " + time + "ms");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "重载配方时发生错误", e);
        }
    }
    public RecipeLoader getRecipeLoader() {
        return recipeLoader;
    }
    public static RecipeAddon getInstance() {
        return instance;
    }
}