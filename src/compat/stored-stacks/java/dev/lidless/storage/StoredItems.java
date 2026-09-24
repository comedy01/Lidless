package dev.lidless.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.stream.Stream;

public final class StoredItems {
    private StoredItems() {
    }

    public static Stream<ItemStack> nonEmpty(ItemContainerContents contents) {
        return contents.nonEmptyStream();
    }

    static void addBundle(List<ItemStack> out, BundleContents bundle) {
        for (ItemStack stack : bundle.items()) {
            out.add(stack);
        }
    }
}
