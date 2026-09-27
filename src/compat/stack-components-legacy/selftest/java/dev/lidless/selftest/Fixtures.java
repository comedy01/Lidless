package dev.lidless.selftest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

final class Fixtures {
    private Fixtures() {
    }

    static String item(int slot, String id, int count) {
        return "{Slot:" + slot + "b,id:\"minecraft:" + id + "\",count:" + count + "}";
    }

    static String name(String text) {
        return "\"" + text + "\"";
    }

    static String boxItem(int slot, String id, int count) {
        return "{slot:" + slot + ",item:{id:\"minecraft:" + id + "\",count:" + count + "}}";
    }

    static String shulkerBox(String... items) {
        return "minecraft:shulker_box[minecraft:container=[" + String.join(",", items) + "]]";
    }

    static ItemStack shulker(List<ItemStack> contents) {
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        return shulker;
    }

    static List<Component> tooltip(Minecraft mc, ItemStack stack) {
        return stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL);
    }
}
