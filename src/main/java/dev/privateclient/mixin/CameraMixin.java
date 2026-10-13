package dev.privateclient.mixin;

import dev.privateclient.Config;
import dev.privateclient.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    /** After vanilla positions the camera each frame, move it to the freecam position. */
    @Inject(method = "setup", at = @At("TAIL"))
    private void privateclient$freecam(CallbackInfo ci) {
        if (!Config.freecam) return;
        Freecam.frame(Minecraft.getInstance());
        Vec3 p = Freecam.position();
        setPosition(p.x, p.y, p.z);
    }
}
