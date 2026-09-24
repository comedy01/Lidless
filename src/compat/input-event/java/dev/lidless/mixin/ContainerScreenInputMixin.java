package dev.lidless.mixin;

import dev.lidless.storage.ContainerScreenAccess;
import dev.lidless.storage.StorageControls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenInputMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void lidless$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        boolean control = event.hasControlDown() || Minecraft.getInstance().hasControlDown();
        if (controls != null && controls.keyPressed(event.key(), control, event.isEscape() || event.isConfirmation(),
                box -> box.keyPressed(event))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lidless$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null && controls.mouseClicked(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }
}
