package org.RWP.sellTheInv;

import me.yic.xconomy.api.XConomyAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

public class SellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        XConomyAPI xcapi = new XConomyAPI();
        FileConfiguration config=SellTheInv.getconfig();
        float prices = 0;
        if (commandSender instanceof Player player) {
            Inventory inv = player.getInventory();
            UUID uuid = player.getUniqueId();
            String playername = player.getName();
            ItemStack[] itemStacks = inv.getContents();
            for(int i=0;i<itemStacks.length;i++){
                ItemStack itemStack = itemStacks[i];
                if(itemStack != null){
                    int n = itemStack.getAmount();
                    String  Key = itemStack.getType().getKey().getKey();
                    float price =((Number)config.get("price."+Key,0)).floatValue()*n/16;
                    prices = prices + price;
                    if(price != 0){
                        inv.setItem(i,null);
                    }
                }
            }
            player.updateInventory();
            BigDecimal b = new BigDecimal(prices);
            prices = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "〢 "+ChatColor.WHITE+"你获得了"+ChatColor.GOLD+prices+"RWB");
            xcapi.changePlayerBalance(uuid,playername, BigDecimal.valueOf(prices), true);
            return true;
        }
        return false;

    }
    private final Logger logger = Logger.getLogger("SellTheInv");
}
