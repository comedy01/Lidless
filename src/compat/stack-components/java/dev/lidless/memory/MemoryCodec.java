package dev.lidless.memory;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.Optional;

final class MemoryCodec {
    private final DynamicOps<JsonElement> ops;

    MemoryCodec(HolderLookup.Provider registries) {
        this.ops = registries.createSerializationContext(JsonOps.INSTANCE);
    }

    Optional<JsonElement> items(List<ItemStack> items) {
        return ItemContainerContents.CODEC.encodeStart(ops, ItemContainerContents.fromItems(items)).result();
    }

    void readItems(JsonElement json, NonNullList<ItemStack> into) {
        ItemContainerContents.CODEC.parse(ops, json).result().orElse(ItemContainerContents.EMPTY).copyInto(into);
    }

    Optional<JsonElement> title(Component title) {
        return ComponentSerialization.CODEC.encodeStart(ops, title).result();
    }

    Component readTitle(JsonElement json) {
        return ComponentSerialization.CODEC.parse(ops, json).result().orElse(null);
    }
}
