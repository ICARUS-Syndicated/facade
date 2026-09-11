package org.rwp.syncconfigfromserver;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.rwp.syncconfigfromserver.network.SyncPayload;
import org.rwp.syncconfigfromserver.internal.WriteConfig;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = SyncConfigFromServer.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = SyncConfigFromServer.MODID, value = Dist.CLIENT)
public class SyncConfigFromServerClient {
}
