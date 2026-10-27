package de.farodesign;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Texture-Style-Browser: Kategorie-Leiste oben, Raster mit Styles, Favoriten, Suche, Settings.
 * Alles wird mit DrawContext.fill/drawText gezeichnet (keine Widget-/Textur-APIs), damit der Code
 * moeglichst wenig von Minecraft-Versionsaenderungen betroffen ist.
 */
public class FaroScreen extends Screen {
    private static final int BG = 0xFF14101E;
    private static final int PANEL = 0xFF1B1629;
    private static final int CELL = 0xFF211B33;
    private static final int CELL_HOVER = 0xFF2D2548;
    private static final int TEXT = 0xFFE6E0F5;
    private static final int TEXT_DIM = 0xFF8E87A6;
    private static final int GOLD = 0xFFFFD34D;

    private final Screen parent;
    private final Map<String, String> pending = new LinkedHashMap<>();

    private int catIndex = 0;
    private double scroll = 0;
    private String search = "";
    private boolean searchFocused = false;
    private boolean favoritesOnly = false;
    private boolean settingsOpen = false;
    private String status = "";

    // Layout (wird in render() gesetzt, in Klick-Handlern wiederverwendet)
    private int px, py, pw, ph;
    private int headerH = 24, barY, barH = 28, titleY, gridTop, gridBottom, footH = 24;
    private int gx, gw, cols, cw = 52, ch = 56, catBtn;

