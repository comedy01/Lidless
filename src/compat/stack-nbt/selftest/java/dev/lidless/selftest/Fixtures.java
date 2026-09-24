package dev.lidless.selftest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

final class Fixtures {
    private Fixtures() {
    }

    static String item(int slot, String id, int count) {
        return "{Slot:" + slot + "b,id:\"minecraft:" + id + "\",Count:" + count + "b}";
    }

    static String name(String text) {
        return "'\"" + text + "\"'";
    }

    static String boxItem(int slot, String id, int count) {
        return item(slot, id, count);
    }

    static String shulkerBox(String... items) {
        return "minecraft:shulker_box{BlockEntityTag:{Items:[" + String.join(",", items) + "]}}";
    }

    static ItemStack shulker(List<ItemStack> contents) {
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        NonNullList<ItemStack> items = NonNullList.withSize(contents.size(), ItemStack.EMPTY);
        for (int i = 0; i < contents.size(); i++) {
            items.set(i, contents.get(i));
        }
        ContainerHelper.saveAllItems(shulker.getOrCreateTagElement("BlockEntityTag"), items);
        return shulker;
    }

    static List<Component> tooltip(Minecraft mc, ItemStack stack) {
        return stack.getTooltipLines(mc.player, TooltipFlag.NORMAL);
    }
}
