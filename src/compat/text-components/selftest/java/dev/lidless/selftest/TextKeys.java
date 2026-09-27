package dev.lidless.selftest;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

final class TextKeys {
    private TextKeys() {
    }

    static String key(Component text) {
        return text.getContents() instanceof TranslatableContents contents ? contents.getKey() : null;
    }
}
