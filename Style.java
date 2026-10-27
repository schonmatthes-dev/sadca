package de.farodesign;

import java.util.Locale;

/** Ein einzelner Style (z. B. "light_pink_3") einer Kategorie. */
public final class Style {
    public static final String DEFAULT_ID = "default";

    public final Category category;
    public final String id;
    public final String name;
    /** 16x16 ARGB-Vorschau; null beim Standard-Style. */
    public final int[] preview;
    /** Farben dunkel, mittel, hell, akzent (ARGB) - nur fuer GUI-Farbe relevant. */
    public final int[] palette;

    public Style(Category category, String id, int[] preview, int[] palette) {
        this.category = category;
        this.id = id;
        this.preview = preview;
        this.palette = palette;
        this.name = id.equals(DEFAULT_ID) ? "Standard" : prettify(id);
    }

    public boolean isDefault() {
        return id.equals(DEFAULT_ID);
    }

    private static String prettify(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return sb.toString();
    }
}
