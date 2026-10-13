package dev.privateclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** All settings live here as plain static fields; saved to config/privateclient.json. */
public final class Config {
    public static boolean storageEsp = true;
    public static boolean oreEsp = true;
    public static boolean ancientDebris = true;
    public static boolean diamondOre = true;
    public static boolean deepslateDiamond = true;
    public static boolean deepslateEmerald = true;
    public static boolean antiKnockback = true;
    public static int range = 32;
    public static float freecamSpeed = 1.0f;

    /** Not saved: freecam always starts off. */
    public static boolean freecam = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("privateclient.json");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) {
            save();
            return;
        }
        try {
            JsonObject o = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            storageEsp = bool(o, "storageEsp", storageEsp);
            oreEsp = bool(o, "oreEsp", oreEsp);
            ancientDebris = bool(o, "ancientDebris", ancientDebris);
            diamondOre = bool(o, "diamondOre", diamondOre);
            deepslateDiamond = bool(o, "deepslateDiamond", deepslateDiamond);
            deepslateEmerald = bool(o, "deepslateEmerald", deepslateEmerald);
            antiKnockback = bool(o, "antiKnockback", antiKnockback);
            if (o.has("range")) range = Math.max(8, Math.min(128, o.get("range").getAsInt()));
            if (o.has("freecamSpeed")) freecamSpeed = o.get("freecamSpeed").getAsFloat();
        } catch (Exception e) {
            PrivateClient.LOGGER.warn("Could not read config, using defaults", e);
        }
    }

    public static void save() {
        JsonObject o = new JsonObject();
        o.addProperty("storageEsp", storageEsp);
        o.addProperty("oreEsp", oreEsp);
        o.addProperty("ancientDebris", ancientDebris);
        o.addProperty("diamondOre", diamondOre);
        o.addProperty("deepslateDiamond", deepslateDiamond);
        o.addProperty("deepslateEmerald", deepslateEmerald);
        o.addProperty("antiKnockback", antiKnockback);
        o.addProperty("range", range);
        o.addProperty("freecamSpeed", freecamSpeed);
        try {
            Files.writeString(file(), GSON.toJson(o));
        } catch (IOException e) {
            PrivateClient.LOGGER.warn("Could not save config", e);
        }
    }

    private static boolean bool(JsonObject o, String key, boolean fallback) {
        return o.has(key) ? o.get(key).getAsBoolean() : fallback;
    }

    private Config() {}
}
