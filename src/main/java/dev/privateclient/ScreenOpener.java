package dev.privateclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Opens a screen without hard-coding the method name, because 26.x moved/renamed
 * Minecraft#setScreen. Tries the likely names on Minecraft, then on its "gui" field.
 */
public final class ScreenOpener {
    private static final String[] NAMES = {"setScreen", "setScreenAndShow"};

    public static void open(Screen screen) {
        Minecraft mc = Minecraft.getInstance();
        if (tryOn(mc, screen)) return;

        try {
            Field gui = Minecraft.class.getField("gui");
            Object guiObj = gui.get(mc);
            if (guiObj != null && tryOn(guiObj, screen)) return;
        } catch (ReflectiveOperationException ignored) {
            // fall through to the error below
        }
        PrivateClient.LOGGER.error("Could not find a method to open a screen on this Minecraft version");
    }

    private static boolean tryOn(Object target, Screen screen) {
        for (String name : NAMES) {
            for (Method m : target.getClass().getMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != 1) continue;
                if (!m.getParameterTypes()[0].isAssignableFrom(Screen.class)) continue;
                try {
                    m.invoke(target, screen);
                    return true;
                } catch (ReflectiveOperationException e) {
                    PrivateClient.LOGGER.warn("Opening screen via {} failed", name, e);
                }
            }
        }
        return false;
    }

    private ScreenOpener() {}
}
