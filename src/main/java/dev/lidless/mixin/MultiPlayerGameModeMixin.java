package dev.lidless.mixin;

import dev.lidless.memory.InteractionTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void lidless$useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit,
                                   CallbackInfoReturnable<InteractionResult> cir) {
        InteractionTracker.onUseBlock(blockHit.getBlockPos());
    }

    @Inject(method = "interact", at = @At("HEAD"))
    private void lidless$interact(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand,
                                  CallbackInfoReturnable<InteractionResult> cir) {
        InteractionTracker.onInteractEntity(entity);
    }
}
