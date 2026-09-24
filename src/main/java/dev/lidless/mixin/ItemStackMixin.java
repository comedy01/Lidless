package dev.lidless.mixin;

import dev.lidless.tooltip.TooltipPreviews;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void lidless$preview(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        Optional<TooltipComponent> preview = TooltipPreviews.imageFor((ItemStack) (Object) this);
        if (preview.isPresent()) {
            cir.setReturnValue(preview);
        }
    }
}
