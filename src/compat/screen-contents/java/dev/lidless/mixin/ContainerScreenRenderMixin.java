package dev.lidless.mixin;

import dev.lidless.hud.Canvas;
import dev.lidless.storage.ContainerScreenAccess;
import dev.lidless.storage.StorageControls;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void lidless$layout(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null) {
            controls.layout();
        }
    }

    @Inject(method = "renderContents", at = @At("TAIL"))
    private void lidless$overlay(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null) {
            controls.drawOverlay(new Canvas(graphics));
        }
    }
}
