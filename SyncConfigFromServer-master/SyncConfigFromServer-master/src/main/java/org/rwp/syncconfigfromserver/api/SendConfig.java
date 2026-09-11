package org.rwp.syncconfigfromserver.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.rwp.syncconfigfromserver.network.SyncPayload;

public class SendConfig {
    public static void SendModConfig(SyncPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,payload);
    }
}
