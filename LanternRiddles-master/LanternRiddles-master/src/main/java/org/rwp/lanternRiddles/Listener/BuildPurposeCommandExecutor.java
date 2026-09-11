package org.rwp.lanternRiddles.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.rwp.lanternRiddles.LanternRiddles;

import java.util.Random;

import static org.rwp.lanternRiddles.LanternRiddles.buildinglistList;


public class BuildPurposeCommandExecutor  implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            Random random = new Random();
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, "title @a[team=build] title '"+ buildinglistList[random.nextInt(buildinglistList.length)]+"'");
            return true; // 返回 true 表示指令处理成功，不会显示 usage
        }
    
}
