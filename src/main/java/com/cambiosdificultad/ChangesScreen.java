package com.cambiosdificultad;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Menú de la tecla G: una página por cambio activo + vista de lista con todos. */
public class ChangesScreen extends Screen {
    private static final ResourceLocation SKULL = new ResourceLocation(CambiosDificultad.MODID, "textures/gui/skull_pixel.png");
    private static final ResourceLocation ARROW_L = new ResourceLocation(CambiosDificultad.MODID, "textures/gui/arrow_left.png");
    private static final ResourceLocation ARROW_R = new ResourceLocation(CambiosDificultad.MODID, "textures/gui/arrow_right.png");
    private static final ResourceLocation WHEEL = new ResourceLocation(CambiosDificultad.MODID, "textures/gui/wheel_icon.png");
    private static final int RED = 0xFFE01010;

    private int page;
    private boolean list;
    private int scroll;

    // geometría
    private int b1x0, b1y0, b1x1, b1y1, b2x0, b2y0, b2x1, b2y1;
    private int cx, cy, gap, arrowW, arrowH, wheelS;
    private float ts;
    private int rowH;

    public ChangesScreen() {
        super(Component.literal("Cambios de dificultad"));
        page = Math.max(0, ClientState.active - 1);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        b1x0 = (int) (width * 0.065); b1x1 = (int) (width * 0.925);
        b1y0 = (int) (height * 0.17); b1y1 = (int) (height * 0.27);
        b2x0 = b1x0; b2x1 = b1x1;
        b2y0 = (int) (height * 0.31); b2y1 = (int) (height * 0.84);
        cx = width / 2; cy = (int) (height * 0.905);
        gap = (int) (height * 0.13);
        int k = Math.max(2, Math.round(height / 240f));
        arrowW = 24 * k; arrowH = 16 * k;
        wheelS = (int) (height * 0.085);
        ts = Math.max(1.2f, height / 300f);
        rowH = (int) (9 * ts * 1.7f);
    }

    private int count() { return ClientState.active; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int l = (int) (width * 0.016), r = width - l, t = (int) (height * 0.012), b = height - t;

        g.fill(0, 0, width, height, 0x50000000);
        g.fillGradient(l, t, r, b, 0xB0300000, 0xB0120000);
        g.fill(l, t, r, (int) (height * 0.15), 0xC0200000);
        border(g, l, t, r - l, b - t);

        // título
        title(g, "CAMBIOS", height * 0.020f, Math.max(3f, height / 200f), 0xFFFF9A1A);
        title(g, "DE DIFICULTAD", height * 0.085f, Math.max(2f, height / 300f), 0xFFFFC21A);

        // caja 1
        g.fill(b1x0, b1y0, b1x1, b1y1, 0x90200000);
        border(g, b1x0, b1y0, b1x1 - b1x0, b1y1 - b1y0);
        // caja 2
        g.fill(b2x0, b2y0, b2x1, b2y1, 0x80200000);
        border(g, b2x0, b2y0, b2x1 - b2x0, b2y1 - b2y0);

        int n = count();
        if (n == 0) {
            scaled(g, Component.literal("SIN CAMBIOS ACTIVOS").withStyle(ChatFormatting.BOLD), b1x0 + 12, b1y0 + (b1y1 - b1y0) / 2 - (int) (4.5f * ts * 1.5f), ts * 1.5f, 0xFFFFC21A);
            wrap(g, Component.literal("Todavía no se activó ningún cambio de dificultad. El primero se activa a los 20 minutos de partida y luego llega uno nuevo cada 20 minutos, hasta completar " + Changes.COUNT + ". Cuando se active uno aparecerá aquí con su explicación."),
                b2x0 + 14, b2y0 + 14, ts, 0xFFFFFFFF);
        } else if (list) {
            drawList(g, mx, my, n);
        } else {
            drawPage(g, n);
        }
        right(g, n);

        // calavera pixel art abajo a la derecha de la caja 2
        int s = Math.max(2, Math.round(height / 200f));
        int sw = 38 * s, sh = 50 * s;
        g.blit(SKULL, b2x1 - sw - 10, b2y1 - sh - 8, sw, sh, 0, 0, 38, 50, 38, 50);

        // botones
        if (n > 0) {
            button(g, ARROW_L, cx - gap, cy, arrowW, arrowH, mx, my);
            button(g, WHEEL, cx, cy, wheelS, wheelS, mx, my);
            button(g, ARROW_R, cx + gap, cy, arrowW, arrowH, mx, my);
            Component hint = Component.literal(list ? "Ver páginas" : "Ver todos").withStyle(ChatFormatting.GRAY);
            g.drawCenteredString(font, hint, cx, cy + wheelS / 2 + 3, 0xFFAAAAAA);
        }
    }

