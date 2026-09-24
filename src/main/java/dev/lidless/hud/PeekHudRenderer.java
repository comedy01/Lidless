package dev.lidless.hud;

import dev.lidless.client.LidlessClient;
import dev.lidless.config.LidlessConfig;
import dev.lidless.config.LidlessPolicy;
import dev.lidless.info.Counts;
import dev.lidless.peek.ItemGrid;
import dev.lidless.peek.PeekResolver;
import dev.lidless.peek.PeekTarget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PeekHudRenderer {
    private static final int PADDING = 5;
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 5;
    private static final int LINE_HEIGHT = 10;
    private static final int SECTION_GAP = 4;
    private static final int BACKGROUND = 0xC0100C08;
    private static final int BORDER = 0xB0A8742C;
    private static final int SLOT = 0x30FFFFFF;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int STATUS_COLOR = 0xFFB0B0B0;
    private static final int NOTE_COLOR = 0xFF909090;

    private static PeekTarget cached;
    private static long cachedTick = Long.MIN_VALUE;
    private static Object cachedHit;

    private PeekHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || HudCompat.isScreenOpen(mc) || HudCompat.isGuiHidden(mc)) {
            return;
        }
        LidlessConfig config = LidlessClient.config();
        if (!config.peek() || config.requireSneak() && !mc.player.isShiftKeyDown()) {
            return;
        }
        PeekTarget target = current(mc);
        if (target != null) {
            draw(graphics, mc.font, target, config);
        }
    }

    public static PeekTarget current(Minecraft mc) {
        long tick = mc.level.getGameTime();
        if (tick != cachedTick || mc.hitResult != cachedHit) {
            cached = PeekResolver.resolve(mc);
            cachedTick = tick;
            cachedHit = mc.hitResult;
        }
        return cached;
    }

    private static void draw(GuiGraphicsExtractor graphics, Font font, PeekTarget target, LidlessConfig config) {
        boolean icon = !target.icon().isEmpty();
        Component status = statusLine(target);

        List<ItemGrid.Cell> cells = List.of();
        int columns = target.columns();
        Component note = null;
        boolean hasItems = !ItemGrid.isEmpty(target.items());
        if (hasItems) {
            if (config.compact()) {
                cells = ItemGrid.compact(target.items());
                columns = Math.min(9, cells.size());
            } else {
                cells = ItemGrid.slots(target.items());
            }
        } else if (target.status() == PeekTarget.Status.LIVE || target.status() == PeekTarget.Status.REMEMBERED) {
            note = Component.translatable("lidless.peek.empty");
        }

        int shown = Math.min(cells.size(), config.rows() * columns);
        if (shown < cells.size()) {
            note = Component.translatable("lidless.peek.more", cells.size() - shown);
        }

        int headerText = Math.max(font.width(target.title()), status == null ? 0 : font.width(status));
        int headerHeight = status == null ? LINE_HEIGHT - 1 : LINE_HEIGHT * 2 - 1;
        headerHeight = icon ? Math.max(headerHeight, ICON_SIZE) : headerHeight;
        int headerWidth = headerText + (icon ? ICON_SIZE + ICON_GAP : 0);

        int gridRows = ItemGrid.rows(shown, Math.max(columns, 1));
        int gridWidth = shown == 0 ? 0 : columns * ItemGrid.CELL - 1;
        int gridHeight = gridRows * ItemGrid.CELL - 1;

        int contentWidth = Math.max(headerWidth, Math.max(gridWidth, note == null ? 0 : font.width(note)));
        int contentHeight = headerHeight;
        if (shown > 0) {
            contentHeight += SECTION_GAP + gridHeight;
        }
        if (note != null) {
            contentHeight += SECTION_GAP + LINE_HEIGHT - 1;
        }

        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = contentHeight + PADDING * 2;
        int left = LidlessPolicy.place(config.xPosition(), graphics.guiWidth(), boxWidth);
        int top = LidlessPolicy.place(config.yPosition(), graphics.guiHeight(), boxHeight);

        graphics.fill(left, top, left + boxWidth, top + boxHeight, BORDER);
        graphics.fill(left + 1, top + 1, left + boxWidth - 1, top + boxHeight - 1, BACKGROUND);

        int x = left + PADDING;
        int y = top + PADDING;
        if (icon) {
            graphics.item(target.icon(), x, y + (headerHeight - ICON_SIZE) / 2);
        }
        int textX = x + (icon ? ICON_SIZE + ICON_GAP : 0);
        int textHeight = status == null ? LINE_HEIGHT - 1 : LINE_HEIGHT * 2 - 1;
        int textY = y + (headerHeight - textHeight) / 2;
        graphics.text(font, target.title(), textX, textY, TITLE_COLOR, true);
        if (status != null) {
            graphics.text(font, status, textX, textY + LINE_HEIGHT, STATUS_COLOR, false);
        }
        y += headerHeight;

        if (shown > 0) {
            y += SECTION_GAP;
            ItemGrid.draw(graphics, font, cells, columns, shown, x, y, SLOT);
            y += gridHeight;
        }
        if (note != null) {
            y += SECTION_GAP;
            graphics.text(font, note, x, y, NOTE_COLOR, false);
        }
    }

    private static Component statusLine(PeekTarget target) {
        return switch (target.status()) {
            case LIVE -> Component.translatable("lidless.peek.slots", used(target.items()), target.items().size());
            case REMEMBERED -> Component.translatable("lidless.peek.remembered",
                    Counts.ago(System.currentTimeMillis() - target.seenAt()));
            case UNKNOWN -> Component.translatable("lidless.peek.unknown");
            case LOOT -> Component.translatable("lidless.peek.loot");
        };
    }

    private static int used(List<ItemStack> items) {
        int used = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                used++;
            }
        }
        return used;
    }
}
