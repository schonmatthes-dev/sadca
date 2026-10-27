package de.farodesign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Gespeicherte Auswahl (config/farodesign.json). */
public final class FaroConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("farodesign.json");

    /** Kategorie-id -> Style-id. Fehlt ein Eintrag, gilt "default". */
    public Map<String, String> selected = new LinkedHashMap<>();
    /** Favoriten als "kategorie/style". */
    public Set<String> favorites = new LinkedHashSet<>();

    private static FaroConfig instance;

    public static FaroConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static FaroConfig load() {
        try {
            if (Files.exists(FILE)) {
                FaroConfig c = GSON.fromJson(Files.readString(FILE), FaroConfig.class);
                if (c != null) {
                    if (c.selected == null) c.selected = new LinkedHashMap<>();
                    if (c.favorites == null) c.favorites = new LinkedHashSet<>();
                    return c;
                }
            }
        } catch (Exception e) {
            FaroDesignClient.LOGGER.warn("Config konnte nicht gelesen werden, nutze Standard", e);
        }
        return new FaroConfig();
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception e) {
            FaroDesignClient.LOGGER.error("Config konnte nicht gespeichert werden", e);
        }
    }

    public String selectedId(Category cat) {
        return selected.getOrDefault(cat.id, Style.DEFAULT_ID);
    }

    public boolean isFavorite(Style s) {
        return favorites.contains(s.category.id + "/" + s.id);
    }

    public void toggleFavorite(Style s) {
        String key = s.category.id + "/" + s.id;
        if (!favorites.remove(key)) favorites.add(key);
    }

    public Map<String, String> copySelection() {
        return new LinkedHashMap<>(selected);
    }
}
