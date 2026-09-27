package dev.lidless.selftest;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

final class ServerCompat {
    private ServerCompat() {
    }

    static void runCommand(IntegratedServer server, String command) {
        server.getCommands().performCommand(server.createCommandSourceStack(), command);
    }

    static void openInventory(Entity entity, Player player) {
        ((AbstractHorse) entity).openInventory(player);
    }

    static void useBlock(Minecraft mc, BlockHitResult hit) {
        mc.gameMode.useItemOn(mc.player, mc.level, InteractionHand.MAIN_HAND, hit);
    }

    static Optional<?> tooltipImage(ItemStack stack) {
        return stack.getTooltipImage();
    }

    static Inventory inventory(Player player) {
        return player.getInventory();
    }

    static void screenshot(Minecraft mc, RenderTarget target) {
        Screenshot.grab(mc.gameDirectory, target, message -> {
        });
    }
}