    private void drawPage(GuiGraphics g, int n) {
        page = Mth.clamp(page, 0, n - 1);
        Change c = Changes.ALL.get(page);
        scaled(g, Component.literal("#" + (page + 1) + "  " + c.name().toUpperCase()).withStyle(ChatFormatting.BOLD),
            b1x0 + 12, b1y0 + (b1y1 - b1y0) / 2 - (int) (4.5f * ts * 1.5f), ts * 1.5f, 0xFFFFC21A);
        g.enableScissor(b2x0 + 2, b2y0 + 2, b2x1 - 2, b2y1 - 2);
        int y = b2y0 + 14;
        y = wrap(g, Component.literal(c.brief()).withStyle(ChatFormatting.BOLD), b2x0 + 14, y, ts * 1.15f, 0xFFFFAA00);
        y += (int) (ts * 8);
        y = wrap(g, Component.literal(c.detail()), b2x0 + 14, y, ts, 0xFFFFFFFF);
        y += (int) (ts * 10);
        wrap(g, Component.literal("Se activó al minuto " + (20 * (page + 1)) + " de partida."), b2x0 + 14, y, ts * 0.9f, 0xFFB0B0B0);
        g.disableScissor();
    }

    private void drawList(GuiGraphics g, int mx, int my, int n) {
        scaled(g, Component.literal("TODOS LOS CAMBIOS ACTIVOS (" + n + "/" + Changes.COUNT + ")").withStyle(ChatFormatting.BOLD),
            b1x0 + 12, b1y0 + (b1y1 - b1y0) / 2 - (int) (4.5f * ts * 1.5f), ts * 1.5f, 0xFFFFC21A);
        g.enableScissor(b2x0 + 2, b2y0 + 2, b2x1 - 2, b2y1 - 2);
        int max = maxScroll(n);
        scroll = Mth.clamp(scroll, 0, max);
        for (int i = 0; i < n; i++) {
            int y = b2y0 + 8 + i * rowH - scroll;
            if (y + rowH < b2y0 || y > b2y1) continue;
            boolean hover = mx >= b2x0 && mx <= b2x1 && my >= y && my < y + rowH && my >= b2y0 && my <= b2y1;
            if (hover) g.fill(b2x0 + 4, y, b2x1 - 4, y + rowH - 2, 0x60E01010);
            Component row = Component.literal(String.format("%02d  ", i + 1)).withStyle(ChatFormatting.RED)
                .append(Component.literal(Changes.ALL.get(i).name()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("  -  " + Changes.ALL.get(i).brief()).withStyle(ChatFormatting.GRAY));
            scaled(g, row, b2x0 + 14, y + (int) (rowH * 0.2f), ts, 0xFFFFFFFF);
        }
        g.disableScissor();
    }

    private int maxScroll(int n) { return Math.max(0, n * rowH + 16 - (b2y1 - b2y0)); }

    private void right(GuiGraphics g, int n) {
        Component c = n >= Changes.COUNT
            ? Component.literal("Todos los cambios activados").withStyle(ChatFormatting.RED)
            : Component.literal("Próximo cambio en " + clock(ClientState.ticksLeft)).withStyle(ChatFormatting.GRAY);
        int w = (int) (font.width(c) * ts);
        scaled(g, c, b1x1 - w - 12, b1y0 + (b1y1 - b1y0) / 2 - (int) (4.5f * ts), ts, 0xFFAAAAAA);
    }

    private static String clock(int ticks) {
        int s = Math.max(0, ticks / 20);
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    // ------------------------------------------------------------------ utilidades de dibujo
    private void border(GuiGraphics g, int x, int y, int w, int h) {
        g.renderOutline(x, y, w, h, RED);
        g.renderOutline(x + 1, y + 1, w - 2, h - 2, 0xFF8A0000);
    }

    private void title(GuiGraphics g, String s, float y, float scale, int color) {
        Component c = Component.literal(s).withStyle(ChatFormatting.BOLD);
        int tw = font.width(c);
        g.pose().pushPose();
        g.pose().translate(width / 2f, y, 0);
        g.pose().scale(scale, scale, 1f);
        for (int[] o : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}})
            g.drawString(font, c, -tw / 2 + o[0], o[1], 0xFF3A0800, false);
        g.drawString(font, c, -tw / 2, 0, color, false);
        g.pose().popPose();
    }

