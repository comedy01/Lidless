package dev.lidless.memory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

final class MemoryCodec {
    MemoryCodec(HolderLookup.Provider registries) {
    }

    Optional<JsonElement> items(List<ItemStack> items) {
        NonNullList<ItemStack> all = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            all.set(i, items.get(i));
        }
        return Optional.of(new JsonPrimitive(ContainerHelper.saveAllItems(new CompoundTag(), all).toString()));
    }

    void readItems(JsonElement json, NonNullList<ItemStack> into) {
        try {
            ContainerHelper.loadAllItems(TagParser.parseTag(json.getAsString()), into);
        } catch (CommandSyntaxException e) {
            into.clear();
        }
    }

    Optional<JsonElement> title(Component title) {
        return Optional.of(Component.Serializer.toJsonTree(title));
    }

    Component readTitle(JsonElement json) {
        try {
            return Component.Serializer.fromJson(json);
        } catch (JsonParseException e) {
            return null;
        }
    }
}
