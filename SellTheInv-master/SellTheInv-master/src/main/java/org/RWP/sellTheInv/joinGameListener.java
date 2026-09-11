package org.RWP.sellTheInv;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.RWP.sellTheInv.SellTheInv;
public class joinGameListener implements org.bukkit.event.Listener{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("RWP.intolocation.bypass")){
            FileConfiguration config = SellTheInv.getconfig();
            int x = (int) config.get("setting.tpto.x",0);
            int y = (int) config.get("setting.tpto.y",0);
            int z = (int) config.get("setting.tpto.z",0);
            player.teleport(new Location(Bukkit.getWorld("world"),x,y,z));
        }
    }
}
