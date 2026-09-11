package org.rwp.syncconfigfromserver.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.rwp.syncconfigfromserver.internal.WriteConfig;

public class ClientPayloadHandler {

    /**
     * 在主线程处理收到的配置同步数据
     */
    public static void handleSynConfig(SyncPayload data, IPayloadContext context) {
        // enqueueWork 确保在主线程执行（涉及 UI/文件操作需要）
        context.enqueueWork(() -> {
            WriteConfig.WriteModConfig(data);
        });
    }
}