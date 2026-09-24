package dev.lidless.mixin;

import dev.lidless.storage.Stacks;
import dev.lidless.tooltip.TooltipPreviews;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
public abstract class ContainerTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void lidless$hideList(ItemStack stack, BlockGetter level, List<Component> lines, TooltipFlag flag,
                                  CallbackInfo ci) {
        if (TooltipPreviews.enabled() && Stacks.hasContents(stack)) {
            ci.cancel();
        }
    }
}
