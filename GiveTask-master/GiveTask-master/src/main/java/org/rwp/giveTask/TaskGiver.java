package org.rwp.giveTask;

import com.handy.playertask.api.PlayerTaskApi;
import com.handy.playertask.entity.TaskList;
import com.handy.playertask.entity.TaskRewards;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaskGiver implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof BlockCommandSender) {
            BlockCommandSender cmdBlock = (BlockCommandSender) sender;
            Block block = cmdBlock.getBlock();
            
            //获取最近的玩家
            Location blockLoc = block.getLocation();
            World world = block.getWorld();
            Player nearest = null;
            double minDistanceSq = Double.MAX_VALUE;
            for (Player player : Bukkit.getOnlinePlayers()) {
                // 只考虑与方块在同一世界的玩家
                if (player.getWorld().equals(world)) {
                    double distanceSq = player.getLocation().distanceSquared(blockLoc);
                    if (distanceSq < minDistanceSq) {
                        minDistanceSq = distanceSq;
                        nearest = player;
                    }
                }
            }
            //获取最近的玩家
            
            if (nearest != null) {
                if(!ApplyTask.hasPlayerMeta(nearest,"gettodaytask-"+args[0])){
                    Random random = new Random();
                    int ID = GiveTask.tasklist.get(args[0]).get(random.nextInt(GiveTask.tasklist.get(args[0]).size()));//获取随机任务的ID

                    //获取任务
                    PlayerTaskApi playerTaskApi = PlayerTaskApi.getInstance();
                    TaskList task = playerTaskApi.findDetailByTaskId(ID);
                    //获取任务

                    //构建奖励的文本组件
                    List<TaskRewards> list = task.getTaskRewardsList();
                    Component reward = Component.text("");
                    for(int i=0;i<list.size();i++){
                        if(list.get(i).getType().equals("vault")){
                            reward = reward.append(Component.text("  "+list.get(i).getAmount()+" RWB\n"));
                        }
                        else if(list.get(i).getType().equals("itemStack")){
                            Material item = Material.matchMaterial(extractIdByRegex(list.get(i).getItemStack()));
                            if (item != null) {
                                reward = reward.append(Component.text("  "+list.get(i).getAmount()+" "))
                                        .append(Component.translatable(item.getItemTranslationKey()))
                                        .append(Component.text("\n"));
                            } else {
                                reward = reward.append(Component.text("  "+list.get(i).getAmount()+" 未知物品\n"));
                            }
                        }
                    }
                    //构建奖励的文本组件

                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
                    bookMeta.setDisplayName("任务悬赏");
                    book.setAmount(1);

                    bookMeta.setTitle("任务悬赏");
                    bookMeta.setAuthor("?");
                    bookMeta.addPages(
                            Component.text("------任务 详情------\n")
                                    .append(Component.text("任务名称：" + task.getTaskName()+"\n"))
                                    .append(Component.text("任务描述：" + task.getDescription()+"\n"))
                                    .append(Component.text("任务奖励：\n"))
                                    .append(reward)
                                    .append(Component.text("[接取]")
                                            .clickEvent(ClickEvent.runCommand("/apply "+ID))
                                            .color(TextColor.color(85,255,85))
                                    ));
                    book.setItemMeta(bookMeta);
                    nearest.getInventory().addItem(book);
                    ApplyTask.addPlayerMeta(nearest, "gettodaytask-"+args[0], "1");   
                }
                else{
                    nearest.sendMessage("你已经看过此等级的悬赏了");
                }
            }
        }
        return true;
    }
    public static String extractIdByRegex(String itemString) {
        // 编译正则：id: 后跟任意空白，然后非贪婪捕获任何字符，直到遇到换行+空白+count:
        Pattern pattern = Pattern.compile("minecraft:\\s*(.*?)(?=\\n\\s*count:)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(itemString);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}