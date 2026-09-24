package dev.lidless.storage;

import dev.lidless.config.SortOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class StorageActions {
    private StorageActions() {
    }

    public static void sort(Minecraft mc, AbstractContainerMenu menu, List<Slot> section, SortOrder order) {
        if (!ready(mc, menu) || section.isEmpty()) {
            return;
        }
        List<ItemStack> kinds = new ArrayList<>();
        List<Integer> totals = new ArrayList<>();
        int size = section.size();
        int[] kindOf = new int[size];
        int[] counts = new int[size];
        for (int i = 0; i < size; i++) {
            ItemStack stack = section.get(i).getItem();
            if (stack.isEmpty()) {
                kindOf[i] = SortPlanner.EMPTY;
                continue;
            }
            int kind = indexOf(kinds, stack);
            if (kind < 0) {
                kind = kinds.size();
                kinds.add(stack.copyWithCount(1));
                totals.add(0);
            }
            kindOf[i] = kind;
            counts[i] = stack.getCount();
            totals.set(kind, totals.get(kind) + stack.getCount());
        }
        int[] limits = new int[kinds.size()];
        Slot first = section.get(0);
        for (int k = 0; k < kinds.size(); k++) {
            limits[k] = Math.min(kinds.get(k).getMaxStackSize(), first.getMaxStackSize(kinds.get(k)));
        }

        List<Integer> clicks = SortPlanner.plan(kindOf, counts, limits, comparator(order, kinds, totals));
        for (int position : clicks) {
            Clicks.pickup(mc, menu, section.get(position));
        }
    }

    public static void deposit(Minecraft mc, AbstractContainerMenu menu, List<Slot> storage, List<Slot> player) {
        if (!ready(mc, menu)) {
            return;
        }
        List<ItemStack> stored = new ArrayList<>();
        for (Slot slot : storage) {
            if (slot.hasItem()) {
                stored.add(slot.getItem());
            }
        }
        for (Slot slot : player) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && containsItem(stored, stack)) {
                Clicks.quickMove(mc, menu, slot);
            }
        }
    }

    public static void takeAll(Minecraft mc, AbstractContainerMenu menu, List<Slot> storage) {
        if (!ready(mc, menu)) {
            return;
        }
        for (Slot slot : storage) {
            if (slot.hasItem()) {
                Clicks.quickMove(mc, menu, slot);
            }
        }
    }

    private static boolean ready(Minecraft mc, AbstractContainerMenu menu) {
        return mc.gameMode != null && mc.player != null && menu.getCarried().isEmpty();
    }

    private static boolean containsItem(List<ItemStack> stacks, ItemStack stack) {
        for (ItemStack other : stacks) {
            if (ItemStack.isSameItem(other, stack)) {
                return true;
            }
        }
        return false;
    }

    private static int indexOf(List<ItemStack> kinds, ItemStack stack) {
        for (int i = 0; i < kinds.size(); i++) {
            if (Stacks.same(kinds.get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    static Comparator<Integer> comparator(SortOrder order, List<ItemStack> kinds, List<Integer> totals) {
        Comparator<Integer> category = Comparator.comparingInt(k -> BuiltInRegistries.ITEM.getId(kinds.get(k).getItem()));
        Comparator<Integer> name = Comparator.comparing(k -> kinds.get(k).getHoverName().getString().toLowerCase(Locale.ROOT));
        return switch (order) {
            case CATEGORY -> category.thenComparing(name);
            case NAME -> name.thenComparing(category);
            case COUNT -> Comparator.<Integer>comparingInt(totals::get).reversed().thenComparing(category);
            case MOD -> Comparator.<Integer, String>comparing(
                            k -> BuiltInRegistries.ITEM.getKey(kinds.get(k).getItem()).getNamespace())
                    .thenComparing(category);
        };
    }
}
