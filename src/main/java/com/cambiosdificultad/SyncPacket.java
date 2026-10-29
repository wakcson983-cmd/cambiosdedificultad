package com.cambiosdificultad;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: cuántos cambios hay activos, cuánto falta para el siguiente y si hay que animar. */
public record SyncPacket(int active, int ticksLeft, boolean animate) {

    public static void encode(SyncPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.active);
        buf.writeVarInt(p.ticksLeft);
        buf.writeBoolean(p.animate);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        return new SyncPacket(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientState.onSync(p.active, p.ticksLeft, p.animate));
    }
}
