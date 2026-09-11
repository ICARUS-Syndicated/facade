package org.rwp.aprilFool;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TNTListener implements Listener {
    @EventHandler
    public void TNTListener(EntityExplodeEvent event) {
        // 1. 检查爆炸的实体是否是 TNT
        if (event.getEntityType() == EntityType.TNT) {
            // 2. 取消原有的小爆炸
            event.setCancelled(true);

            // 3. 在相同位置创建一个新的、威力更大的爆炸
            //    第二个参数 "10.0f" 就是你要修改的爆炸威力，数值越大半径越大
            event.getEntity().getWorld().createExplosion(
                    event.getEntity().getLocation(),
                    6.0f, //爆炸威力
                    false,  // 是否引发火灾
                    true   // 是否破坏方块
            );
        }
    } 
}

