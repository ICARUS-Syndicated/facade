package org.rwp.lanternRiddles.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.rwp.lanternRiddles.LanternRiddles;
import org.rwp.lanternRiddles.puzzle;

import java.util.Random;

public class PlayerInteractEventListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.COBWEB) {
                Random rand = new Random();
                rand.setSeed(System.currentTimeMillis());
                int randomIndex;
                puzzle puzzle;
                int i = 0;
                do{
                    if (i>=LanternRiddles.puzzles.length) {
                        player.sendMessage(Component.text("当前没有未被回答的灯谜").color(net.kyori.adventure.text.format.NamedTextColor.RED));
                        return;
                    }
                    randomIndex = rand.nextInt(LanternRiddles.puzzles.length);
                    puzzle = LanternRiddles.puzzles[randomIndex];
                    i = i+1;
                }while(puzzle.selected==true);
                LanternRiddles.puzzles[randomIndex].selected=true;
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) book.getItemMeta();
                bookMeta.setDisplayName("未知的灯谜");
                book.setAmount(1);
                
                bookMeta.setTitle("灯谜");
                bookMeta.setAuthor("神秘人");
                
                bookMeta.addPages(
                        Component.text("     游戏规则\n")
                                .append(Component.text("灯笼在被找到后会消失\n"))
                                .append(Component.text("不可再次被其他人找到\n"))
                                .append(Component.text("每个灯谜仅可回答一次\n"))
                                .append(Component.text("回答错误本灯谜会消失\n"))
                                .append(Component.text("回答正确会获得奖励并消失\n"))
                                .append(Component.text("四个选项仅有一个正确")),
                        Component.text("          谜题\n")
                                .append(Component.text(puzzle.question+"\n\n\n"))
                                .append(Component.text("§a["+puzzle.options[0]+"]         ")
                                        .clickEvent(ClickEvent.runCommand("/post"+" "+puzzle.ID+" "+" 0")))
                                .append(Component.text("§a["+puzzle.options[1]+"]\n")
                                        .clickEvent(ClickEvent.runCommand("/post"+" "+puzzle.ID+" "+" 1")))
                                .append(Component.text("§a["+puzzle.options[2]+"]         ")
                                        .clickEvent(ClickEvent.runCommand("/post"+" "+puzzle.ID+" "+" 2")))
                                .append(Component.text("§a["+puzzle.options[3]+"]")
                                        .clickEvent(ClickEvent.runCommand("/post"+" "+puzzle.ID+" "+" 3")))
                );
                bookMeta.addPages();
                book.setItemMeta(bookMeta);
                player.getInventory().setItemInMainHand(book);
                block.setType(Material.AIR);
            }
        }
    }
}
