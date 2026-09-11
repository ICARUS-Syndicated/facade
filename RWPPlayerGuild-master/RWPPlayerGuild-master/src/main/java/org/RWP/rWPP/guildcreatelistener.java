package org.RWP.rWPP;
import cn.handyplus.guild.event.GuildCreateEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class guildcreatelistener implements org.bukkit.event.Listener {
    @EventHandler
    public void onGuildCreate(GuildCreateEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        LuckPerms luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            user = luckPerms.getUserManager().loadUser(uuid).join();
        }
        user.data().add(Node.builder("dominion.limitation.guildleader1").build());
        luckPerms.getUserManager().saveUser(user);
    }
}
