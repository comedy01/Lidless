package dev.lidless.showcase.mixin;

import dev.lidless.showcase.Showcase;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftFrameMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void lidlessShowcase$frame(boolean advanceGameTime, CallbackInfo ci) {
        Showcase.frame();
    }
}
