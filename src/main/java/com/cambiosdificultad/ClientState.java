package com.cambiosdificultad;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/** Estado y dibujo del lado del cliente (ruleta, mensaje arriba a la izquierda, teclas). */
public final class ClientState {
    private ClientState() {}

    public static final KeyMapping OPEN = new KeyMapping("key.cambiosdificultad.menu",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.cambiosdificultad");
    public static final KeyMapping HIDE = new KeyMapping("key.cambiosdificultad.hide",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.categories.cambiosdificultad");

    public static final int FRAME_COUNT = 194;
    /** Cuadro en el que la ruleta ya se detuvo en rojo: aquí salen el chat y el mensaje. */
    private static final int REVEAL_FRAME = 135;
    private static final int NOTIF_TICKS = 1200; // 1 minuto
    private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];
    static {
        for (int i = 0; i < FRAME_COUNT; i++)
            FRAMES[i] = new ResourceLocation(CambiosDificultad.MODID, String.format("textures/gui/ruleta/f%03d.png", i));
    }

    public static int active = 0;
    public static int ticksLeft = 0;

    private static int anim = -1;
    private static int animIdx = 0;
    private static boolean revealed = false;

    private static int notifTicks = -1;
    private static int notifIdx = 0;
    private static boolean notifHidden = false;

    // ------------------------------------------------------------------ red
    public static void onSync(int act, int left, boolean animate) {
        active = act;
        ticksLeft = left;
        if (animate && act > 0) {
            anim = 0;
            animIdx = act - 1;
            revealed = false;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Sounds.RULETA.get(), 1.0F, 1.0F));
        }
    }

    public static void reset() {
        active = 0;
        ticksLeft = 0;
        endAnim();
        notifTicks = -1;
    }

    // ------------------------------------------------------------------ tick
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.screen == null) {
            while (OPEN.consumeClick()) mc.setScreen(new ChangesScreen());
            while (HIDE.consumeClick()) {
                if (notifTicks >= 0) {
                    notifHidden = !notifHidden;
                    mc.player.displayClientMessage(Component.literal(notifHidden
                        ? "Mensaje oculto (pulsa U para mostrarlo)" : "Mensaje visible").withStyle(ChatFormatting.RED), true);
                }
            }
        }
        if (mc.isPaused()) return;

        if (ticksLeft > 0) ticksLeft--;
        if (notifTicks >= 0 && ++notifTicks >= NOTIF_TICKS) notifTicks = -1;

        if (anim >= 0) {
            anim++;
            for (int i = anim; i < Math.min(FRAME_COUNT, anim + 24); i++) mc.getTextureManager().getTexture(FRAMES[i]);
            if (!revealed && anim >= REVEAL_FRAME) reveal();
            if (anim >= FRAME_COUNT) endAnim();
        }
    }

    private static void endAnim() {
        if (anim >= 0) {
            Minecraft mc = Minecraft.getInstance();
            for (ResourceLocation rl : FRAMES) mc.getTextureManager().release(rl);
        }
        anim = -1;
    }

    private static void reveal() {
        revealed = true;
        Minecraft mc = Minecraft.getInstance();
        Change c = Changes.ALL.get(animIdx);
        String dashes = "-".repeat(44);
        Component msg = Component.literal(dashes).withStyle(ChatFormatting.RED)
            .append(Component.literal("\n"))
            .append(Component.literal("¡Nuevo cambio de dificultad! ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal(c.name() + ": ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal(c.brief()).withStyle(ChatFormatting.RED))
            .append(Component.literal("\n"))
            .append(Component.literal(dashes).withStyle(ChatFormatting.RED));
        mc.gui.getChat().addMessage(msg);
        notifIdx = animIdx;
        notifTicks = 0;
        notifHidden = false;
    }

    // ------------------------------------------------------------------ dibujo
    public static void drawRoulette(GuiGraphics g, int w, int h, float partial) {
        if (anim < 0) return;
        int f = Math.min(FRAME_COUNT - 1, anim);
        int size = (int) (h * 0.6f);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        RenderSystem.enableBlend();
        g.blit(FRAMES[f], x, y, size, size, 0, 0, 256, 256, 256, 256);
    }

    public static void drawNotification(GuiGraphics g, float partial) {
        if (notifTicks < 0 || notifHidden) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Change c = Changes.ALL.get(notifIdx);

        Component title = Component.literal("CAMBIO DE DIFICULTAD").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        Component name = Component.literal("#" + (notifIdx + 1) + "  " + c.name()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
        Component brief = Component.literal(c.brief()).withStyle(ChatFormatting.GOLD);
        Component keys = Component.literal("G: ver detalles   U: ocultar").withStyle(ChatFormatting.GRAY);

        int w = Math.max(Math.max(font.width(title), font.width(name)), Math.max(font.width(brief), font.width(keys))) + 16;
        int h = 4 * 11 + 14;
        float in = Math.min(1f, (notifTicks + partial) / 8f);
        int x = (int) (6 - (1f - in) * (w + 12));
        int y = 6;

        g.fill(x, y, x + w, y + h, 0xCC200000);
        g.fill(x, y, x + 3, y + h, 0xFFE01010);
        g.renderOutline(x, y, w, h, 0xFFE01010);
        g.drawString(font, title, x + 9, y + 5, 0xFFFF2020, true);
        g.drawString(font, name, x + 9, y + 16, 0xFFFFFFFF, true);
        g.drawString(font, brief, x + 9, y + 27, 0xFFFFAA00, true);
        g.drawString(font, keys, x + 9, y + 38, 0xFFAAAAAA, true);
        float left = 1f - (notifTicks + partial) / NOTIF_TICKS;
        g.fill(x + 3, y + h - 3, x + 3 + (int) ((w - 3) * Math.max(0f, left)), y + h - 1, 0xFFFF4040);
    }
}
