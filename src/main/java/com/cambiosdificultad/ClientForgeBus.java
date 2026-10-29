package com.cambiosdificultad;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CambiosDificultad.MODID, value = Dist.CLIENT)
public final class ClientForgeBus {
    private ClientForgeBus() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) ClientState.tick();
    }

    /** Si hay un menú abierto (inventario, etc.) la ruleta se sigue viendo encima. */
    @SubscribeEvent
    public static void screenRender(ScreenEvent.Render.Post e) {
        ClientState.drawRoulette(e.getGuiGraphics(), e.getScreen().width, e.getScreen().height, e.getPartialTick());
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.reset();
    }
}
