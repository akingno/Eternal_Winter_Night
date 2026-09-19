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

public final class PolarNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(WinterNight.MOD_ID, "polar"), () -> "1", "1"::equals, "1"::equals);
    public static void register() {
        CHANNEL.registerMessage(0, BlizzardPacket.class, (packet, buffer) -> buffer.writeBoolean(packet.active),
                buffer -> new BlizzardPacket(buffer.readBoolean()), PolarNetwork::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    private static void handle(BlizzardPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlizzardClient.setActive(packet.active)));
        context.setPacketHandled(true);
    }
    public static void sync(ServerPlayer player, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BlizzardPacket(active));
    }
    private record BlizzardPacket(boolean active) {}
}
