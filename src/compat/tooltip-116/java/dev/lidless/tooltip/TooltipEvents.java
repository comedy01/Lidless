package dev.lidless.tooltip;

import dev.lidless.client.Texts;
import dev.lidless.hud.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.Collections;
import java.util.List;

public final class TooltipEvents {
    private static final int LINE_HEIGHT = 10;
    private static final int TITLE_GAP = 2;
    private static final int Z = 400;

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static PreviewPanel lastPanel;
    private static int lastBlanks;

    private TooltipEvents() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, TooltipEvents::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(TooltipEvents::onPostText);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ContainerPreview preview = TooltipPreviews.imageFor(stack).orElse(null);
        if (preview == null) {
            return;
        }
        PreviewPanel panel = new PreviewPanel(preview);
        Font font = Minecraft.getInstance().font;
        int spaces = (panel.width() + font.width(" ") - 1) / font.width(" ");
        Component blank = Texts.literal(String.join("", Collections.nCopies(spaces, " ")));
        int blanks = (panel.height() + LINE_HEIGHT - 1) / LINE_HEIGHT;
        List<Component> lines = event.getToolTip();
        for (int i = 0; i < blanks; i++) {
            lines.add(blank);
        }
        lastStack = stack;
        lastPanel = panel;
        lastBlanks = blanks;
    }

    private static void onPostText(RenderTooltipEvent.PostText event) {
        if (lastPanel == null || event.getStack() != lastStack) {
            return;
        }
        int first = event.getLines().size() - lastBlanks;
        if (first < 0) {
            return;
        }
        int y = event.getY() + first * LINE_HEIGHT + (first > 0 ? TITLE_GAP : 0);
        ItemRenderer items = Minecraft.getInstance().getItemRenderer();
        float old = items.blitOffset;
        items.blitOffset = Z;
        lastPanel.draw(new Canvas(event.getMatrixStack(), Z), Minecraft.getInstance().font, event.getX(), y);
        items.blitOffset = old;
    }
}
