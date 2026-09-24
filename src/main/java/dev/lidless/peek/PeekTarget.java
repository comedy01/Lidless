package dev.lidless.peek;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PeekTarget(Component title, ItemStack icon, List<ItemStack> items, Status status, long seenAt) {
    public enum Status {
        LIVE,
        REMEMBERED,
        UNKNOWN,
        LOOT
    }

    public int columns() {
        int size = items.size();
        if (size == 0 || size % 9 == 0) {
            return 9;
        }
        return Math.min(size, 9);
    }
}
