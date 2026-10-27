package de.farodesign;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaroDesignClient implements ClientModInitializer {
    public static final String MOD_ID = "farodesign";
    public static final Logger LOGGER = LoggerFactory.getLogger("Faro Design");

    private static KeyBinding openKey;
    /** Seit 1.21.9 ist die Kategorie ein Objekt (Uebersetzung: key.category.farodesign.main). */
    private static final KeyBinding.Category KEY_CATEGORY =
            KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

    @Override
    public void onInitializeClient() {
        StyleRegistry.load();
        FaroConfig.get();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.farodesign.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                client.setScreen(new FaroScreen(client.currentScreen));
            }
        });
        LOGGER.info("Faro Design geladen - Taste K oeffnet das Menue");
    }
}
