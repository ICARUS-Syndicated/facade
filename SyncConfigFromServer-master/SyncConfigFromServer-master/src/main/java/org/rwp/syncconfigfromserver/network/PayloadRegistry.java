package org.rwp.syncconfigfromserver.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.rwp.syncconfigfromserver.SyncConfigFromServer;

@EventBusSubscriber(
        modid = SyncConfigFromServer.MODID
)
public class PayloadRegistry {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // 服务端 → 客户端：只注册，不需要服务端处理器
        registrar.playToClient(
                SyncPayload.TYPE,
                SyncPayload.STREAM_CODEC
        );
    }
}