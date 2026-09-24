package dev.lidless.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lidless.storage.ContainerScreenAccess;
import dev.lidless.storage.StorageControls;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenInputMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void lidless$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        boolean leave = keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_RETURN
                || keyCode == InputConstants.KEY_NUMPADENTER;
        boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || Screen.hasControlDown();
        if (controls != null && controls.keyPressed(keyCode, control, leave,
                box -> box.keyPressed(keyCode, scanCode, modifiers))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lidless$mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        StorageControls controls = ((ContainerScreenAccess) this).lidless$controls();
        if (controls != null && controls.mouseClicked(x, y, button)) {
            cir.setReturnValue(true);
        }
    }
}
