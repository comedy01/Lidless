package dev.lidless.tooltip;

import net.minecraft.world.item.ItemStack;

final class TooltipHiding {
    private TooltipHiding() {
    }

    static boolean showsContents(ItemStack stack) {
        return stack.getTag() == null
                || (stack.getTag().getInt("HideFlags") & ItemStack.TooltipPart.ADDITIONAL.getMask()) == 0;
    }
}