    public FaroScreen(Screen parent) {
        super(Text.literal("Faro Design"));
        this.parent = parent;
        this.pending.putAll(FaroConfig.get().selected);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    // ------------------------------------------------------------------ Hilfen
    private Category cat() {
        return Category.values()[catIndex];
    }

    private int accent() {
        Style s = StyleRegistry.find(Category.GUI_COLOR, pending.getOrDefault(Category.GUI_COLOR.id, Style.DEFAULT_ID));
        return s.palette != null && s.palette.length > 2 ? s.palette[1] : 0xFFB05CFF;
    }

    private static int withAlpha(int argb, int a) {
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private List<Style> visibleStyles() {
        List<Style> out = new ArrayList<>();
        String q = search.trim().toLowerCase(Locale.ROOT);
        for (Style s : StyleRegistry.styles(cat())) {
            if (!q.isEmpty() && !s.name.toLowerCase(Locale.ROOT).contains(q)) continue;
            if (favoritesOnly && !FaroConfig.get().isFavorite(s)) continue;
            out.add(s);
        }
        return out;
    }

    private int changeCount() {
        int n = 0;
        for (Category c : Category.values()) {
            String a = pending.getOrDefault(c.id, Style.DEFAULT_ID);
            String b = FaroConfig.get().selectedId(c);
            if (!a.equals(b)) n++;
        }
        return n;
    }

    static void drawPixels(DrawContext ctx, int[] px, int x, int y, int s) {
        if (px == null) return;
        for (int row = 0; row < 16; row++) {
            int col = 0;
            while (col < 16) {
                int c = px[row * 16 + col];
                int run = 1;
                while (col + run < 16 && px[row * 16 + col + run] == c) run++;
                if ((c >>> 24) > 0) ctx.fill(x + col * s, y + row * s, x + (col + run) * s, y + (row + 1) * s, c);
                col += run;
            }
        }
    }

    private void centered(DrawContext ctx, String text, int cx, int y, int color) {
        ctx.drawText(textRenderer, text, cx - textRenderer.getWidth(text) / 2, y, color, true);
    }

    private static void border(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void layout() {
        pw = Math.min(width - 8, 500);
        ph = Math.min(height - 8, 300);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        barY = py + headerH + 2;
        titleY = barY + barH + 4;
        gridTop = titleY + 14;
        gridBottom = py + ph - footH - 2;
        gx = px + 10;
        gw = pw - 20;
        cols = Math.max(1, gw / cw);
        int avail = pw - 20 - 58 - 8;
        catBtn = Math.max(14, Math.min(24, avail / Category.values().length));
    }

    private int[] cellPos(int index) {
        int col = index % cols, row = index / cols;
        int x = gx + col * cw + (gw - cols * cw) / 2;
        int y = gridTop + row * ch - (int) scroll;
        return new int[]{x, y};
    }

    private int maxScroll(int count) {
        int rows = (count + cols - 1) / cols;
        return Math.max(0, rows * ch - (gridBottom - gridTop));
    }

    // ------------------------------------------------------------------ Rendering
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        layout();
        int accent = accent();
        ctx.fill(0, 0, width, height, 0xB0000000);
        ctx.fill(px, py, px + pw, py + ph, BG);
        ctx.fill(px, py, px + pw, py + 2, accent);
        border(ctx, px, py, px + pw, py + ph, withAlpha(accent, 0x66));

        // Kopfzeile
        ctx.drawText(textRenderer, "FARO", px + 10, py + 9, accent, true);
        ctx.drawText(textRenderer, "| TEXTURE STYLES", px + 40, py + 9, TEXT_DIM, false);

        int searchW = Math.min(100, pw / 4);
        int sx = px + pw - 10 - searchW;
        ctx.fill(sx, py + 5, sx + searchW, py + 20, 0xFF0E0B17);
        border(ctx, sx, py + 5, sx + searchW, py + 20, searchFocused ? accent : 0xFF2D2548);
        String shown = search.isEmpty() && !searchFocused ? "Suche..." : search + (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ctx.drawText(textRenderer, textRenderer.trimToWidth(shown, searchW - 8), sx + 4, py + 9,
                search.isEmpty() && !searchFocused ? TEXT_DIM : TEXT, false);

        int favW = 66;
        int fx = sx - 6 - favW;
        ctx.drawText(textRenderer, "\u2605 Favoriten", fx, py + 9, favoritesOnly ? GOLD : TEXT_DIM, favoritesOnly);

        // Kategorie-Leiste
        ctx.fill(px + 6, barY, px + pw - 6, barY + barH, PANEL);
        String hoverLabel = null;
        for (int i = 0; i < Category.values().length; i++) {
            Category c = Category.values()[i];
            int bx = px + 10 + i * catBtn, by = barY + 2;
            boolean sel = i == catIndex;
            boolean hov = in(mx, my, bx, by, catBtn, barH - 4);
            if (sel) {
                ctx.fill(bx, by, bx + catBtn, by + barH - 4, withAlpha(accent, 0x44));
                border(ctx, bx, by, bx + catBtn, by + barH - 4, accent);
            } else if (hov) {
                ctx.fill(bx, by, bx + catBtn, by + barH - 4, CELL_HOVER);
            }
            Style show = StyleRegistry.find(c, pending.getOrDefault(c.id, Style.DEFAULT_ID));
            if (show.isDefault() && StyleRegistry.styles(c).size() > 1) show = StyleRegistry.styles(c).get(1);
            drawPixels(ctx, show.preview, bx + (catBtn - 16) / 2, by + (barY + barH - 4 - by - 16) / 2 + 1, 1);
            if (hov) hoverLabel = c.label;
        }
        int setW = 58, setX = px + pw - 10 - setW;
        boolean setHov = in(mx, my, setX, barY + 2, setW, barH - 4);
        ctx.fill(setX, barY + 2, setX + setW, barY + barH - 2, settingsOpen || setHov ? accent : withAlpha(accent, 0xCC));
        centered(ctx, "Settings", setX + setW / 2, barY + 10, 0xFFFFFFFF);

        // Titelzeile
        if (settingsOpen) {
            renderSettings(ctx, mx, my, accent);
        } else {
            List<Style> list = visibleStyles();
            ctx.fill(px + 6, titleY + 1, px + 8, titleY + 10, accent);
            ctx.drawText(textRenderer, cat().label.toUpperCase(Locale.ROOT), px + 12, titleY + 2, TEXT, false);
            int tw = textRenderer.getWidth(cat().label.toUpperCase(Locale.ROOT));
            ctx.drawText(textRenderer, (StyleRegistry.styles(cat()).size() - 1) + " Styles", px + 16 + tw, titleY + 2, TEXT_DIM, false);
            Style active = StyleRegistry.find(cat(), pending.getOrDefault(cat().id, Style.DEFAULT_ID));
            String act = "Aktiv: " + active.name;
            ctx.drawText(textRenderer, act, px + pw - 10 - textRenderer.getWidth(act), titleY + 2, accent, false);

            // Raster
            scroll = Math.max(0, Math.min(scroll, maxScroll(list.size())));
            ctx.enableScissor(px + 6, gridTop, px + pw - 6, gridBottom);
            for (int i = 0; i < list.size(); i++) {
                int[] pos = cellPos(i);
                int x = pos[0], y = pos[1];
                if (y + ch < gridTop || y > gridBottom) continue;
                Style s = list.get(i);
                boolean sel = s.id.equals(pending.getOrDefault(cat().id, Style.DEFAULT_ID));
                boolean hov = in(mx, my, x, y, cw - 4, ch - 4) && my >= gridTop && my <= gridBottom;
                ctx.fill(x, y, x + cw - 4, y + ch - 4, hov ? CELL_HOVER : CELL);
                if (sel) border(ctx, x, y, x + cw - 4, y + ch - 4, accent);
                if (s.isDefault()) {
                    centered(ctx, "\u2014", x + (cw - 4) / 2, y + 17, TEXT_DIM);
                } else {
                    drawPixels(ctx, s.preview, x + (cw - 4 - 32) / 2, y + 6, 2);
                }
                String name = textRenderer.trimToWidth(s.name, cw - 6);
                centered(ctx, name, x + (cw - 4) / 2, y + ch - 14, sel ? 0xFFFFFFFF : TEXT_DIM);
                if (sel) ctx.drawText(textRenderer, "\u2713", x + 3, y + 2, accent, true);
                if (!s.isDefault()) {
                    boolean fav = FaroConfig.get().isFavorite(s);
                    if (fav || hov) ctx.drawText(textRenderer, fav ? "\u2605" : "\u2606", x + cw - 14, y + 2, fav ? GOLD : TEXT_DIM, false);
                }
            }
            ctx.disableScissor();
            if (list.isEmpty()) {
                centered(ctx, favoritesOnly ? "Keine Favoriten in dieser Kategorie" : "Keine Treffer",
                        px + pw / 2, gridTop + 30, TEXT_DIM);
            }
            // Scrollbar
            int max = maxScroll(list.size());
            if (max > 0) {
                int trackH = gridBottom - gridTop;
                int barH2 = Math.max(16, trackH * trackH / (trackH + max));
                int by = gridTop + (int) ((trackH - barH2) * (scroll / max));
                ctx.fill(px + pw - 8, gridTop, px + pw - 6, gridBottom, 0xFF0E0B17);
                ctx.fill(px + pw - 8, by, px + pw - 6, by + barH2, accent);
            }
        }

        // Fusszeile
        int changes = changeCount();
        String left = !status.isEmpty() ? status : (changes == 0 ? "Keine Änderungen" : changes + " Änderung(en) nicht angewendet");
        ctx.drawText(textRenderer, textRenderer.trimToWidth(left, pw - 220), px + 10, py + ph - footH + 7, changes == 0 && status.isEmpty() ? TEXT_DIM : TEXT, false);
        int aw = 66, ax = px + pw - 10 - aw, ay = py + ph - footH + 3;
        boolean aHov = in(mx, my, ax, ay, aw, 16);
        ctx.fill(ax, ay, ax + aw, ay + 16, aHov ? accent : withAlpha(accent, 0xCC));
        centered(ctx, "Anwenden", ax + aw / 2, ay + 4, 0xFFFFFFFF);
        int rw = 100, rx = ax - 6 - rw;
        boolean rHov = in(mx, my, rx, ay, rw, 16);
        ctx.fill(rx, ay, rx + rw, ay + 16, rHov ? CELL_HOVER : CELL);
        centered(ctx, "Kategorie zurück", rx + rw / 2, ay + 4, TEXT);

        if (hoverLabel != null) {
            int tw = textRenderer.getWidth(hoverLabel) + 8;
            int tx = Math.min(mx + 8, width - tw - 2), ty = my + 12;
            ctx.fill(tx, ty, tx + tw, ty + 14, 0xF0100C18);
            border(ctx, tx, ty, tx + tw, ty + 14, accent);
            ctx.drawText(textRenderer, hoverLabel, tx + 4, ty + 3, TEXT, false);
        }
    }

    private void renderSettings(DrawContext ctx, int mx, int my, int accent) {
        ctx.fill(px + 6, titleY + 1, px + 8, titleY + 10, accent);
        ctx.drawText(textRenderer, "SETTINGS", px + 12, titleY + 2, TEXT, false);
        String[] labels = {"Alle Kategorien auf Standard", "Resourcepack neu erstellen", "Alle Favoriten löschen"};
        for (int i = 0; i < labels.length; i++) {
            int y = gridTop + 4 + i * 24;
            boolean hov = in(mx, my, gx, y, 200, 18);
            ctx.fill(gx, y, gx + 200, y + 18, hov ? CELL_HOVER : CELL);
            ctx.drawText(textRenderer, labels[i], gx + 8, y + 5, TEXT, false);
        }
        int y = gridTop + 4 + labels.length * 24 + 6;
        ctx.drawText(textRenderer, "Taste K öffnet dieses Menü (änderbar unter Steuerung).", gx, y, TEXT_DIM, false);
        ctx.drawText(textRenderer, "Die GUI-Farbe färbt dieses Menü. Pack: resourcepacks/" + PackBuilder.PACK_NAME, gx, y + 12, TEXT_DIM, false);
    }

    // ------------------------------------------------------------------ Eingabe
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        return handleClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }

    private boolean handleClick(double mx, double my, int button) {
        layout();
        int searchW = Math.min(100, pw / 4);
        int sx = px + pw - 10 - searchW;
        searchFocused = in(mx, my, sx, py + 5, searchW, 15);
        if (searchFocused) return true;

        int favW = 66, fx = sx - 6 - favW;
        if (in(mx, my, fx, py + 5, favW, 15)) {
            favoritesOnly = !favoritesOnly;
            scroll = 0;
            return true;
        }

        for (int i = 0; i < Category.values().length; i++) {
            if (in(mx, my, px + 10 + i * catBtn, barY + 2, catBtn, barH - 4)) {
                catIndex = i;
                scroll = 0;
                settingsOpen = false;
                return true;
            }
        }
        int setW = 58, setX = px + pw - 10 - setW;
        if (in(mx, my, setX, barY + 2, setW, barH - 4)) {
            settingsOpen = !settingsOpen;
            return true;
        }

        int aw = 66, ax = px + pw - 10 - aw, ay = py + ph - footH + 3;
        if (in(mx, my, ax, ay, aw, 16)) {
            applyNow();
            return true;
        }
        int rw = 100, rx = ax - 6 - rw;
        if (in(mx, my, rx, ay, rw, 16)) {
            pending.remove(cat().id);
            status = "";
            return true;
        }

        if (settingsOpen) {
            for (int i = 0; i < 3; i++) {
                if (in(mx, my, gx, gridTop + 4 + i * 24, 200, 18)) {
                    if (i == 0) {
                        pending.clear();
                    } else if (i == 1) {
                        applyNow();
                    } else {
                        FaroConfig.get().favorites.clear();
                        FaroConfig.get().save();
                        status = "Favoriten gelöscht";
                    }
                    return true;
                }
            }
            return false;
        }

        if (my >= gridTop && my <= gridBottom) {
            List<Style> list = visibleStyles();
            for (int i = 0; i < list.size(); i++) {
                int[] pos = cellPos(i);
                if (in(mx, my, pos[0], pos[1], cw - 4, ch - 4)) {
                    Style s = list.get(i);
                    boolean starHit = in(mx, my, pos[0] + cw - 16, pos[1], 14, 12);
                    if ((button == 1 || starHit) && !s.isDefault()) {
                        FaroConfig.get().toggleFavorite(s);
                        FaroConfig.get().save();
                    } else if (button == 0) {
                        if (s.isDefault()) pending.remove(cat().id);
                        else pending.put(cat().id, s.id);
                        status = "";
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void applyNow() {
        FaroConfig cfg = FaroConfig.get();
        cfg.selected = new LinkedHashMap<>(pending);
        cfg.save();
        try {
            PackBuilder.apply(pending);
            status = "Angewendet - Ressourcen werden neu geladen";
        } catch (Exception e) {
            FaroDesignClient.LOGGER.error("Pack konnte nicht gebaut werden", e);
            status = "Fehler: " + e.getMessage();
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (settingsOpen) return false;
        scroll = Math.max(0, Math.min(scroll - vAmount * 24, maxScroll(visibleStyles().size())));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && input.isValidChar() && search.length() < 24) {
            search += input.asString();
            scroll = 0;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
            }
            return true;
        }
        return super.keyPressed(input);
    }
}
