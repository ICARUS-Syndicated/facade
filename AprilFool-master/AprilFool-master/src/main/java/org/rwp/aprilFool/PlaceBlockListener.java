package org.rwp.aprilFool;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceBlockListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.TNT) {
            LuckPermsProvider.get().getUserManager().modifyUser(event.getPlayer().getUniqueId(), user -> {
                // 1. 获取当前 placedTNT 的值（可能为 null）
                String currentValue = user.getCachedData().getMetaData().getMetaValue("placedtnt");
                int score = 0;
                if (currentValue != null) {
                    try {
                        score = Integer.parseInt(currentValue);
                    } catch (NumberFormatException e) {
                        // 如果值不是合法数字，从0开始
                        score = 0;
                    }
                }
                int newScore = score + 1;
                

                // 2. 删除所有已有的 placedTNT 节点
                user.data().clear(NodeType.META.predicate(mn ->
                        mn.getMetaKey().equals("placedtnt")
                ));

                // 3. 添加新节点
                MetaNode newMetaNode = MetaNode.builder("placedtnt", String.valueOf(newScore)).build();
                user.data().add(newMetaNode);

                // modifyUser 会自动保存
            });
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(0.5,0,0.5), TNTPrimed.class,tnt ->{
                tnt.setFuseTicks(20);
            });
        }
    }
}
