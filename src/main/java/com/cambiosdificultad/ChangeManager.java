package com.cambiosdificultad;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lógica del servidor: temporizador de 20 min, activación, cambios de mundo y comandos. */
@Mod.EventBusSubscriber(modid = CambiosDificultad.MODID)
public final class ChangeManager {
    /** 20 minutos = 24000 ticks. */
    public static final int TICKS_PER_CHANGE = 24000;

    private static volatile int active = 0;

    private ChangeManager() {}

    public static class Data extends SavedData {
        public int active;
        public int ticks;

        public static Data load(CompoundTag t) {
            Data d = new Data();
            d.active = t.getInt("active");
            d.ticks = t.getInt("ticks");
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            t.putInt("active", active);
            t.putInt("ticks", ticks);
            return t;
        }
    }

    private static Data data(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(Data::load, Data::new, "cambiosdificultad");
    }

    // ------------------------------------------------------------ ciclo de vida
    @SubscribeEvent
    public static void onStarted(ServerStartedEvent e) {
        active = data(e.getServer()).active;
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent e) {
        active = 0;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            Data d = data(sp.server);
            Net.sendTo(sp, new SyncPacket(d.active, Math.max(0, TICKS_PER_CHANGE - d.ticks), false));
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        if (e.getEntity() instanceof Mob m && active > 0) Changes.applyAll(m, active);
    }

    // ------------------------------------------------------------ tick
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer s = e.getServer();
        if (s.getPlayerList().getPlayerCount() == 0) return; // el reloj se pausa sin jugadores
        Data d = data(s);
        if (d.active < Changes.COUNT) {
            d.ticks++;
            if (d.ticks % 200 == 0) d.setDirty();
            if (d.ticks >= TICKS_PER_CHANGE) activateNext(s, d);
        }
        worldTick(s, d.active);
    }

    public static void activateNext(MinecraftServer s, Data d) {
        if (d.active >= Changes.COUNT) return;
        d.active++;
        d.ticks = 0;
        d.setDirty();
        active = d.active;
        for (ServerLevel lvl : s.getAllLevels())
            for (Entity en : lvl.getAllEntities())
                if (en instanceof Mob m) Changes.applyAll(m, d.active);
        Net.sendAll(new SyncPacket(d.active, d.active < Changes.COUNT ? TICKS_PER_CHANGE : 0, true));
    }

    private static void worldTick(MinecraftServer s, int act) {
        int t = s.getTickCount();
        if (act <= 0 || t % 20 != 0) return;
        ServerLevel ow = s.overworld();

        if (t % 200 == 0 && Changes.worldOn("rain", act) && !ow.isRaining())
            ow.setWeatherParameters(0, 24000, true, false);
        if (t % 12000 == 0 && Changes.worldOn("thunder", act))
            ow.setWeatherParameters(0, 6000, true, true);

        boolean night = !ow.isDay();
        if (t % 2400 == 0 && ow.isThundering() && Changes.worldOn("lightning", act)) lightning(ow);
        if (ow.isDay() && t % 9000 == 0 && Changes.worldOn("patrol", act)) forPlayers(ow, EntityType.PILLAGER, 3);
        if (night && t % 6000 == 0 && Changes.worldOn("horde_zombie", act)) forPlayers(ow, EntityType.ZOMBIE, 3);
        if (night && t % 8000 == 0 && Changes.worldOn("spiders", act)) forPlayers(ow, EntityType.SPIDER, 3);
        if (night && (t + 3000) % 6000 == 0 && Changes.worldOn("horde_skeleton", act)) forPlayers(ow, EntityType.SKELETON, 2);
        if (night && t % 9000 == 0 && Changes.worldOn("horde_creeper", act)) forPlayers(ow, EntityType.CREEPER, 2);
    }

    private static void forPlayers(ServerLevel lvl, EntityType<? extends Mob> type, int count) {
        for (ServerPlayer p : lvl.players()) {
            if (p.isSpectator()) continue;
            if (lvl.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(48)).size() >= 30) continue;
            RandomSource r = lvl.random;
            for (int i = 0; i < count; i++) {
                for (int tries = 0; tries < 8; tries++) {
                    double ang = r.nextDouble() * Math.PI * 2;
                    int dist = 20 + r.nextInt(14);
                    int x = Mth.floor(p.getX() + Math.cos(ang) * dist);
                    int z = Mth.floor(p.getZ() + Math.sin(ang) * dist);
                    if (!lvl.hasChunkAt(new BlockPos(x, 64, z))) continue;
                    int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!lvl.getFluidState(pos.below()).isEmpty()) continue;
                    if (!lvl.getBlockState(pos.below()).isFaceSturdy(lvl, pos.below(), Direction.UP)) continue;
                    type.spawn(lvl, pos, MobSpawnType.EVENT);
                    break;
                }
            }
        }
    }

    private static void lightning(ServerLevel lvl) {
        var players = lvl.players();
        if (players.isEmpty()) return;
        ServerPlayer p = players.get(lvl.random.nextInt(players.size()));
        double ang = lvl.random.nextDouble() * Math.PI * 2;
        int dist = 24 + lvl.random.nextInt(17);
        int x = Mth.floor(p.getX() + Math.cos(ang) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(ang) * dist);
        if (!lvl.hasChunkAt(new BlockPos(x, 64, z))) return;
        int y = lvl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lvl);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
        lvl.addFreshEntity(bolt);
    }

    // ------------------------------------------------------------ comandos (solo operadores)
    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("cambiosdificultad")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("estado").executes(c -> {
                Data d = data(c.getSource().getServer());
                int min = Math.max(0, (TICKS_PER_CHANGE - d.ticks) / 1200);
                c.getSource().sendSuccess(() -> Component.literal("Cambios activos: " + d.active + "/" + Changes.COUNT
                    + (d.active < Changes.COUNT ? " · siguiente en ~" + min + " min" : " · todos activados")), false);
                return 1;
            }))
            .then(Commands.literal("siguiente").executes(c -> {
                activateNext(c.getSource().getServer(), data(c.getSource().getServer()));
                return 1;
            }))
            .then(Commands.literal("fijar").then(Commands.argument("n", IntegerArgumentType.integer(0, Changes.COUNT)).executes(c -> {
                MinecraftServer s = c.getSource().getServer();
                Data d = data(s);
                d.active = IntegerArgumentType.getInteger(c, "n");
                d.ticks = 0;
                d.setDirty();
                active = d.active;
                for (ServerLevel lvl : s.getAllLevels())
                    for (Entity en : lvl.getAllEntities())
                        if (en instanceof Mob m) Changes.applyAll(m, d.active);
                Net.sendAll(new SyncPacket(d.active, d.active < Changes.COUNT ? TICKS_PER_CHANGE : 0, false));
                return 1;
            })))
            .then(Commands.literal("reiniciar").executes(c -> {
                MinecraftServer s = c.getSource().getServer();
                Data d = data(s);
                d.active = 0;
                d.ticks = 0;
                d.setDirty();
                active = 0;
                Net.sendAll(new SyncPacket(0, TICKS_PER_CHANGE, false));
                c.getSource().sendSuccess(() -> Component.literal("Cambios reiniciados (los mobs ya modificados conservan sus mejoras)."), false);
                return 1;
            })));
    }
}
