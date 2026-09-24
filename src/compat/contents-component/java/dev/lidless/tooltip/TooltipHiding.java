package dev.lidless.tooltip;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

final class TooltipHiding {
    private TooltipHiding() {
    }

    static boolean showsContents(ItemStack stack) {
        return stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).shows(DataComponents.CONTAINER);
    }
}
