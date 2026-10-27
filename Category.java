package de.farodesign;

/** Alle konfigurierbaren Kategorien. Die id entspricht dem Ordner unter assets/farodesign/styles/. */
public enum Category {
    GLINT("glint", "Glint"),
    SKY("sky", "Himmel"),
    ARMOUR("armour", "Rüstung"),
    TOTEM("totem", "Totem"),
    SWORD("sword", "Schwert"),
    AXE("axe", "Axt"),
    PICKAXE("pickaxe", "Spitzhacke"),
    XP_BOTTLE("xp_bottle", "XP-Flasche"),
    MACE("mace", "Mace"),
    GOLDEN_APPLE("golden_apple", "Goldapfel"),
    SPEAR("spear", "Speer"),
    TRIDENT("trident", "Dreizack"),
    GUI_COLOR("gui_color", "GUI-Farbe"),
    GLOWSTONE("glowstone", "Glowstone"),
    ANCHOR("anchor", "Respawn Anchor"),
    CRYSTAL("crystal", "Kristall"),
    OBSIDIAN("obsidian", "Obsidian");

    public final String id;
    public final String label;

    Category(String id, String label) {
        this.id = id;
        this.label = label;
    }
}
