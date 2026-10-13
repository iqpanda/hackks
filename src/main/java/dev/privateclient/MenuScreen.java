package dev.privateclient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Two-column menu built only from stock widgets so it doesn't depend on the GUI draw API. */
public final class MenuScreen extends Screen {
    private static final int W = 150;
    private static final int H = 20;
    private static final int GAP = 6;
    private static final int ROW = 24;
    private static final int[] RANGES = {16, 24, 32, 48, 64, 96};
    private static final float[] SPEEDS = {0.5f, 1.0f, 2.0f, 3.0f};

    public MenuScreen() {
        super(Component.literal("Private Client"));
    }

    @Override
    protected void init() {
        int left = width / 2 - W - GAP / 2;
        int right = width / 2 + GAP / 2;
        int y = height / 2 - 96;

        addRenderableWidget(new StringWidget(width / 2 - 120, y - 22, 240, 12,
                Component.literal("PRIVATE CLIENT").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), font));

        toggle(left, y, "Storage ESP", () -> Config.storageEsp, v -> Config.storageEsp = v);
        toggle(right, y, "Ore ESP", () -> Config.oreEsp, v -> Config.oreEsp = v);
        y += ROW;
        toggle(left, y, "Ancient Debris", () -> Config.ancientDebris, v -> Config.ancientDebris = v);
        toggle(right, y, "Diamond Ore", () -> Config.diamondOre, v -> Config.diamondOre = v);
        y += ROW;
        toggle(left, y, "Deepslate Diamond", () -> Config.deepslateDiamond, v -> Config.deepslateDiamond = v);
        toggle(right, y, "Deepslate Emerald", () -> Config.deepslateEmerald, v -> Config.deepslateEmerald = v);
        y += ROW;
        toggle(left, y, "Anti-Knockback", () -> Config.antiKnockback, v -> Config.antiKnockback = v);
        toggle(right, y, "Freecam", () -> Config.freecam, v -> Freecam.setActive(Minecraft.getInstance(), v));
        y += ROW;

        Button range = Button.builder(rangeLabel(), b -> {
            Config.range = next(RANGES, Config.range);
            Config.save();
            b.setMessage(rangeLabel());
        }).bounds(left, y, W, H).build();
        addRenderableWidget(range);

        Button speed = Button.builder(speedLabel(), b -> {
            Config.freecamSpeed = next(SPEEDS, Config.freecamSpeed);
            Config.save();
            b.setMessage(speedLabel());
        }).bounds(right, y, W, H).build();
        addRenderableWidget(speed);
        y += ROW;

        addRenderableWidget(Button.builder(Component.literal("Gamemode: Spectator"),
                b -> command("gamemode spectator")).bounds(left, y, W, H).build());
        addRenderableWidget(Button.builder(Component.literal("Gamemode: Creative"),
                b -> command("gamemode creative")).bounds(right, y, W, H).build());
        y += ROW;

        addRenderableWidget(Button.builder(Component.literal("Gamemode: Survival"),
                b -> command("gamemode survival")).bounds(left, y, W, H).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),
                b -> onClose()).bounds(right, y, W, H).build());
        y += ROW + 4;

        addRenderableWidget(new StringWidget(width / 2 - 150, y, 300, 10,
                Component.literal("Right Shift: menu    G: freecam    (gamemode buttons need cheats on)")
                        .withStyle(ChatFormatting.GRAY), font));
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        Button button = Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            Config.save();
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x, y, W, H).build();
        addRenderableWidget(button);
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": ")
                .append(Component.literal(on ? "ON" : "OFF")
                        .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static Component rangeLabel() {
        return Component.literal("Range: " + Config.range + " blocks");
    }

    private static Component speedLabel() {
        return Component.literal("Freecam Speed: " + Config.freecamSpeed + "x");
    }

    private static int next(int[] values, int current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) return values[(i + 1) % values.length];
        }
        return values[0];
    }

    private static float next(float[] values, float current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) return values[(i + 1) % values.length];
        }
        return values[0];
    }

    private static void command(String cmd) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.connection.sendCommand(cmd);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
