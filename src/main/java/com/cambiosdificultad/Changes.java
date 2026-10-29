package com.cambiosdificultad;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Los 40 cambios, en orden de activación (del más suave al más duro).
 * Ninguno le quita habilidades al jugador: solo cambian mobs y mundo.
 */
public final class Changes {
    private Changes() {}

    private static final Predicate<Mob> ANIMAL = m -> m instanceof Animal;
    private static final Predicate<Mob> HOSTIL = m -> m instanceof Enemy;
    private static final Predicate<Mob> ZOMBI = m -> m instanceof Zombie;
    private static final Predicate<Mob> ARANA = m -> m instanceof Spider;
    private static final Predicate<Mob> CREEPER = m -> m instanceof Creeper;
    private static final Predicate<Mob> AHOGADO = m -> m instanceof Drowned;
    private static final Predicate<Mob> ESQUELETO = m -> m instanceof AbstractSkeleton;
    private static final Predicate<Mob> ARQUERO = m -> m instanceof Skeleton || m instanceof Stray;
    private static final Predicate<Mob> ENDERMAN = m -> m instanceof EnderMan;
    private static final Predicate<Mob> SLIME = m -> m instanceof Slime;
    private static final Predicate<Mob> FANTASMA = m -> m instanceof Phantom;

    public static final List<Change> ALL = List.of(
        mob("Animales veloces", "Los animales corren más rápido.",
            "Vacas, ovejas, cerdos, gallinas, caballos y el resto de animales se mueven un 40% más rápido. Será más difícil alcanzarlos y arrearlos. No te afecta en nada más.",
            attr(ANIMAL, Attributes.MOVEMENT_SPEED, 0.4, Operation.MULTIPLY_BASE)),
        mob("Animales robustos", "Los animales tienen más vida.",
            "Todos los animales ganan 10 puntos de vida extra (5 corazones). Tardarás más en conseguir su comida y sus materiales.",
            attr(ANIMAL, Attributes.MAX_HEALTH, 10, Operation.ADDITION)),
        mob("Animales resplandecientes", "Los animales brillan.",
            "Los animales reciben el efecto Resplandor permanente: se ven con un contorno brillante incluso a través de los bloques. Es solo visual.",
            eff(ANIMAL, MobEffects.GLOWING, 0)),
        mob("Monstruos resplandecientes", "Los monstruos brillan.",
            "Todos los monstruos reciben el efecto Resplandor: puedes verlos a través de las paredes, pero también destacan en la oscuridad. No cambia su fuerza.",
            eff(HOSTIL, MobEffects.GLOWING, 0)),
        mob("Zombis bebé", "Algunos zombis son bebés.",
            "El 25% de los zombis serán bebés: más pequeños, más rápidos y más difíciles de golpear. Los zombis ya existentes también pueden convertirse una sola vez.",
            doOnce(ZOMBI, m -> { if (m.getRandom().nextFloat() < 0.25f) ((Zombie) m).setBaby(true); })),
        mob("Zombis veloces", "Los zombis corren más.",
            "Los zombis (y sus variantes: husks y ahogados) se mueven un 30% más rápido. Ya no podrás huir caminando con tanta facilidad.",
            attr(ZOMBI, Attributes.MOVEMENT_SPEED, 0.3, Operation.MULTIPLY_BASE)),
        mob("Arañas ágiles", "Las arañas son más rápidas.",
            "Las arañas y arañas de cueva se mueven un 30% más rápido y saltan sobre ti antes de lo que esperas.",
            attr(ARANA, Attributes.MOVEMENT_SPEED, 0.3, Operation.MULTIPLY_BASE)),
        mob("Creepers veloces", "Los creepers se acercan más rápido.",
            "Los creepers caminan un 25% más rápido. Tendrás menos tiempo para alejarte o prepararte.",
            attr(CREEPER, Attributes.MOVEMENT_SPEED, 0.25, Operation.MULTIPLY_BASE)),
        mob("Ahogados ágiles", "Los ahogados nadan y caminan más rápido.",
            "Los ahogados se mueven un 30% más rápido, tanto en el agua como en tierra. Cuidado al cruzar ríos y océanos.",
            attr(AHOGADO, Attributes.MOVEMENT_SPEED, 0.3, Operation.MULTIPLY_BASE)),
        mob("Esqueletos resistentes", "Los esqueletos aguantan más.",
            "Los esqueletos, strays y esqueletos wither tienen 10 puntos de vida extra (5 corazones).",
            attr(ESQUELETO, Attributes.MAX_HEALTH, 10, Operation.ADDITION)),
        mob("Arañas robustas", "Las arañas tienen más vida.",
            "Las arañas ganan 8 puntos de vida extra (4 corazones), así que necesitarás un golpe o dos más para vencerlas.",
            attr(ARANA, Attributes.MAX_HEALTH, 8, Operation.ADDITION)),
        mob("Visión aguda", "Los monstruos te detectan desde más lejos.",
            "El rango de detección de los monstruos aumenta 24 bloques. Te notarán y te perseguirán desde mucho más lejos.",
            attr(HOSTIL, Attributes.FOLLOW_RANGE, 24, Operation.ADDITION)),
        mob("Slimes saltarines", "Los slimes saltan muchísimo.",
            "Los slimes y cubos de magma reciben Salto III permanente: saltan más alto y llegan más lejos en cada brinco.",
            eff(SLIME, MobEffects.JUMP, 2)),
        world("Lluvia constante", "Nunca deja de llover.",
            "El clima queda en lluvia permanente: cada 10 segundos el juego revisa y, si paró, vuelve a llover. La visibilidad baja y los ahogados pueden aparecer lejos del agua.",
            "rain"),
        mob("Monstruos ligeros", "Los monstruos caen despacio.",
            "Los monstruos reciben Caída lenta: flotan al caer y nunca se hacen daño. Las trampas de caída dejan de servir.",
            eff(HOSTIL, MobEffects.SLOW_FALLING, 0)),
        mob("Monstruos saltarines", "Los monstruos saltan más alto.",
            "Los monstruos reciben Salto II: pueden subir bloques más altos y llegar a donde antes no podían.",
            eff(HOSTIL, MobEffects.JUMP, 1)),
        mob("Monstruos ignífugos", "Los monstruos no se queman.",
            "Los monstruos reciben Resistencia al fuego: los zombis y esqueletos ya no arden con el sol y la lava o el fuego no los daña. Tus trampas de fuego dejan de funcionar.",
            eff(HOSTIL, MobEffects.FIRE_RESISTANCE, 0)),
        mob("Endermans robustos", "Los endermans tienen más vida.",
            "Los endermans ganan 20 puntos de vida extra (10 corazones). Pelear contra uno será bastante más largo.",
            attr(ENDERMAN, Attributes.MAX_HEALTH, 20, Operation.ADDITION)),
        mob("Phantoms gigantes", "Los phantoms son enormes.",
            "Los phantoms nacen con tamaño 5 en vez de 0: son mucho más grandes y su ataque es más fuerte. Aparecen si pasas varias noches sin dormir.",
            doOnce(FANTASMA, m -> ((Phantom) m).setPhantomSize(5))),
        mob("Zombis armados", "Los zombis llevan espada.",
            "Los zombis sin arma reciben una espada de hierro, así que hacen más daño. La espada no se suelta al morir.",
            doOnce(ZOMBI, m -> {
                if (m.getMainHandItem().isEmpty()) {
                    m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    m.setDropChance(EquipmentSlot.MAINHAND, 0f);
                }
            })),
        mob("Monstruos con casco", "Zombis y esqueletos llevan casco.",
            "Los zombis y esqueletos sin casco reciben un casco de hierro: reduce el daño que reciben en la cabeza y los protege del sol. No se suelta al morir.",
            doOnce(m -> (m instanceof Zombie || m instanceof AbstractSkeleton), m -> {
                if (m.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                    m.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    m.setDropChance(EquipmentSlot.HEAD, 0f);
                }
            })),
        mob("Monstruos firmes", "Es más difícil empujar a los monstruos.",
            "Los monstruos ganan 0,6 de resistencia al retroceso: tus golpes, flechas y explosiones los empujan mucho menos.",
            attr(HOSTIL, Attributes.KNOCKBACK_RESISTANCE, 0.6, Operation.ADDITION)),
        mob("Monstruos regenerativos", "Los monstruos se curan solos.",
            "Los monstruos reciben Regeneración I permanente y recuperan vida con el tiempo. Conviene rematarlos rápido.",
            eff(HOSTIL, MobEffects.REGENERATION, 0)),
        mob("Monstruos rápidos", "Todos los monstruos se mueven más rápido.",
            "Todos los monstruos se mueven un 15% más rápido (se suma a los cambios anteriores de velocidad).",
            attr(HOSTIL, Attributes.MOVEMENT_SPEED, 0.15, Operation.MULTIPLY_BASE)),
        world("Tormentas eléctricas", "Llegan tormentas con truenos.",
            "Cada 10 minutos de juego se desata una tormenta eléctrica de unos 5 minutos. Con truenos, aparecen más monstruos en la superficie y el cielo se oscurece.",
            "thunder"),
        mob("Creepers de mecha corta", "Los creepers explotan antes.",
            "La mecha de los creepers baja de 1,5 segundos a 0,75 segundos. Reaccionar a tiempo será mucho más difícil.",
            creeperTag(c -> c.putShort("Fuse", (short) 15))),
        mob("Monstruos robustos", "Los monstruos tienen +50% de vida.",
            "Todos los monstruos tienen un 50% más de vida máxima y empiezan con la vida completa.",
            attr(HOSTIL, Attributes.MAX_HEALTH, 0.5, Operation.MULTIPLY_BASE)),
        mob("Monstruos fuertes", "Los monstruos hacen más daño.",
            "El daño de ataque cuerpo a cuerpo de todos los monstruos aumenta un 30%.",
            attr(HOSTIL, Attributes.ATTACK_DAMAGE, 0.3, Operation.MULTIPLY_BASE)),
        mob("Monstruos acorazados", "Los monstruos tienen armadura natural.",
            "Todos los monstruos ganan 6 puntos de armadura (3 iconos de armadura). Tus golpes les hacen menos daño.",
            attr(HOSTIL, Attributes.ARMOR, 6, Operation.ADDITION)),
        mob("Monstruos absorbentes", "Los monstruos tienen corazones extra.",
            "Los monstruos reciben Absorción II permanente: tienen 8 puntos de vida extra (4 corazones amarillos) que hay que vencer primero.",
            eff(HOSTIL, MobEffects.ABSORPTION, 1)),
        mob("Esqueletos con arcos de poder", "Los arqueros disparan flechas más fuertes.",
            "Los esqueletos y strays reciben un arco con Poder III, por lo que sus flechas hacen bastante más daño. El arco no se suelta al morir.",
            doOnce(ARQUERO, m -> {
                ItemStack bow = new ItemStack(Items.BOW);
                bow.enchant(Enchantments.POWER_ARROWS, 3);
                m.setItemSlot(EquipmentSlot.MAINHAND, bow);
                m.setDropChance(EquipmentSlot.MAINHAND, 0f);
            })),
        mob("Creepers cargados", "Algunos creepers nacen cargados.",
            "El 15% de los creepers nacen cargados por un rayo, con una explosión mucho más potente. Se reconocen por su aura azul.",
            creeperTag2(0.15f)),
        world("Relámpagos cercanos", "Caen rayos a tu alrededor durante las tormentas.",
            "Mientras haya tormenta, cada 2 minutos cae un rayo entre 24 y 40 bloques de un jugador al azar. Pueden prender fuego a lo que esté cerca. Funciona mejor junto a «Tormentas eléctricas».",
            "lightning"),
        mob("Creepers explosivos", "Las explosiones de creeper son enormes.",
            "El radio de explosión de los creepers pasa de 3 a 6 bloques. Un solo creeper puede destruir una base pequeña.",
            creeperTag(c -> c.putByte("ExplosionRadius", (byte) 6))),
        world("Patrullas de saqueadores", "Aparecen saqueadores durante el día.",
            "Cada 7 minutos y medio, de día, aparecen 3 saqueadores con ballesta a 20-33 bloques de cada jugador (si no hay ya muchos monstruos cerca).",
            "patrol"),
        world("Hordas de zombis", "Hordas de zombis por la noche.",
            "Cada 5 minutos, de noche, aparecen 3 zombis a 20-33 bloques de cada jugador (si no hay ya muchos monstruos cerca).",
            "horde_zombie"),
        world("Nidos de arañas", "Grupos de arañas por la noche.",
            "Cada 6 minutos y 40 segundos, de noche, aparecen 3 arañas a 20-33 bloques de cada jugador (si no hay ya muchos monstruos cerca).",
            "spiders"),
        world("Hordas de esqueletos", "Esqueletos en grupo por la noche.",
            "Cada 5 minutos, de noche, aparecen 2 esqueletos a 20-33 bloques de cada jugador (si no hay ya muchos monstruos cerca).",
            "horde_skeleton"),
        world("Invasión de creepers", "Creepers extra por la noche.",
            "Cada 7 minutos y medio, de noche, aparecen 2 creepers a 20-33 bloques de cada jugador (si no hay ya muchos monstruos cerca).",
            "horde_creeper"),
        mob("Monstruos resistentes", "Los monstruos reciben menos daño.",
            "Los monstruos reciben Resistencia I permanente: todo el daño que reciben baja un 20%. Es el último cambio: ¡a sobrevivir!",
            eff(HOSTIL, MobEffects.DAMAGE_RESISTANCE, 0))
    );

