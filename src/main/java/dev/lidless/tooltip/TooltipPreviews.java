package dev.lidless.tooltip;

import dev.lidless.client.LidlessClient;
import dev.lidless.peek.ItemGrid;
import dev.lidless.peek.PeekResolver;
import dev.lidless.storage.StoredItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TooltipPreviews {
    private static final int ROW = 9;
    private static final int MAX_SLOTS = 256;
    private static final int SHULKER_SIZE = 27;
    private static final int SHULKER_TINT = 0xFF8B5E8B;
    private static final int CHEST_TINT = 0xFF6B4A2B;
    private static final int ENDER_TINT = 0xFF1B4A48;
    private static final int[] DYE_COLORS = {
            0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
            0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
    };

    private TooltipPreviews() {
    }

    public static boolean enabled() {
        return LidlessClient.config().tooltipPreview();
    }

    public static boolean hasContents(ItemContainerContents contents) {
        return contents != null && StoredItems.nonEmpty(contents).findAny().isPresent();
    }

    public static Optional<TooltipComponent> imageFor(ItemStack stack) {
        if (!enabled() || stack.isEmpty()) {
            return Optional.empty();
        }
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (hasContents(contents)) {
            if (!TooltipHiding.showsContents(stack)) {
                return Optional.empty();
            }
            boolean shulker = isShulker(stack);
            return Optional.of(new ContainerPreview(items(contents, shulker ? SHULKER_SIZE : 0), shulker ? shulkerTint(stack) : CHEST_TINT));
        }
        if (stack.is(Items.ENDER_CHEST)) {
            List<ItemStack> ender = PeekResolver.enderItems(Minecraft.getInstance());
            if (ender != null && !ItemGrid.isEmpty(ender)) {
                return Optional.of(new ContainerPreview(ender, ENDER_TINT));
            }
        }
        return Optional.empty();
    }

    private static List<ItemStack> items(ItemContainerContents contents, int minimum) {
        NonNullList<ItemStack> all = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
        contents.copyInto(all);
        int last = all.size() - 1;
        while (last >= 0 && all.get(last).isEmpty()) {
            last--;
        }
        int size = Math.max(minimum, (last + ROW) / ROW * ROW);
        return new ArrayList<>(all.subList(0, Math.min(size, all.size())));
    }

    private static boolean isShulker(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock;
    }

    private static int shulkerTint(ItemStack stack) {
        DyeColor color = ((ShulkerBoxBlock) ((BlockItem) stack.getItem()).getBlock()).getColor();
        if (color == null || color.getId() >= DYE_COLORS.length) {
            return SHULKER_TINT;
        }
        return darken(DYE_COLORS[color.getId()]);
    }

    static int darken(int rgb) {
        int r = (rgb >> 16 & 0xFF) * 3 / 5;
        int g = (rgb >> 8 & 0xFF) * 3 / 5;
        int b = (rgb & 0xFF) * 3 / 5;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
