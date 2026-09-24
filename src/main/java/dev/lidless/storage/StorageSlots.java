package dev.lidless.storage;

import dev.lidless.peek.Mounts;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class StorageSlots {
    private static final int HOTBAR_SIZE = 9;
    private static final int MAIN_END = 36;

    private StorageSlots() {
    }

    public static boolean isStorageMenu(AbstractContainerMenu menu) {
        return menu instanceof ChestMenu
                || menu instanceof ShulkerBoxMenu
                || menu instanceof HopperMenu
                || menu instanceof DispenserMenu
                || Mounts.isMountMenu(menu);
    }

    public static List<Slot> container(AbstractContainerMenu menu) {
        Map<Container, List<Slot>> groups = new IdentityHashMap<>();
        for (Slot slot : menu.slots) {
            if (!(slot.container instanceof Inventory)) {
                groups.computeIfAbsent(slot.container, key -> new ArrayList<>()).add(slot);
            }
        }
        List<Slot> largest = List.of();
        for (List<Slot> group : groups.values()) {
            if (group.size() > largest.size()) {
                largest = group;
            }
        }
        if (Mounts.isMountMenu(menu)) {
            largest = new ArrayList<>(largest);
            largest.removeIf(slot -> slot.getContainerSlot() < Mounts.FIRST_CHEST_SLOT);
        }
        return largest;
    }

    public static List<Slot> playerMain(AbstractContainerMenu menu) {
        List<Slot> slots = new ArrayList<>(MAIN_END - HOTBAR_SIZE);
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory) {
                int index = slot.getContainerSlot();
                if (index >= HOTBAR_SIZE && index < MAIN_END) {
                    slots.add(slot);
                }
            }
        }
        return slots;
    }
}
