package dev.lidless.storage;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

public final class Stacks {
    private Stacks() {
    }

    public static boolean same(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static boolean hasContents(ItemStack stack) {
        return hasContents(stack.get(DataComponents.CONTAINER));
    }

    public static boolean hasContents(ItemContainerContents contents) {
        return contents != null && StoredItems.nonEmpty(contents).findAny().isPresent();
    }

    public static void copyContents(ItemStack stack, NonNullList<ItemStack> into) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(into);
        }
    }

    static void addStored(List<ItemStack> out, ItemStack stack) {
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            StoredItems.nonEmpty(container).forEach(out::add);
        }
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            StoredItems.addBundle(out, bundle);
        }
    }

    static void addEnchantments(List<String> texts, ItemStack stack) {
        addEnchantments(texts, stack.get(DataComponents.ENCHANTMENTS));
        addEnchantments(texts, stack.get(DataComponents.STORED_ENCHANTMENTS));
    }

    private static void addEnchantments(List<String> texts, ItemEnchantments enchantments) {
        if (enchantments == null) {
            return;
        }
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            texts.add(enchantment.value().description().getString());
        }
    }
}
