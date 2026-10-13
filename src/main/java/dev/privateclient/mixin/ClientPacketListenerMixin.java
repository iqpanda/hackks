package dev.privateclient.mixin;

import dev.privateclient.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    /** Anti-knockback: ignore server velocity updates that target the local player. */
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void privateclient$noKnockback(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (Config.antiKnockback && mc.player != null && packet.id() == mc.player.getId()) {
            ci.cancel();
        }
    }
}
