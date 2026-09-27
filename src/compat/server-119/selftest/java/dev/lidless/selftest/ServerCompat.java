package dev.lidless.selftest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

final class ServerCompat {
    private ServerCompat() {
    }

    static void runCommand(IntegratedServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    static void openInventory(Entity entity, Player player) {
        ((HasCustomInventoryScreen) entity).openCustomInventoryScreen(player);
    }

    static void useBlock(Minecraft mc, BlockHitResult hit) {
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }
}
