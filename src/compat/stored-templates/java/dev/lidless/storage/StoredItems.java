package dev.lidless.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.stream.Stream;

public final class StoredItems {
    private StoredItems() {
    }

    public static Stream<ItemStack> nonEmpty(ItemContainerContents contents) {
        return contents.nonEmptyItemCopyStream();
    }

    static void addBundle(List<ItemStack> out, BundleContents bundle) {
        for (ItemStackTemplate template : bundle.items()) {
            out.add(template.create());
        }
    }
}