    public static final int COUNT = ALL.size();

    /** Aplica (de forma idempotente) los primeros {@code active} cambios a un mob. */
    public static void applyAll(Mob mob, int active) {
        int n = Math.min(active, COUNT);
        for (int i = 0; i < n; i++) {
            Change.Hook h = ALL.get(i).hook();
            if (h != null) h.apply(mob, i);
        }
    }

    public static boolean worldOn(String key, int active) {
        int n = Math.min(active, COUNT);
        for (int i = 0; i < n; i++) if (key.equals(ALL.get(i).world())) return true;
        return false;
    }

    // ------------------------------------------------------------------ helpers
    private static Change mob(String n, String b, String d, Change.Hook h) { return new Change(n, b, d, h, null); }
    private static Change world(String n, String b, String d, String key) { return new Change(n, b, d, null, key); }

    private static UUID uid(int id) { return new UUID(0x0CD1F1C0DEADBEEFL, 1000L + id); }

    private static boolean flag(Mob m, int id) {
        CompoundTag t = m.getPersistentData();
        String k = "cd_" + id;
        if (t.getBoolean(k)) return false;
        t.putBoolean(k, true);
        return true;
    }

    private static Change.Hook attr(Predicate<Mob> target, Attribute a, double amount, Operation op) {
        return (m, id) -> {
            if (!target.test(m)) return;
            AttributeInstance in = m.getAttribute(a);
            if (in == null || in.getModifier(uid(id)) != null) return;
            in.addPermanentModifier(new AttributeModifier(uid(id), "cambiosdificultad." + id, amount, op));
            if (a == Attributes.MAX_HEALTH) m.setHealth(m.getMaxHealth());
        };
    }

    private static Change.Hook eff(Predicate<Mob> target, MobEffect e, int amp) {
        return (m, id) -> {
            if (target.test(m) && flag(m, id))
                m.addEffect(new MobEffectInstance(e, MobEffectInstance.INFINITE_DURATION, amp, false, false, false));
        };
    }

    private static Change.Hook doOnce(Predicate<Mob> target, Consumer<Mob> action) {
        return (m, id) -> { if (target.test(m) && flag(m, id)) action.accept(m); };
    }

    private static Change.Hook creeperTag(Consumer<CompoundTag> edit) {
        return (m, id) -> {
            if (!(m instanceof Creeper c) || !flag(m, id)) return;
            CompoundTag t = new CompoundTag();
            c.addAdditionalSaveData(t);
            edit.accept(t);
            c.readAdditionalSaveData(t);
        };
    }

    private static Change.Hook creeperTag2(float chance) {
        return (m, id) -> {
            if (!(m instanceof Creeper c) || !flag(m, id)) return;
            if (c.getRandom().nextFloat() >= chance) return;
            CompoundTag t = new CompoundTag();
            c.addAdditionalSaveData(t);
            t.putBoolean("powered", true);
            c.readAdditionalSaveData(t);
        };
    }
}
