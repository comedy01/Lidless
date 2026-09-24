package dev.lidless.tooltip;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

final class TooltipHiding {
    private TooltipHiding() {
    }

    static boolean showsContents(ItemStack stack) {
        return !stack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP);
    }
}
