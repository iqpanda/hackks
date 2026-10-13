package dev.privateclient;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;

/**
 * Draws a through-walls outline around every hit from {@link ScanCache}.
 *
 * This is the part most likely to need a tweak for 26.2, because the rendering API was reworked
 * in 26.x. Isolated here on purpose: the event constant, the Gizmos calls and setAlwaysOnTop()
 * are the only things that touch it. See README.md, "If it doesn't compile".
 */
public final class EspRenderer {
    private static boolean warned = false;

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            try {
                for (ScanCache.Hit hit : ScanCache.hits()) {
                    AABB box = new AABB(hit.pos()).inflate(0.003);
                    Gizmos.cuboid(box, GizmoStyle.stroke(hit.argb())).setAlwaysOnTop();
                }
            } catch (Throwable t) {
                if (!warned) {
                    warned = true;
                    PrivateClient.LOGGER.error("ESP drawing failed (logged once)", t);
                }
            }
        });
    }

    private EspRenderer() {}
}
