package dev.privateclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side freecam. The camera is detached from the player (see CameraMixin) and moved
 * with the normal movement keys; the real player stands still (see KeyboardInputMixin).
 * Mouse look still turns the player, and the camera follows that rotation.
 */
public final class Freecam {
    private static Vec3 pos = Vec3.ZERO;
    private static long lastNanos;

    public static Vec3 position() {
        return pos;
    }

    public static void toggle(Minecraft mc) {
        setActive(mc, !Config.freecam);
    }

    public static void setActive(Minecraft mc, boolean on) {
        if (on && mc.player != null) {
            pos = mc.player.getEyePosition();
            lastNanos = System.nanoTime();
            Config.freecam = true;
        } else {
            Config.freecam = false;
        }
    }

    /** Called once per rendered frame while freecam is on. */
    public static void frame(Minecraft mc) {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNanos) / 1.0e9, 0.1);
        lastNanos = now;

        LocalPlayer player = mc.player;
        if (player == null) return;
        Options o = mc.options;

        Vec3 look = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
        Vec3 flat = Vec3.directionFromRotation(0.0f, player.getYRot());
        Vec3 right = new Vec3(-flat.z, 0.0, flat.x);

        Vec3 move = Vec3.ZERO;
        if (o.keyUp.isDown()) move = move.add(look);
        if (o.keyDown.isDown()) move = move.subtract(look);
        if (o.keyRight.isDown()) move = move.add(right);
        if (o.keyLeft.isDown()) move = move.subtract(right);
        if (o.keyJump.isDown()) move = move.add(0.0, 1.0, 0.0);
        if (o.keyShift.isDown()) move = move.subtract(0.0, 1.0, 0.0);

        if (move.lengthSqr() > 0.0) {
            double speed = 12.0 * Config.freecamSpeed * (o.keySprint.isDown() ? 3.0 : 1.0);
            pos = pos.add(move.normalize().scale(speed * dt));
        }
    }

    private Freecam() {}
}
