package org.RWP.rWPP;

import cn.handyplus.guild.event.GuildCreateEvent;
import cn.handyplus.guild.event.GuildDissolutionEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;
import java.util.logging.Logger;

public class guildDeleteListener implements org.bukkit.event.Listener {
    @EventHandler
    public void onGuildDelete(GuildDissolutionEvent event) {
        Player player = event.getPlayer();
        UUID uuid = event.getPlayer().getUniqueId();
        LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            user = luckPerms.getUserManager().loadUser(uuid).join();
        }
        for (int i = 1; i <= 12; i++) {
            if (player.hasPermission("residence.group.guildleader" + i)) {
                user.data().remove(Node.builder("residence.group.guildleader" + i).build());
            }
        }
        luckPerms.getUserManager().saveUser(user);
    }

}
