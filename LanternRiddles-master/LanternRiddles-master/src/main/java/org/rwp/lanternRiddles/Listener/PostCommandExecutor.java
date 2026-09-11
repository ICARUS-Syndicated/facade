package org.rwp.lanternRiddles.Listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.rwp.lanternRiddles.puzzle;

import static org.rwp.lanternRiddles.LanternRiddles.buildinglistList;
import static org.rwp.lanternRiddles.LanternRiddles.puzzles;

public class PostCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String ID = args[0];
        byte answer = Byte.parseByte(args[1]);
        for (puzzle puzzle : puzzles) {
            if (puzzle.ID.equals(ID)) {
                if (puzzle.answer == answer) {
                    sender.sendMessage("§a你成功回答了谜题！☺");
                    int packet = puzzle.pride.redpacket;
                    int luck = puzzle.pride.luck;
                    ConsoleCommandSender console = Bukkit.getConsoleSender();
                    if (packet > 0) {
                        Bukkit.dispatchCommand(console, "give " + sender.getName() + " minecraft:gold_ingot[minecraft:item_name=\"红包！\",minecraft:rarity=\"uncommon\",minecraft:enchantment_glint_override=1b,minecraft:item_model=\"rwspring:red_packet\",minecraft:use_remainder={\"id\": \"minecraft:gold_nugget\",\"count\": 1,\"components\": {\"minecraft:item_model\": \"rwspring:event_coin\",\"minecraft:rarity\": \"epic\",\"minecraft:item_name\": \"活动硬币\",\"minecraft:enchantment_glint_override\": 1b}},consumable={\"consume_seconds\": 0.3f,\"animation\": \"brush\"}] "+packet);
                    }
                    if (luck > 0) {
                        Bukkit.dispatchCommand(console, "give " + sender.getName() + " minecraft:nether_star[minecraft:item_name=\"幸运\"] "+luck);
                    }
                    
                } else {
                    sender.sendMessage("§c你回答了错误的谜题！☹");
                }
                ((Player) sender).getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
            }
        }
        return true; // 返回 true 表示指令处理成功，不会显示 usage
    }
    
}
