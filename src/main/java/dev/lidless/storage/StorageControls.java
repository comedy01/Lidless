package dev.lidless.storage;

import dev.lidless.client.LidlessClient;
import com.mojang.blaze3d.platform.InputConstants;
import dev.lidless.config.LidlessConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class StorageControls {
    private static final int BUTTON = 12;
    private static final int ICON = 8;
    private static final int GAP = 1;
    private static final int SEARCH_WIDTH = 86;
    private static final int SEARCH_MIN_WIDTH = 48;
    private static final int SEARCH_HEIGHT = 12;
    private static final int DIM = 0xB0101010;
    private static final int MATCH = 0xFFE0B040;

    private final AbstractContainerScreen<?> screen;
    private final ContainerScreenAccess access;
    private final AbstractContainerMenu menu;
    private final List<Slot> storage;
    private final List<Slot> player;
    private final Map<Slot, ItemStack> seen = new IdentityHashMap<>();
    private final Map<Slot, Boolean> matched = new IdentityHashMap<>();
    private EditBox search;
    private Button sortButton;
    private Button depositButton;
    private Button takeButton;
    private Button playerSortButton;
    private SearchQuery query = SearchQuery.parse("");
    private String searchText = "";

    private StorageControls(AbstractContainerScreen<?> screen, List<Slot> storage, List<Slot> player) {
        this.screen = screen;
        this.access = (ContainerScreenAccess) screen;
        this.menu = screen.getMenu();
        this.storage = storage;
        this.player = player;
    }

    public static StorageControls create(AbstractContainerScreen<?> screen) {
        if (screen instanceof CreativeModeInventoryScreen) {
            return null;
        }
        AbstractContainerMenu menu = screen.getMenu();
        List<Slot> storage = StorageSlots.isStorageMenu(menu) ? StorageSlots.container(menu) : List.of();
        if (storage.size() < 3) {
            storage = List.of();
        }
        if (storage.isEmpty() && !(screen instanceof InventoryScreen)) {
            return null;
        }
        return new StorageControls(screen, storage, StorageSlots.playerMain(menu));
    }

    public void init() {
        LidlessConfig config = LidlessClient.config();
        Minecraft mc = Minecraft.getInstance();
        search = null;
        sortButton = null;
        depositButton = null;
        takeButton = null;
        playerSortButton = null;

        if (!storage.isEmpty() && config.sortButtons()) {
            Component order = Component.translatable(config.sortOrder().translationKey());
            sortButton = button("sort", Component.translatable("lidless.button.sort", order),
                    () -> StorageActions.sort(mc, menu, storage, LidlessClient.config().sortOrder()));
            depositButton = button("deposit", Component.translatable("lidless.button.deposit"),
                    () -> StorageActions.deposit(mc, menu, storage, player));
            takeButton = button("take", Component.translatable("lidless.button.take"),
                    () -> StorageActions.takeAll(mc, menu, storage));
        }
        if (!player.isEmpty() && config.sortButtons()) {
            Component order = Component.translatable(config.sortOrder().translationKey());
            playerSortButton = button("sort", Component.translatable("lidless.button.sort_inventory", order),
                    () -> StorageActions.sort(mc, menu, player, LidlessClient.config().sortOrder()));
        }
        if (!storage.isEmpty() && config.searchBox()) {
            Font font = mc.font;
            search = new EditBox(font, 0, 0, SEARCH_WIDTH, SEARCH_HEIGHT, Component.translatable("lidless.search"));
            search.setMaxLength(50);
            search.setHint(Component.translatable("lidless.search.hint").withStyle(EditBox.SEARCH_HINT_STYLE));
            search.setValue(searchText);
            search.setResponder(this::onSearch);
            access.lidless$addWidget(search);
            onSearch(searchText);
        }
        layout();
    }

    private Button button(String sprite, Component label, Runnable action) {
        Button button = SpriteIconButton.builder(label, pressed -> action.run(), true)
                .size(BUTTON, BUTTON)
                .sprite(Identifier.fromNamespaceAndPath(LidlessClient.MOD_ID, sprite), ICON, ICON)
                .build();
        button.setTooltip(Tooltip.create(label));
        access.lidless$addWidget(button);
        return button;
    }

    private void onSearch(String text) {
        searchText = text;
        query = SearchQuery.parse(text);
        seen.clear();
        matched.clear();
    }

    public void layout() {
        int left = access.lidless$leftPos();
        int top = access.lidless$topPos();

        if (!storage.isEmpty()) {
            Slot corner = topRight(storage);
            int right = left + corner.x + 17;
            int y = top + corner.y - BUTTON - 3;
            int x = right;
            for (Button button : new Button[] {takeButton, depositButton, sortButton}) {
                if (button != null) {
                    x -= BUTTON;
                    button.setPosition(x, y);
                    x -= GAP;
                }
            }
            if (search != null) {
                Font font = Minecraft.getInstance().font;
                int titleEnd = left + access.lidless$titleLabelX() + font.width(screen.getTitle()) + 4;
                int available = x - 2 - titleEnd;
                int width = Math.min(SEARCH_WIDTH, available);
                if (width < SEARCH_MIN_WIDTH) {
                    width = SEARCH_WIDTH;
                    if (top >= SEARCH_HEIGHT + 4) {
                        search.setPosition(right - width, top - SEARCH_HEIGHT - 2);
                    } else {
                        search.setPosition(x - 2 - width, y);
                    }
                } else {
                    search.setPosition(x - 2 - width, y);
                }
                search.setWidth(width);
            }
        }

        if (playerSortButton != null) {
            Slot corner = topRight(player);
            playerSortButton.setPosition(left + corner.x + 17 - BUTTON, top + corner.y - BUTTON - 2);
        }
    }

    private static Slot topRight(List<Slot> slots) {
        Slot best = slots.get(0);
        for (Slot slot : slots) {
            if (slot.y < best.y || slot.y == best.y && slot.x > best.x) {
                best = slot;
            }
        }
        return best;
    }

    public void drawOverlay(GuiGraphicsExtractor graphics) {
        if (search == null || query.isEmpty()) {
            return;
        }
        int left = access.lidless$leftPos();
        int top = access.lidless$topPos();
        graphics.nextStratum();
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            int x = left + slot.x;
            int y = top + slot.y;
            if (matches(slot)) {
                graphics.outline(x - 1, y - 1, 18, 18, MATCH);
            } else {
                graphics.fill(x, y, x + 16, y + 16, DIM);
            }
        }
    }

    private boolean matches(Slot slot) {
        ItemStack stack = slot.getItem();
        if (seen.get(slot) != stack) {
            seen.put(slot, stack);
            matched.put(slot, ItemTexts.matches(query, stack));
        }
        return matched.get(slot);
    }

    public boolean keyPressed(KeyEvent event) {
        if (search == null) {
            return false;
        }
        if (search.isFocused()) {
            if (event.isEscape() || event.isConfirmation()) {
                search.setFocused(false);
                screen.setFocused(null);
            } else {
                search.keyPressed(event);
            }
            return true;
        }
        if (event.key() == InputConstants.KEY_F && (event.hasControlDown() || Minecraft.getInstance().hasControlDown())) {
            screen.setFocused(search);
            search.setFocused(true);
            return true;
        }
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (search != null && search.isFocused() && !search.isMouseOver(event.x(), event.y())) {
            search.setFocused(false);
            screen.setFocused(null);
        }
        if (event.button() != InputConstants.MOUSE_BUTTON_MIDDLE || !LidlessClient.config().middleClickSort()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        Slot hovered = access.lidless$hoveredSlot();
        if (mc.player == null || mc.player.hasInfiniteMaterials() || hovered == null) {
            return false;
        }
        if (storage.contains(hovered)) {
            StorageActions.sort(mc, menu, storage, LidlessClient.config().sortOrder());
            return true;
        }
        if (player.contains(hovered)) {
            StorageActions.sort(mc, menu, player, LidlessClient.config().sortOrder());
            return true;
        }
        return false;
    }
}
