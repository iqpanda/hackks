package dev.privateclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PrivateClient implements ClientModInitializer {
    public static final String MOD_ID = "privateclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping menuKey;
    private static KeyMapping freecamKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.privateclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        freecamKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.privateclient.freecam", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.level == null || mc.player == null) {
                Config.freecam = false;
                return;
            }
            while (menuKey.consumeClick()) {
                ScreenOpener.open(new MenuScreen());
            }
            while (freecamKey.consumeClick()) {
                Freecam.toggle(mc);
            }
            ScanCache.tick(mc);
        });

        EspRenderer.register();
        LOGGER.info("Private Client loaded. Right Shift = menu, G = freecam.");
    }
}
