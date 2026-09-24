package dev.lidless.storage;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

public final class ItemTexts {
    private ItemTexts() {
    }

    public static boolean matches(SearchQuery query, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (query.matches(namespace(stack), texts(stack))) {
            return true;
        }
        for (ItemStack inner : contents(stack)) {
            if (query.matches(namespace(inner), texts(inner))) {
                return true;
            }
        }
        return false;
    }

    private static String namespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }

    private static List<String> texts(ItemStack stack) {
        List<String> texts = new ArrayList<>(3);
        texts.add(stack.getHoverName().getString());
        texts.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
        addEnchantments(texts, stack.get(DataComponents.ENCHANTMENTS));
        addEnchantments(texts, stack.get(DataComponents.STORED_ENCHANTMENTS));
        return texts;
    }

    private static void addEnchantments(List<String> texts, ItemEnchantments enchantments) {
        if (enchantments == null) {
            return;
        }
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            texts.add(enchantment.value().description().getString());
        }
    }

    private static List<ItemStack> contents(ItemStack stack) {
        List<ItemStack> inner = new ArrayList<>();
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            StoredItems.nonEmpty(container).forEach(inner::add);
        }
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            StoredItems.addBundle(inner, bundle);
        }
        return inner;
    }
}
