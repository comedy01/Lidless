package dev.lidless.mixin;

import dev.lidless.memory.InteractionTracker;
import dev.lidless.storage.ContainerScreenAccess;
import dev.lidless.storage.StorageControls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements ContainerScreenAccess {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int titleLabelX;

    @Shadow
    protected Slot hoveredSlot;

    @Unique
    private StorageControls lidless$controls;

    @Unique
    private boolean lidless$opened;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void lidless$init(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        if (!lidless$opened) {
            lidless$opened = true;
            InteractionTracker.onScreenOpened(Minecraft.getInstance(), self);
            lidless$controls = StorageControls.create(self);
        }
        if (lidless$controls != null) {
            lidless$controls.init();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void lidless$layout(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (lidless$controls != null) {
            lidless$controls.layout();
        }
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void lidless$overlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (lidless$controls != null) {
            lidless$controls.drawOverlay(graphics);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void lidless$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (lidless$controls != null && lidless$controls.keyPressed(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lidless$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (lidless$controls != null && lidless$controls.mouseClicked(event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void lidless$removed(CallbackInfo ci) {
        InteractionTracker.onScreenClosed(Minecraft.getInstance(), (AbstractContainerScreen<?>) (Object) this);
    }

    @Override
    public int lidless$leftPos() {
        return leftPos;
    }

    @Override
    public int lidless$topPos() {
        return topPos;
    }

    @Override
    public int lidless$titleLabelX() {
        return titleLabelX;
    }

    @Override
    public Slot lidless$hoveredSlot() {
        return hoveredSlot;
    }

    @Override
    public void lidless$addWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
    }
}
