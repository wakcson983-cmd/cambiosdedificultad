package com.cambiosdificultad;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CambiosDificultad.MODID)
public class CambiosDificultad {
    public static final String MODID = "cambiosdificultad";

    public CambiosDificultad() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Sounds.SOUNDS.register(bus);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(Net::register));
    }
}
