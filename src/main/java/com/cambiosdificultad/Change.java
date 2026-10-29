package com.cambiosdificultad;

import net.minecraft.world.entity.Mob;

/** Un cambio de dificultad: texto para el menú + efecto sobre mobs (hook) o sobre el mundo (world). */
public record Change(String name, String brief, String detail, Hook hook, String world) {
    @FunctionalInterface
    public interface Hook {
        void apply(Mob mob, int id);
    }
}
