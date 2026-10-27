# Faro Design (Fabric, Minecraft 1.21.11)

Texture-Style-Browser: Pro Kategorie 60 Styles (20 Farben x 3 Varianten), Favoriten, Suche, "Anwenden"-Button.
Taste **K** öffnet das Menü.

Kategorien: Glint, Himmel (Sonne/Mond/Wolken), Rüstung, Totem, Schwert, Axt, Spitzhacke (inkl. Kupfer), XP-Flasche,
Mace, Goldapfel, Speer (alle 7 Speere), Dreizack, GUI-Farbe, Glowstone, Respawn Anchor, Kristall, Obsidian.

## Bauen
Voraussetzungen: JDK 21, Gradle 8.14+ / 9.x (oder Gradle-Wrapper aus https://fabricmc.net/develop/template erzeugen).

    gradle build            # fertige Mod: build/libs/faro-design-1.0.0.jar
    gradle runClient        # zum Testen

Die Jar in `.minecraft/mods` legen (zusammen mit Fabric API, Fabric Loader >= 0.18.1).

## Funktionsweise
"Anwenden" baut aus der Auswahl ein Ordner-Resourcepack `resourcepacks/FaroDesign`, aktiviert es an oberster
Stelle und lädt die Ressourcen neu. Die Auswahl steht in `config/farodesign.json`.

## Eigene Styles / Texturen neu erzeugen
`python3 tools/generate_textures.py` (benötigt Pillow) erzeugt alle Styles neu unter
`src/main/resources/assets/farodesign/styles/<kategorie>/<style>/`. Der Ordnerbaum im Style-Ordner
entspricht `assets/minecraft/textures/`. Eigene Styles: Ordner anlegen, `preview.png` (16x16) beilegen und
den Namen in `index.json` eintragen.

## Hinweise
- Alle Texturen sind eigene, generierte Pixel-Art-Styles (keine Texturen aus anderen Mods/Packs).
- GUI-Farbe färbt das Faro-Menü selbst (Akzentfarbe), nicht das Vanilla-Inventar.
- Dieses Projekt wurde ohne Netzzugang erstellt und noch nicht gegen Minecraft kompiliert;
  Versionsnummern in `gradle.properties` bitte auf fabricmc.net/develop prüfen.
