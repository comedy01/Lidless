package dev.lidless.mixin;

import dev.lidless.hud.Canvas;
import dev.lidless.storage.ContainerScreenAccess;
import dev.lidless.storage.StorageControls;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenRenderMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void lidless$layout(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null) {
            controls.layout();
        }
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void lidless$overlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null) {
            controls.drawOverlay(new Canvas(graphics));
        }
    }
}
