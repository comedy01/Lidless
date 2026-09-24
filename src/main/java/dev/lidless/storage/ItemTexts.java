package dev.lidless.storage;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

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
        Stacks.addEnchantments(texts, stack);
        return texts;
    }

    private static List<ItemStack> contents(ItemStack stack) {
        List<ItemStack> inner = new ArrayList<>();
        Stacks.addStored(inner, stack);
        return inner;
    }
}
