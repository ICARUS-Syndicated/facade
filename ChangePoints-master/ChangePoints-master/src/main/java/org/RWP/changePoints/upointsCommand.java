package org.RWP.changePoints;

import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import java.util.UUID;

public class upointsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("upoints")) {
            if (args.length < 2) {
                sender.sendMessage("§c用法: /upoints <玩家> <分数>");
                return true;
            }
            else{
                String player = args[0];
                String points = args[1];
                int pointsInt = Integer.parseInt(points);
                UUID uuid =Bukkit.getOfflinePlayer(player).getUniqueId();
                LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
                User user = luckPerms.getUserManager().getUser(uuid);
                int lastPoints;
                if(user.getCachedData().getMetaData().getMetaValue("points") == null){
                    lastPoints = 0;
                }
                else{
                    lastPoints = Integer.parseInt(user.getCachedData().getMetaData().getMetaValue("points"));
                }
                int newPoints = lastPoints + pointsInt;
                MetaNode node = MetaNode.builder("points", Integer.toString(newPoints)).build();
                user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals("points")));
                user.data().add(node);
                luckPerms.getUserManager().saveUser(user);
                return true;
            }
        }
        return false; // 交由其他处理器处理
    }
}
