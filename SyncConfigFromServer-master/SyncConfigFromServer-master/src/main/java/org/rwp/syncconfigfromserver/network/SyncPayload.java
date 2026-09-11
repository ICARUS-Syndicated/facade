package org.rwp.syncconfigfromserver.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.rwp.syncconfigfromserver.api.ConfigList;

public record SyncPayload(
        ConfigList modConfig,  // 枚举类型
        String configPath,                  // 配置文件路径
        String newValue                     // 新值
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath("syncconfigfromserver", "sync_payload")
            );

    // 自定义枚举编解码器：枚举 <-> 字符串
    public static final StreamCodec<ByteBuf, ConfigList> CONFIG_LIST_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    ConfigList::valueOf,    // 字符串 -> 枚举（解码）
                    ConfigList::name          // 枚举 -> 字符串（编码）
            );

    // 正确的 STREAM_CODEC：3 个字段对应 3 个编解码器
    public static final StreamCodec<ByteBuf, SyncPayload> STREAM_CODEC = StreamCodec.composite(
            CONFIG_LIST_CODEC,           // 第1个字段的编解码器
            SyncPayload::modConfig,      // 第1个字段的 getter（小写！）

            ByteBufCodecs.STRING_UTF8,   // 第2个字段的编解码器
            SyncPayload::configPath,     // 第2个字段的 getter

            ByteBufCodecs.STRING_UTF8,   // 第3个字段的编解码器
            SyncPayload::newValue,       // 第3个字段的 getter

            SyncPayload::new             // 3 个参数的构造函数
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
