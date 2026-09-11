package org.RWP.rWPP;
import cn.handyplus.guild.event.GuildUpEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class guildUpdateListener implements org.bukkit.event.Listener{
    @EventHandler
    public void onGuildUpdate(GuildUpEvent event){
        int level = event.getNewLevel();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getGuildInfo().getCreator());
        UUID uuid = offlinePlayer.getUniqueId();
        LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            user = luckPerms.getUserManager().loadUser(uuid).join();
        }
        user.data().add(Node.builder("dominion.limitation.guildleader"+level).build());
        user.data().remove(Node.builder("dominion.limitation.guildleader"+(level-1)).build());
        luckPerms.getUserManager().saveUser(user);
    }
}
