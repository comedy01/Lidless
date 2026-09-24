package dev.lidless.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

final class Clicks {
    private Clicks() {
    }

    static void pickup(Minecraft mc, AbstractContainerMenu menu, Slot slot) {
        click(mc, menu, slot, ContainerInput.PICKUP);
    }

    static void quickMove(Minecraft mc, AbstractContainerMenu menu, Slot slot) {
        click(mc, menu, slot, ContainerInput.QUICK_MOVE);
    }

    private static void click(Minecraft mc, AbstractContainerMenu menu, Slot slot, ContainerInput input) {
        mc.gameMode.handleContainerInput(menu.containerId, slot.index, 0, input, mc.player);
    }
}
