package de.farodesign;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Liest index.json + preview.png + palette.txt aus der Mod-JAR. Reine Java-Logik, keine Minecraft-Klassen. */
public final class StyleRegistry {
    public static final String STYLES_ROOT = "assets/farodesign/styles/";

    private static final Map<Category, List<Style>> STYLES = new EnumMap<>(Category.class);

    private StyleRegistry() {}

    public static void load() {
        STYLES.clear();
        try (InputStream in = StyleRegistry.class.getClassLoader().getResourceAsStream(STYLES_ROOT + "index.json")) {
            if (in == null) throw new IllegalStateException("index.json fehlt");
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            for (Category cat : Category.values()) {
                List<Style> list = new ArrayList<>();
                list.add(new Style(cat, Style.DEFAULT_ID, null, null));
                JsonArray ids = root.has(cat.id) ? root.getAsJsonArray(cat.id) : new JsonArray();
                for (JsonElement e : ids) {
                    String id = e.getAsString();
                    list.add(new Style(cat, id, readPreview(cat, id), readPalette(cat, id)));
                }
                STYLES.put(cat, list);
            }
        } catch (Exception ex) {
            FaroDesignClient.LOGGER.error("Styles konnten nicht geladen werden", ex);
            for (Category cat : Category.values()) {
                STYLES.computeIfAbsent(cat, c -> new ArrayList<>(List.of(new Style(c, Style.DEFAULT_ID, null, null))));
            }
        }
    }

    public static List<Style> styles(Category cat) {
        return STYLES.getOrDefault(cat, List.of());
    }

    public static Style find(Category cat, String id) {
        for (Style s : styles(cat)) if (s.id.equals(id)) return s;
        return styles(cat).get(0);
    }

    /** Pfad eines Style-Ordners (funktioniert in der JAR und in der Dev-Umgebung). */
    public static Path styleDir(Category cat, String id) {
        return FabricLoader.getInstance().getModContainer(FaroDesignClient.MOD_ID)
                .flatMap(c -> c.findPath(STYLES_ROOT + cat.id + "/" + id))
                .orElse(null);
    }

    private static int[] readPreview(Category cat, String id) {
        try (InputStream in = StyleRegistry.class.getClassLoader()
                .getResourceAsStream(STYLES_ROOT + cat.id + "/" + id + "/preview.png")) {
            if (in == null) return new int[256];
            BufferedImage img = ImageIO.read(in);
            int[] px = new int[256];
            for (int y = 0; y < 16; y++)
                for (int x = 0; x < 16; x++)
                    px[y * 16 + x] = img.getRGB(x, y);
            return px;
        } catch (Exception e) {
            return new int[256];
        }
    }

    private static int[] readPalette(Category cat, String id) {
        try {
            Path dir = styleDir(cat, id);
            if (dir == null) return null;
            String[] hex = Files.readString(dir.resolve("palette.txt")).trim().split("\\s+");
            int[] out = new int[hex.length];
            for (int i = 0; i < hex.length; i++) out[i] = 0xFF000000 | Integer.parseInt(hex[i], 16);
            return out;
        } catch (Exception e) {
            return null;
        }
    }
}
