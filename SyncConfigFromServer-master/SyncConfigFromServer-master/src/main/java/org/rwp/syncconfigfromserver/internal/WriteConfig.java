package org.rwp.syncconfigfromserver.internal;

import com.electronwill.nightconfig.core.file.FileConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.rwp.syncconfigfromserver.network.SyncPayload;

import java.nio.file.Files;
import java.nio.file.Path;

public class WriteConfig {
    public static void WriteModConfig(SyncPayload payload) {
        // 获取其他模单的配置目录
        Path configDir = FMLPaths.CONFIGDIR.get();

        // 直接读取文件
        Path otherModConfig = configDir.resolve(payload.modConfig().getConfigName());

        if (Files.exists(otherModConfig)) {
            FileConfig config = FileConfig.of(otherModConfig);
            config.load();
            
            config.set(payload.configPath(), payload.newValue());
            config.save();  
            payload.modConfig().runReload();
            config.close();
        }
    }

        
}
