package dev.lidless.selftest;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

final class TextKeys {
    private TextKeys() {
    }

    static String key(Component text) {
        return text instanceof TranslatableComponent translatable ? translatable.getKey() : null;
    }
}
