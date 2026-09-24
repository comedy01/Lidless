package dev.lidless.memory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record Remembered(Component title, List<ItemStack> items, long seenAt) {
}
