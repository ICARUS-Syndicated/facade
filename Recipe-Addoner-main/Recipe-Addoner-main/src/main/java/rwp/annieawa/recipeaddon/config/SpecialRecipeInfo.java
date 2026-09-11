package rwp.annieawa.recipeaddon.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection; /**
 * 配方加载器：扫描 recipes 目录下的所有 yml 文件并注册配方
 */
public class SpecialRecipeInfo {
    public Material targetMaterial;
    public boolean copyInput;
    public ConfigurationSection components;
    public Character sourceCharacter; // 用于有序配方（pattern 中的字符）
    public Integer sourceSlot;        // 用于任意配方（工作台槽位索引 0-8）
}