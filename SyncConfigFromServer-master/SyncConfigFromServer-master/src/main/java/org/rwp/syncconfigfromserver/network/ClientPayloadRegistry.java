package org.rwp.syncconfigfromserver.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.rwp.syncconfigfromserver.SyncConfigFromServer;
import org.rwp.syncconfigfromserver.internal.WriteConfig;

@EventBusSubscriber(
        modid = SyncConfigFromServer.MODID,
        value = Dist.CLIENT                   // 只在客户端加载
)
public class ClientPayloadRegistry {

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                SyncPayload.TYPE,
                ClientPayloadHandler::handleSynConfig  // ← 客户端处理器
        );
    }
}