package com.akingno.winternightak.network;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.client.BlizzardClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Optional;
import java.util.function.Supplier;

/** 暴雪状态单向同步：服务端决定是否暴雪，客户端只据此显示粒子和雾。 */
public final class PolarNetwork {
    // 字符串1为网络协议版本，双方必须一致；不是同步频率或数值平衡参数。
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(WinterNight.MOD_ID, "polar"), () -> "1", "1"::equals, "1"::equals);
    public static void register() {
        // 消息编号0在此频道内唯一；只允许服务端发往客户端，不能用客户端包触发暴雪。
        CHANNEL.registerMessage(0, BlizzardPacket.class, (packet, buffer) -> buffer.writeBoolean(packet.active),
                buffer -> new BlizzardPacket(buffer.readBoolean()), PolarNetwork::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    private static void handle(BlizzardPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        // 网络回调转交客户端主线程，并隔离客户端类，防止专用服务器加载渲染代码。
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlizzardClient.setActive(packet.active)));
        context.setPacketHandled(true);
    }
    public static void sync(ServerPlayer player, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BlizzardPacket(active));
    }
    private record BlizzardPacket(boolean active) {}
}
