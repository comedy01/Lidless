package dev.lidless.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public final class Stacks {
    private Stacks() {
    }

    public static boolean same(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }

    public static boolean hasContents(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();
        addItems(items, BlockItem.getBlockEntityData(stack));
        return !items.isEmpty();
    }

    public static void copyContents(ItemStack stack, NonNullList<ItemStack> into) {
        CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) {
            ContainerHelper.loadAllItems(tag, into);
        }
    }

    static void addStored(List<ItemStack> out, ItemStack stack) {
        addItems(out, BlockItem.getBlockEntityData(stack));
        if (stack.getItem() instanceof BundleItem) {
            addItems(out, stack.getTag());
        }
    }

    static void addEnchantments(List<String> texts, ItemStack stack) {
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            texts.add(Component.translatable(enchantment.getDescriptionId()).getString());
        }
    }

    private static void addItems(List<ItemStack> out, CompoundTag tag) {
        if (tag == null) {
            return;
        }
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.of((CompoundTag) entry);
            if (!stack.isEmpty()) {
                out.add(stack);
            }
        }
    }
}