    private void scaled(GuiGraphics g, Component c, int x, int y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, c, 0, 0, color, true);
        g.pose().popPose();
    }

    /** Texto con salto de línea automático; devuelve la siguiente y libre. */
    private int wrap(GuiGraphics g, Component c, int x, int y, float scale, int color) {
        int maxW = (int) ((b2x1 - b2x0 - 40) / scale);
        List<FormattedCharSequence> lines = font.split(c, maxW);
        int lh = (int) (font.lineHeight * scale * 1.25f);
        for (FormattedCharSequence line : lines) {
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(scale, scale, 1f);
            g.drawString(font, line, 0, 0, color, true);
            g.pose().popPose();
            y += lh;
        }
        return y;
    }

    private boolean over(int mx, int my, int ccx, int ccy, int w, int h) {
        return mx >= ccx - w / 2 && mx <= ccx + w / 2 && my >= ccy - h / 2 && my <= ccy + h / 2;
    }

    private void button(GuiGraphics g, ResourceLocation tex, int ccx, int ccy, int w, int h, int mx, int my) {
        float sc = over(mx, my, ccx, ccy, w, h) ? 1.12f : 1f;
        int dw = (int) (w * sc), dh = (int) (h * sc);
        int tw = tex == WHEEL ? 32 : 24, th = tex == WHEEL ? 32 : 16;
        g.blit(tex, ccx - dw / 2, ccy - dh / 2, dw, dh, 0, 0, tw, th, tw, th);
    }

    // ------------------------------------------------------------------ entrada
    private void go(int d) {
        int n = count();
        if (n == 0) return;
        page = Math.floorMod(page + d, n);
        list = false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int n = count();
        if (btn == 0 && n > 0) {
            if (over((int) mx, (int) my, cx - gap, cy, arrowW, arrowH)) { go(-1); return true; }
            if (over((int) mx, (int) my, cx + gap, cy, arrowW, arrowH)) { go(1); return true; }
            if (over((int) mx, (int) my, cx, cy, wheelS, wheelS)) { list = !list; return true; }
            if (list && mx >= b2x0 && mx <= b2x1 && my >= b2y0 && my <= b2y1) {
                int i = (int) ((my - b2y0 - 8 + scroll) / rowH);
                if (i >= 0 && i < n) { page = i; list = false; }
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (list) { scroll = Mth.clamp(scroll - (int) (delta * rowH), 0, maxScroll(count())); return true; }
        go(delta > 0 ? -1 : 1);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientState.OPEN.matches(key, scan)) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) { go(-1); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) { go(1); return true; }
        return super.keyPressed(key, scan, mods);
    }
}
