package com.cambiosdificultad;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CambiosDificultad.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private Net() {}

    public static void register() {
        CH.messageBuilder(SyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SyncPacket::encode)
            .decoder(SyncPacket::decode)
            .consumerMainThread(SyncPacket::handle)
            .add();
    }

    public static void sendAll(SyncPacket p) { CH.send(PacketDistributor.ALL.noArg(), p); }

    public static void sendTo(ServerPlayer pl, SyncPacket p) { CH.send(PacketDistributor.PLAYER.with(() -> pl), p); }
}
