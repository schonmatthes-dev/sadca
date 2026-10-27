package de.farodesign;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Baut aus der Auswahl ein normales Ordner-Resourcepack ("FaroDesign") im resourcepacks-Ordner,
 * aktiviert es an oberster Stelle und laedt die Ressourcen neu.
 */
public final class PackBuilder {
    public static final String PACK_NAME = "FaroDesign";
    public static final String PACK_ID = "file/" + PACK_NAME;
    /** Resourcepack-Format 75.0 = Minecraft 1.21.11. */
    private static final int PACK_FORMAT = 75;

    private PackBuilder() {}

    public static void apply(Map<String, String> selection) throws IOException {
        MinecraftClient mc = MinecraftClient.getInstance();
        Path packDir = mc.getResourcePackDir().resolve(PACK_NAME);
        build(packDir, selection);

        ResourcePackManager manager = mc.getResourcePackManager();
        manager.scanPacks();
        if (!manager.getEnabledIds().contains(PACK_ID)) {
            manager.enable(PACK_ID);
        }
        mc.options.refreshResourcePacks(manager);
        mc.reloadResources();
    }

    public static void build(Path packDir, Map<String, String> selection) throws IOException {
        if (Files.exists(packDir)) deleteRecursively(packDir);
        Path textures = packDir.resolve("assets/minecraft/textures");
        Files.createDirectories(textures);

        String mcmeta = "{\"pack\":{\"description\":\"Faro Design - automatisch erzeugt\","
                + "\"min_format\":[" + PACK_FORMAT + ",0],\"max_format\":[" + PACK_FORMAT + ",0]}}";
        Files.writeString(packDir.resolve("pack.mcmeta"), mcmeta, StandardCharsets.UTF_8);

        for (Category cat : Category.values()) {
            String styleId = selection.getOrDefault(cat.id, Style.DEFAULT_ID);
            if (styleId.equals(Style.DEFAULT_ID)) continue;
            Path src = StyleRegistry.styleDir(cat, styleId);
            if (src == null) continue;
            copyStyle(src, textures);
        }
    }

    private static void copyStyle(Path src, Path textures) throws IOException {
        try (Stream<Path> walk = Files.walk(src)) {
            for (Path p : (Iterable<Path>) walk::iterator) {
                if (Files.isDirectory(p)) continue;
                String rel = src.relativize(p).toString().replace('\\', '/');
                if (rel.equals("preview.png") || rel.equals("palette.txt")) continue;
                Path target = textures.resolve(rel);
                Files.createDirectories(target.getParent());
                Files.copy(p, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
    }
}
