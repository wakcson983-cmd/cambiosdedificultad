package com.cambiosdificultad;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CambiosDificultad.MODID);

    public static final RegistryObject<SoundEvent> RULETA = SOUNDS.register("ruleta",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CambiosDificultad.MODID, "ruleta")));

    private Sounds() {}
}
