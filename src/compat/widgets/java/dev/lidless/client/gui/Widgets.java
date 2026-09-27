package dev.lidless.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class Widgets {
    private Widgets() {
    }

    public static int x(AbstractWidget widget) {
        return widget.getX();
    }

    public static int y(AbstractWidget widget) {
        return widget.getY();
    }

    public static void move(AbstractWidget widget, int x, int y) {
        widget.setPosition(x, y);
    }

    public static EditBox searchBox(Font font, int width, int height, Component label, Component hint) {
        EditBox box = new EditBox(font, 0, 0, width, height, label);
        box.setHint(hint);
        return box;
    }

    public static void focus(EditBox box, boolean focused) {
        box.setFocused(focused);
    }
}
