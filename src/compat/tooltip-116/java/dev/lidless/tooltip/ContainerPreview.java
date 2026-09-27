package dev.lidless.tooltip;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ContainerPreview(List<ItemStack> items, int tint) {
}
