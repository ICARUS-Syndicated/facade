package org.rwp.giveTask;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class ApplyTask implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("请输入任务ID");
            return true;
        }
        else{
            if(sender instanceof Player){
                Player player = (Player) sender;
                if(hasPlayerMeta(player,"todaytask")){
                    player.sendMessage("你今天已经申请过任务了");
                }
                else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plk changeItem "+args[0]+" "+player.getName());
                    addPlayerMeta(player, "todaytask", "1");
                    player.sendMessage("成功领取任务");
                }
            }
        }
        return true;
    }
    public static void addPlayerMeta(Player player, String key, String newValue) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        // 3. 添加新节点
        MetaNode newNode = 
                MetaNode.builder(key, newValue)
                        .expiry(getNext4amExpiry())
                        .build();
        user.data().add(newNode);

        // 4. 保存用户
        luckPerms.getUserManager().saveUser(user);
    }
    public static boolean hasPlayerMeta(Player player, String key) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;

        // 遍历玩家直接持有的所有节点，筛选元数据节点并匹配键名（忽略上下文）
        return user.data().toCollection().stream()
                .filter(NodeType.META::matches)
                .map(NodeType.META::cast)
                .anyMatch(node -> node.getMetaKey().equals(key));
    }
    public static Instant getNext4amExpiry() {
        // 获取当前系统时区的时间
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        // 构建今天的凌晨4点
        ZonedDateTime today4am = now.withHour(4).withMinute(0).withSecond(0).withNano(0);

        // 判断当前时间是否在今天的凌晨4点之后（包括等于）
        ZonedDateTime target;
        if (now.isBefore(today4am)) {
            // 当前时间早于今天4点，目标为今天4点
            target = today4am;
        } else {
            // 当前时间晚于或等于今天4点，目标为明天4点
            target = today4am.plusDays(1);
        }

        // 转换为Instant（UTC时间）
        return target.toInstant();
    }
}
