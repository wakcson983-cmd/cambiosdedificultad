package com.cambiosdificultad;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CambiosDificultad.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModBus {
    private ClientModBus() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(ClientState.OPEN);
        e.register(ClientState.HIDE);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("cambios", (gui, g, partial, w, h) -> {
            if (Minecraft.getInstance().options.hideGui) return;
            ClientState.drawRoulette(g, w, h, partial);
            ClientState.drawNotification(g, partial);
        });
    }
}
