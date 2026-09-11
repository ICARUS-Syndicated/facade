package org.rwp.syncconfigfromserver;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SyncConfigFromServer.MODID)
public class SyncConfigFromServer {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "syncconfigfromserver";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "syncconfigfromserver" namespace
}
