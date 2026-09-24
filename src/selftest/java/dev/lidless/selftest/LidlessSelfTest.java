package dev.lidless.selftest;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lidless.client.LidlessClient;
import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.config.LidlessConfig;
import dev.lidless.config.LidlessPolicy;
import dev.lidless.peek.PeekResolver;
import dev.lidless.peek.PeekTarget;
import dev.lidless.tooltip.ContainerPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public final class LidlessSelfTest {
    private static final BlockPos FRONT = new BlockPos(0, -59, 3);
    private static final int TIMEOUT = 2400;

    private final List<Step> steps = new ArrayList<>();
    private boolean started;
    private boolean finished;
    private int idle;
    private int index;
    private int delay;
    private int waited;

    private record Step(int delay, BooleanSupplier ready, Runnable action) {
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (finished) {
            return;
        }
        if (!started) {
            if (mc.level == null && Screens.overlay(mc) == null && Screens.current(mc) != null && ++idle > 40) {
                started = true;
                mc.options.pauseOnLostFocus = false;
                plan(mc);
                delay = steps.get(0).delay();
                log("creating world");
                deleteWorld(mc, "lidless-selftest");
                Worlds.createFlat(mc, "lidless-selftest");
            }
            return;
        }
        if (delay > 0) {
            delay--;
            return;
        }
        Step step = steps.get(index);
        if (!step.ready().getAsBoolean()) {
            if (++waited > TIMEOUT) {
                finish(mc, new AssertionError("timed out at step " + index));
            }
            return;
        }
        waited = 0;
        index++;
        try {
            step.action().run();
        } catch (Throwable e) {
            finish(mc, e);
            return;
        }
        if (index >= steps.size()) {
            finish(mc, null);
        } else {
            delay = steps.get(index).delay();
        }
    }

    private void finish(Minecraft mc, Throwable failure) {
        finished = true;
        if (failure == null) {
            log("ALL CHECKS PASSED");
        } else {
            log("FAILED: " + failure);
            failure.printStackTrace();
        }
        mc.stop();
    }

    private static void deleteWorld(Minecraft mc, String name) {
        Path dir = mc.gameDirectory.toPath().resolve("saves").resolve(name);
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void then(int ticks, Runnable action) {
        steps.add(new Step(ticks, () -> true, action));
    }

    private void when(BooleanSupplier ready, Runnable action) {
        steps.add(new Step(0, ready, action));
    }

    private void plan(Minecraft mc) {
        when(() -> mc.level != null && mc.player != null && Screens.current(mc) == null
                && mc.getSingleplayerServer() != null, () -> log("world loaded"));
        then(40, () -> {
            LidlessClient.config().resetToDefaults();
            run(mc, "gamemode survival @p");
            run(mc, "tp @p 0 -60 0 0 0");
        });
        planChestPeek(mc);
        planDoubleChest(mc);
        planLootAndShulker(mc);
        planEnderChest(mc);
        planMounts(mc);
        then(5, () -> checkTooltips(mc));
        planChestScreen(mc);
        planInventoryTooltip(mc);
        planSettingsReset(mc);
    }

    private void planChestPeek(Minecraft mc) {
        then(0, () -> run(mc, "setblock 0 -59 3 chest[facing=north]{Items:["
                + Fixtures.item(0, "diamond", 5) + ","
                + Fixtures.item(4, "cobblestone", 64) + ","
                + Fixtures.item(5, "cobblestone", 20) + "]}"));
        then(20, () -> {
            PeekTarget chest = PeekResolver.resolve(mc);
            check(chest != null, "no peek while looking at a chest");
            log("chest: " + describe(chest));
            check(chest.status() == PeekTarget.Status.LIVE, "chest not read live: " + chest.status());
            check(chest.items().size() == 27, "wrong chest size: " + chest.items().size());
            check(chest.items().get(0).is(Items.DIAMOND) && chest.items().get(0).getCount() == 5, "diamonds missing");
            check(chest.title().getString().equals("Chest"), "wrong title: " + chest.title().getString());
            screenshot(mc, "peek-chest");
            LidlessClient.config().setCompact(false);
        });
        then(3, () -> {
            screenshot(mc, "peek-chest-slots");
            LidlessClient.config().setCompact(true);
            run(mc, "setblock 0 -59 3 stone");
        });
        then(10, () -> check(PeekResolver.resolve(mc) == null, "peek shown for a plain block"));
    }

    private void planDoubleChest(Minecraft mc) {
        then(0, () -> {
            run(mc, "setblock 0 -59 3 air");
            run(mc, "setblock 0 -59 3 chest[facing=north,type=left]");
            run(mc, "setblock 1 -59 3 chest[facing=north,type=right]{Items:[" + Fixtures.item(3, "gold_ingot", 9) + "]}");
        });
        then(20, () -> {
            PeekTarget chest = PeekResolver.resolve(mc);
            check(chest != null, "no peek at a double chest");
            log("double chest: " + describe(chest));
            check(chest.items().size() == 54, "double chest not combined: " + chest.items().size());
            check(chest.title().getString().equals("Large Chest"), "wrong double chest title: " + chest.title().getString());
            check(countOf(chest.items(), Items.GOLD_INGOT) == 9, "gold from the other half missing");
            run(mc, "setblock 1 -59 3 air");
            run(mc, "setblock 0 -59 3 air");
        });
    }

    private void planLootAndShulker(Minecraft mc) {
        then(5, () -> run(mc, "setblock 0 -59 3 chest[facing=north]{LootTable:\"minecraft:chests/simple_dungeon\"}"));
        then(20, () -> {
            PeekTarget loot = PeekResolver.resolve(mc);
            check(loot != null && loot.status() == PeekTarget.Status.LOOT, "unopened loot chest not flagged: " + describe(loot));
            run(mc, "setblock 0 -59 3 air");
            run(mc, "setblock 0 -59 3 red_shulker_box{CustomName:" + Fixtures.name("Tools")
                    + ",Items:[" + Fixtures.item(13, "iron_pickaxe", 1) + "]}");
        });
        then(20, () -> {
            PeekTarget shulker = PeekResolver.resolve(mc);
            log("shulker: " + describe(shulker));
            check(shulker != null && shulker.items().size() == 27, "shulker box not read: " + describe(shulker));
            check(shulker.title().getString().equals("Tools"), "custom name not used: " + shulker.title().getString());
            check(shulker.items().get(13).is(Items.IRON_PICKAXE), "pickaxe not in slot 13");
            run(mc, "setblock 0 -59 3 air");
        });
    }

    private void planEnderChest(Minecraft mc) {
        then(5, () -> {
            run(mc, "item replace entity @p enderchest.0 with minecraft:emerald 7");
            run(mc, "setblock 0 -59 3 ender_chest[facing=north]");
        });
        then(20, () -> {
            PeekTarget ender = PeekResolver.resolve(mc);
            log("ender chest: " + describe(ender));
            check(ender != null && countOf(ender.items(), Items.EMERALD) == 7, "ender chest contents missing");
            screenshot(mc, "peek-ender");
            run(mc, "setblock 0 -59 3 air");
        });
    }

    private void planMounts(Minecraft mc) {
        then(5, () -> {
            run(mc, "tp @p 0 -60 0 0 20");
            run(mc, "summon donkey 0 -60 3 {NoAI:1b,Tame:1b,ChestedHorse:1b,Rotation:[90f,0f]}");
        });
        then(5, () -> {
            run(mc, "item replace entity @e[type=donkey] horse.0 with minecraft:apple 3");
            run(mc, "item replace entity @e[type=donkey] horse.4 with minecraft:bread 12");
            run(mc, "item replace entity @e[type=donkey] horse.saddle with minecraft:saddle");
            run(mc, "item replace entity @e[type=donkey] saddle with minecraft:saddle");
        });
        then(30, () -> {
            PeekTarget donkey = PeekResolver.resolve(mc);
            log("donkey: " + describe(donkey));
            check(donkey != null && donkey.status() == PeekTarget.Status.LIVE, "donkey with chest not peeked");
            check(countOf(donkey.items(), Items.BREAD) == 12, "donkey bread missing");
            check(donkey.items().size() == 15, "donkey chest is not 15 slots: " + donkey.items().size());
            screenshot(mc, "peek-donkey");
        });
        then(0, () -> openInventory(mc, entityAhead(mc).getId()));
        when(() -> Screens.current(mc) instanceof AbstractContainerScreen<?>, () -> log("donkey inventory open"));
        then(5, () -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Screens.current(mc);
            screenshot(mc, "donkey-screen");
            check(hasButton(screen, "lidless.button.take"), "no take all button on the donkey screen");
            clickButton(mc, "lidless.button.take");
        });
        then(10, () -> {
            PeekTarget donkey = PeekResolver.resolve(mc);
            log("donkey after take all: " + describe(donkey));
            check(carried(mc, Items.BREAD) == 12, "take all did not move the bread");
            check(carried(mc, Items.SADDLE) == 0, "take all took the saddle");
            Screens.open(mc, null);
            run(mc, "clear @p");
            run(mc, "kill @e[type=donkey]");
        });
        then(25, () -> {
            run(mc, "kill @e[type=item]");
            run(mc, "tp @p 0 -60 0 0 30");
            run(mc, "summon " + chestBoat() + " 0 -60 2 {Items:[" + Fixtures.item(0, "oak_log", 32) + "]}");
        });
        then(30, () -> {
            PeekTarget boat = PeekResolver.resolve(mc);
            log("chest boat: " + describe(boat));
            check(boat != null && countOf(boat.items(), Items.OAK_LOG) == 32, "chest boat not peeked");
            run(mc, "kill @e[type=" + chestBoat() + "]");
            run(mc, "kill @e[type=item]");
            run(mc, "tp @p 0 -60 0 0 0");
        });
    }

    private static String chestBoat() {
        boolean split = BuiltInRegistries.ENTITY_TYPE.keySet().stream().anyMatch(id -> id.getPath().equals("oak_chest_boat"));
        return split ? "oak_chest_boat" : "chest_boat";
    }

    private static Entity entityAhead(Minecraft mc) {
        check(mc.hitResult instanceof EntityHitResult, "not looking at an entity");
        return ((EntityHitResult) mc.hitResult).getEntity();
    }

    private static void openInventory(Minecraft mc, int id) {
        IntegratedServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            Entity entity = server.getLevel(Level.OVERWORLD).getEntity(id);
            ((HasCustomInventoryScreen) entity).openCustomInventoryScreen(player);
        });
    }

    private static int carried(Minecraft mc, Item item) {
        int total = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void checkTooltips(Minecraft mc) {
        List<ItemStack> contents = new ArrayList<>();
        contents.add(new ItemStack(Items.DIAMOND, 3));
        contents.add(ItemStack.EMPTY);
        contents.add(new ItemStack(Items.TORCH, 40));
        ItemStack shulker = Fixtures.shulker(contents);

        Optional<TooltipComponent> image = shulker.getTooltipImage();
        check(image.isPresent() && image.get() instanceof ContainerPreview, "no shulker preview");
        ContainerPreview preview = (ContainerPreview) image.get();
        check(preview.items().size() == 27, "shulker preview not 27 slots: " + preview.items().size());
        check(preview.items().get(2).is(Items.TORCH), "torches not in slot 2");

        String lines = text(Fixtures.tooltip(mc, shulker));
        check(!lines.contains("x40"), "vanilla content list still shown: " + lines);

        check(new ItemStack(Items.ENDER_CHEST).getTooltipImage().isPresent(), "no ender chest preview");

        LidlessClient.config().setTooltipPreview(false);
        check(shulker.getTooltipImage().isEmpty(), "preview shown although switched off");
        String plain = text(Fixtures.tooltip(mc, shulker));
        check(plain.contains("40"), "vanilla list missing with preview off: " + plain);
        LidlessClient.config().setTooltipPreview(true);
        log("tooltip lines: " + lines + " / off: " + plain);
    }

    private void planChestScreen(Minecraft mc) {
        then(0, () -> {
            run(mc, "setblock 0 -59 3 chest[facing=north]{Items:["
                    + Fixtures.item(0, "stick", 10) + ","
                    + Fixtures.item(3, "dirt", 30) + ","
                    + Fixtures.item(7, "cobblestone", 50) + ","
                    + Fixtures.item(9, "stick", 20) + ","
                    + Fixtures.item(12, "dirt", 40) + ","
                    + Fixtures.item(20, "diamond", 2) + "]}");
            run(mc, "clear @p");
            run(mc, "item replace entity @p inventory.0 with minecraft:cobblestone 16");
            run(mc, "item replace entity @p inventory.1 with minecraft:torch 5");
        });
        then(15, () -> {
            check(mc.hitResult instanceof BlockHitResult, "not looking at the chest");
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, (BlockHitResult) mc.hitResult);
        });
        when(() -> Screens.current(mc) instanceof ContainerScreen, () -> log("chest open"));
        then(5, () -> {
            screenshot(mc, "chest-screen");
            clickButton(mc, "lidless.button.sort");
        });
        then(10, () -> {
            List<String> sorted = serverContents(mc);
            log("after sort: " + sorted);
            check(sorted.equals(expectedSort()), "unexpected sort result: " + sorted);
            Screen screen = Screens.current(mc);
            Ui.key(screen, InputConstants.KEY_F, InputConstants.MOD_CONTROL);
            check(searchBox(screen).isFocused(), "ctrl+f did not focus the search box");
            for (char c : "stone".toCharArray()) {
                searchBox(screen).insertText(String.valueOf(c));
            }
            Ui.key(screen, InputConstants.KEY_E, 0);
        });
        then(3, () -> {
            Screen screen = Screens.current(mc);
            check(screen instanceof ContainerScreen, "typing into the search box closed the chest");
            String typed = searchBox(screen).getValue();
            check(typed.equals("stone"), "search box did not receive text: " + typed);
            screenshot(mc, "chest-search");
            Ui.key(screen, InputConstants.KEY_ESCAPE, 0);
        });
        then(2, () -> {
            check(Screens.current(mc) instanceof ContainerScreen, "escape closed the chest instead of leaving the search box");
            clickButton(mc, "lidless.button.deposit");
        });
        then(10, () -> {
            List<String> deposited = serverContents(mc);
            log("after deposit: " + deposited);
            check(countText(deposited, "cobblestone") == 66, "cobblestone was not deposited: " + deposited);
            check(countText(deposited, "torch") == 0, "unmatched torches were deposited: " + deposited);
            clickButton(mc, "lidless.button.take");
        });
        then(10, () -> {
            List<String> taken = serverContents(mc);
            check(taken.isEmpty(), "take all left items behind: " + taken);
            Screens.open(mc, null);
        });
        then(5, () -> {
            run(mc, "setblock 0 -59 3 air");
            run(mc, "clear @p");
        });
    }

    private void planInventoryTooltip(Minecraft mc) {
        then(5, () -> {
            run(mc, "give @p " + Fixtures.shulkerBox(
                    Fixtures.boxItem(0, "diamond", 3),
                    Fixtures.boxItem(4, "golden_apple", 2),
                    Fixtures.boxItem(13, "torch", 64)));
            run(mc, "give @p minecraft:stone 9");
            run(mc, "give @p minecraft:dirt 3");
        });
        then(10, () -> Screens.open(mc, new InventoryScreen(mc.player)));
        then(5, () -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Screens.current(mc);
            Slot target = null;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.getItem().is(Items.SHULKER_BOX)) {
                    target = slot;
                }
            }
            check(target != null, "shulker box not in the inventory");
            check(hasButton(screen, "lidless.button.sort_inventory"), "inventory sort button missing");
            double scale = mc.getWindow().getGuiScale();
            int left = (screen.width - 176) / 2;
            int top = (screen.height - 166) / 2;
            Ui.moveMouse(mc, (left + target.x + 8) * scale, (top + target.y + 8) * scale);
        });
        then(5, () -> screenshot(mc, "shulker-tooltip"));
        then(1, () -> Screens.open(mc, null));
    }

    private void planSettingsReset(Minecraft mc) {
        then(5, () -> {
            LidlessClient.config().setYPosition(10);
            Screens.open(mc, new LidlessSettingsScreen(null, mc.options));
        });
        then(5, () -> {
            screenshot(mc, "settings");
            Settings.scroll(Screens.current(mc));
        });
        then(2, () -> {
            Screen screen = Screens.current(mc);
            Button reset = findButton(screen, "lidless.options.reset");
            check(reset != null, "no reset button");
            Ui.click(screen, reset.getX() + reset.getWidth() / 2.0, reset.getY() + reset.getHeight() / 2.0);
        });
        then(5, () -> {
            LidlessConfig config = LidlessClient.config();
            check(config.yPosition() == LidlessPolicy.DEFAULT_Y_POSITION, "reset did not restore the vertical position");
            Screen screen = Screens.current(mc);
            int sliders = countSliders(screen, "Vertical");
            check(sliders == 1, "reset left " + sliders + " vertical sliders on the screen");
            Settings.scroll(screen);
        });
        then(2, () -> {
            Screen screen = Screens.current(mc);
            AbstractSliderButton slider = findSlider(screen, "Vertical");
            check(slider != null, "no vertical slider");
            Ui.click(screen, slider.getX() + slider.getWidth() - 2.0, slider.getY() + slider.getHeight() / 2.0);
        });
        then(5, () -> {
            LidlessConfig config = LidlessClient.config();
            check(config.yPosition() == LidlessPolicy.MAX_POSITION, "dragging the vertical slider after reset did not change the config");
            String shown = findSlider(Screens.current(mc), "Vertical").getMessage().getString();
            check(shown.contains("100%"), "the vertical slider did not move after reset: " + shown);
            screenshot(mc, "settings-after-reset");
            Screens.open(mc, null);
            config.resetToDefaults();
            LidlessClient.saveConfig();
        });
    }

    private static void run(Minecraft mc, String command) {
        IntegratedServer server = mc.getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
    }

    private static void screenshot(Minecraft mc, String name) {
        log("screenshot: " + name);
        Screenshot.grab(mc.gameDirectory, Screens.renderTarget(mc), message -> {
        });
    }

    private static void clickButton(Minecraft mc, String key) {
        Screen screen = Screens.current(mc);
        AbstractWidget button = findWidget(screen, key);
        check(button != null, "no button " + key);
        Ui.click(screen, button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0);
    }

    private static boolean hasButton(Screen screen, String key) {
        return findWidget(screen, key) != null;
    }

    private static AbstractWidget findWidget(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget
                    && widget.getMessage().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(key)) {
                return widget;
            }
        }
        return null;
    }

    private static Button findButton(GuiEventListener node, String key) {
        if (node instanceof Button button
                && button.getMessage().getContents() instanceof TranslatableContents contents
                && contents.getKey().equals(key)) {
            return button;
        }
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                Button found = findButton(child, key);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractSliderButton findSlider(GuiEventListener node, String caption) {
        if (node instanceof AbstractSliderButton slider && slider.getMessage().getString().contains(caption)) {
            return slider;
        }
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                AbstractSliderButton found = findSlider(child, caption);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static int countSliders(GuiEventListener node, String caption) {
        int count = node instanceof AbstractSliderButton slider && slider.getMessage().getString().contains(caption) ? 1 : 0;
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                count += countSliders(child, caption);
            }
        }
        return count;
    }

    private static EditBox searchBox(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof EditBox box) {
                return box;
            }
        }
        throw new AssertionError("no search box on " + screen);
    }

    private static List<String> expectedSort() {
        List<Item> order = new ArrayList<>(List.of(Items.STICK, Items.DIRT, Items.COBBLESTONE, Items.DIAMOND));
        order.sort((a, b) -> Integer.compare(BuiltInRegistries.ITEM.getId(a), BuiltInRegistries.ITEM.getId(b)));
        List<String> out = new ArrayList<>();
        for (Item item : order) {
            int total = item == Items.STICK ? 30 : item == Items.DIRT ? 70 : item == Items.COBBLESTONE ? 50 : 2;
            for (int left = total; left > 0; left -= 64) {
                out.add(Math.min(64, left) + " " + name(item));
            }
        }
        return out;
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static List<String> serverContents(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        return server.submit(() -> {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            Container chest = (Container) level.getBlockEntity(FRONT);
            List<String> out = new ArrayList<>();
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (!stack.isEmpty()) {
                    out.add(stack.getCount() + " " + name(stack.getItem()));
                }
            }
            return out;
        }).join();
    }

    private static int countText(List<String> stacks, String item) {
        int total = 0;
        for (String stack : stacks) {
            String[] parts = stack.split(" ", 2);
            if (parts[1].equals(item)) {
                total += Integer.parseInt(parts[0]);
            }
        }
        return total;
    }

    private static int countOf(List<ItemStack> items, Item item) {
        int total = 0;
        for (ItemStack stack : items) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String describe(PeekTarget target) {
        if (target == null) {
            return "nothing";
        }
        int used = 0;
        for (ItemStack stack : target.items()) {
            if (!stack.isEmpty()) {
                used++;
            }
        }
        return target.title().getString() + " / " + target.status() + " / " + used + " of " + target.items().size();
    }

    private static String text(List<Component> lines) {
        StringBuilder out = new StringBuilder();
        for (Component line : lines) {
            if (out.length() > 0) {
                out.append(" | ");
            }
            out.append(line.getString());
        }
        return out.toString();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void log(String message) {
        System.out.println("[lidless-selftest] " + message);
    }
}
