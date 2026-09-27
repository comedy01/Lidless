package dev.lidless.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

final class Clicks {
    private Clicks() {
    }

    static void pickup(Minecraft mc, AbstractContainerMenu menu, Slot slot) {
        click(mc, menu, slot, ClickType.PICKUP);
    }

    static void quickMove(Minecraft mc, AbstractContainerMenu menu, Slot slot) {
        click(mc, menu, slot, ClickType.QUICK_MOVE);
    }

    private static void click(Minecraft mc, AbstractContainerMenu menu, Slot slot, ClickType input) {
        mc.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, 0, input, mc.player);
    }
}
