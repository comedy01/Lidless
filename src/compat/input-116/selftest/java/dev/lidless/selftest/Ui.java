package dev.lidless.selftest;

import dev.lidless.selftest.mixin.MouseHandlerMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

final class Ui {
    static final int KEY_E = GLFW.GLFW_KEY_E;
    static final int KEY_F = GLFW.GLFW_KEY_F;
    static final int KEY_ESCAPE = GLFW.GLFW_KEY_ESCAPE;
    static final int MOD_CONTROL = GLFW.GLFW_MOD_CONTROL;

    private Ui() {
    }

    static void click(Screen screen, double x, double y) {
        screen.mouseClicked(x, y, 0);
        screen.mouseReleased(x, y, 0);
    }

    static void key(Screen screen, int key, int modifiers) {
        screen.keyPressed(key, 0, modifiers);
    }

    static void moveMouse(Minecraft mc, double x, double y) {
        MouseHandlerMixin mouse = (MouseHandlerMixin) mc.mouseHandler;
        mouse.lidless$setX(x);
        mouse.lidless$setY(y);
    }
}
