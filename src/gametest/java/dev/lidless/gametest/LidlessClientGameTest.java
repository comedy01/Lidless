package dev.lidless.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lidless.client.LidlessClient;
import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.config.LidlessConfig;
import dev.lidless.config.LidlessPolicy;
import dev.lidless.peek.PeekResolver;
import dev.lidless.peek.PeekTarget;
import dev.lidless.tooltip.ContainerPreview;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class LidlessClientGameTest implements FabricClientGameTest {
    private static final BlockPos FRONT = new BlockPos(0, -59, 3);

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(20);
            LidlessConfig config = LidlessClient.config();
            config.resetToDefaults();
            world.getServer().runCommand("gamemode survival @p");
            world.getServer().runCommand("tp @p 0 -60 0 0 0");
            context.waitTicks(10);

            checkChestPeek(context, world);
            checkDoubleChest(context, world);
            checkLootAndShulker(context, world);
            checkEnderChest(context, world);
            checkMounts(context, world);
            checkTooltips(context);
            checkChestScreen(context, world);
            checkInventoryTooltip(context, world);
            checkSettingsReset(context);
        }
        log("ALL CHECKS PASSED");
    }

    private static void checkChestPeek(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("setblock 0 -59 3 chest[facing=north]{Items:["
                + "{Slot:0b,id:\"minecraft:diamond\",count:5},"
                + "{Slot:4b,id:\"minecraft:cobblestone\",count:64},"
                + "{Slot:5b,id:\"minecraft:cobblestone\",count:20}]}");
        context.waitTicks(10);
        PeekTarget chest = peek(context);
        check(chest != null, "no peek while looking at a chest");
        log("chest: " + describe(chest));
        check(chest.status() == PeekTarget.Status.LIVE, "chest not read live: " + chest.status());
        check(chest.items().size() == 27, "wrong chest size: " + chest.items().size());
        check(chest.items().get(0).is(Items.DIAMOND) && chest.items().get(0).getCount() == 5, "diamonds missing");
        check(chest.title().getString().equals("Chest"), "wrong title: " + chest.title().getString());
        log("screenshot: " + context.takeScreenshot("lidless-peek-chest"));

        LidlessClient.config().setCompact(false);
        context.waitTicks(2);
        log("screenshot: " + context.takeScreenshot("lidless-peek-chest-slots"));
        LidlessClient.config().setCompact(true);

        LidlessClient.config().setRequireSneak(true);
        context.waitTicks(2);
        log("screenshot: " + context.takeScreenshot("lidless-peek-sneak-off"));
        LidlessClient.config().setRequireSneak(false);

        world.getServer().runCommand("setblock 0 -59 3 stone");
        context.waitTicks(5);
        check(peek(context) == null, "peek shown for a plain block");
    }

    private static void checkDoubleChest(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("setblock 0 -59 3 air");
        world.getServer().runCommand("setblock 0 -59 3 chest[facing=north,type=left]");
        world.getServer().runCommand("setblock 1 -59 3 chest[facing=north,type=right]{Items:[{Slot:3b,id:\"minecraft:gold_ingot\",count:9}]}");
        context.waitTicks(10);
        PeekTarget chest = peek(context);
        check(chest != null, "no peek at a double chest");
        log("double chest: " + describe(chest));
        check(chest.items().size() == 54, "double chest not combined: " + chest.items().size());
        check(chest.title().getString().equals("Large Chest"), "wrong double chest title: " + chest.title().getString());
        check(countOf(chest.items(), Items.GOLD_INGOT) == 9, "gold from the other half missing");
        world.getServer().runCommand("setblock 1 -59 3 air");
        world.getServer().runCommand("setblock 0 -59 3 air");
    }

    private static void checkLootAndShulker(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("setblock 0 -59 3 chest[facing=north]{LootTable:\"minecraft:chests/simple_dungeon\"}");
        context.waitTicks(10);
        PeekTarget loot = peek(context);
        check(loot != null && loot.status() == PeekTarget.Status.LOOT, "unopened loot chest not flagged: " + describe(loot));

        world.getServer().runCommand("setblock 0 -59 3 air");
        world.getServer().runCommand("setblock 0 -59 3 red_shulker_box{CustomName:\"Tools\",Items:[{Slot:13b,id:\"minecraft:iron_pickaxe\",count:1}]}");
        context.waitTicks(10);
        PeekTarget shulker = peek(context);
        log("shulker: " + describe(shulker));
        check(shulker != null && shulker.items().size() == 27, "shulker box not read: " + describe(shulker));
        check(shulker.title().getString().equals("Tools"), "custom name not used: " + shulker.title().getString());
        check(shulker.items().get(13).is(Items.IRON_PICKAXE), "pickaxe not in slot 13");
        world.getServer().runCommand("setblock 0 -59 3 air");
    }

    private static void checkEnderChest(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("item replace entity @p enderchest.0 with minecraft:emerald 7");
        world.getServer().runCommand("setblock 0 -59 3 ender_chest[facing=north]");
        context.waitTicks(10);
        PeekTarget ender = peek(context);
        log("ender chest: " + describe(ender));
        check(ender != null && countOf(ender.items(), Items.EMERALD) == 7, "ender chest contents missing");
        log("screenshot: " + context.takeScreenshot("lidless-peek-ender"));
        world.getServer().runCommand("setblock 0 -59 3 air");
    }

    private static void checkMounts(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("tp @p 0 -60 0 0 20");
        world.getServer().runCommand("summon donkey 0 -60 3 {NoAI:1b,Tame:1b,ChestedHorse:1b,Rotation:[90f,0f],"
                + "Items:[{Slot:0b,id:\"minecraft:apple\",count:3},{Slot:4b,id:\"minecraft:bread\",count:12}]}");
        context.waitTicks(15);
        PeekTarget donkey = peek(context);
        log("donkey: " + describe(donkey));
        check(donkey != null && donkey.status() == PeekTarget.Status.LIVE, "donkey with chest not peeked");
        check(countOf(donkey.items(), Items.BREAD) == 12, "donkey bread missing");
        log("screenshot: " + context.takeScreenshot("lidless-peek-donkey"));
        world.getServer().runCommand("kill @e[type=donkey]");
        context.waitTicks(25);
        world.getServer().runCommand("kill @e[type=item]");

        world.getServer().runCommand("tp @p 0 -60 0 0 30");
        world.getServer().runCommand("summon oak_chest_boat 0 -60 2 {Items:[{Slot:0b,id:\"minecraft:oak_log\",count:32}]}");
        context.waitTicks(15);
        PeekTarget boat = peek(context);
        log("chest boat: " + describe(boat));
        check(boat != null && countOf(boat.items(), Items.OAK_LOG) == 32, "chest boat not peeked");
        world.getServer().runCommand("kill @e[type=oak_chest_boat]");
        world.getServer().runCommand("kill @e[type=item]");
        world.getServer().runCommand("tp @p 0 -60 0 0 0");
        context.waitTicks(5);
    }

    private static void checkTooltips(ClientGameTestContext context) {
        AtomicReference<String> result = new AtomicReference<>("");
        context.runOnClient(client -> {
            List<ItemStack> contents = new ArrayList<>();
            contents.add(new ItemStack(Items.DIAMOND, 3));
            contents.add(ItemStack.EMPTY);
            contents.add(new ItemStack(Items.TORCH, 40));
            ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
            shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));

            Optional<TooltipComponent> image = shulker.getTooltipImage();
            check(image.isPresent() && image.get() instanceof ContainerPreview, "no shulker preview");
            ContainerPreview preview = (ContainerPreview) image.get();
            check(preview.items().size() == 27, "shulker preview not 27 slots: " + preview.items().size());
            check(preview.items().get(2).is(Items.TORCH), "torches not in slot 2");

            String lines = text(shulker.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL));
            check(!lines.contains("Torch x40") && !lines.contains("x40"), "vanilla content list still shown: " + lines);

            Optional<TooltipComponent> ender = new ItemStack(Items.ENDER_CHEST).getTooltipImage();
            check(ender.isPresent(), "no ender chest preview");

            LidlessClient.config().setTooltipPreview(false);
            check(shulker.getTooltipImage().isEmpty(), "preview shown although switched off");
            String plain = text(shulker.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL));
            check(plain.contains("x40") || plain.contains("40"), "vanilla list missing with preview off: " + plain);
            LidlessClient.config().setTooltipPreview(true);
            result.set(lines);
        });
        log("tooltip lines: " + result.get());
    }

    private static void checkChestScreen(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("setblock 0 -59 3 chest[facing=north]{Items:["
                + "{Slot:0b,id:\"minecraft:stick\",count:10},"
                + "{Slot:3b,id:\"minecraft:dirt\",count:30},"
                + "{Slot:7b,id:\"minecraft:cobblestone\",count:50},"
                + "{Slot:9b,id:\"minecraft:stick\",count:20},"
                + "{Slot:12b,id:\"minecraft:dirt\",count:40},"
                + "{Slot:20b,id:\"minecraft:diamond\",count:2}]}");
        world.getServer().runCommand("clear @p");
        world.getServer().runCommand("item replace entity @p inventory.0 with minecraft:cobblestone 16");
        world.getServer().runCommand("item replace entity @p inventory.1 with minecraft:torch 5");
        context.waitTicks(10);

        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(ContainerScreen.class);
        context.waitTicks(5);
        log("screenshot: " + context.takeScreenshot("lidless-chest-screen"));

        clickButton(context, "lidless.button.sort");
        context.waitTicks(10);
        List<String> sorted = serverContents(context, world);
        log("after sort: " + sorted);
        check(sorted.equals(expectedSort(context)), "unexpected sort result: " + sorted);
        log("screenshot: " + context.takeScreenshot("lidless-chest-sorted"));

        context.getInput().holdControl();
        context.getInput().pressKey(InputConstants.KEY_F);
        context.getInput().releaseControl();
        context.getInput().typeChars("stone");
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitTicks(3);
        check(context.computeOnClient(client -> Screens.current(client) instanceof ContainerScreen),
                "typing into the search box closed the chest");
        String typed = context.computeOnClient(client -> searchBox(Screens.current(client)).getValue());
        check(typed.equals("stone"), "search box did not receive text: " + typed);
        log("screenshot: " + context.takeScreenshot("lidless-chest-search"));
        context.getInput().pressKey(InputConstants.KEY_ESCAPE);
        context.waitTicks(2);
        check(context.computeOnClient(client -> Screens.current(client) instanceof ContainerScreen),
                "escape closed the chest instead of leaving the search box");

        clickButton(context, "lidless.button.deposit");
        context.waitTicks(10);
        List<String> deposited = serverContents(context, world);
        log("after deposit: " + deposited);
        check(countText(deposited, "cobblestone") == 66, "cobblestone was not deposited: " + deposited);
        check(countText(deposited, "torch") == 0, "unmatched torches were deposited: " + deposited);

        clickButton(context, "lidless.button.take");
        context.waitTicks(10);
        List<String> taken = serverContents(context, world);
        check(taken.isEmpty(), "take all left items behind: " + taken);

        context.setScreen(() -> null);
        context.waitTicks(5);
        world.getServer().runCommand("setblock 0 -59 3 air");
        world.getServer().runCommand("clear @p");
    }

    private static void checkInventoryTooltip(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("give @p minecraft:shulker_box[minecraft:container=["
                + "{slot:0,item:{id:\"minecraft:diamond\",count:3}},"
                + "{slot:4,item:{id:\"minecraft:golden_apple\",count:2}},"
                + "{slot:13,item:{id:\"minecraft:torch\",count:64}}]]");
        world.getServer().runCommand("give @p minecraft:stone 9");
        world.getServer().runCommand("give @p minecraft:dirt 3");
        context.waitTicks(5);
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(InventoryScreen.class);
        context.waitTicks(3);

        double[] cursor = context.computeOnClient(client -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Screens.current(client);
            Slot target = null;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.getItem().is(Items.SHULKER_BOX)) {
                    target = slot;
                }
            }
            check(target != null, "shulker box not in the inventory");
            double scale = client.getWindow().getGuiScale();
            int left = (screen.width - 176) / 2;
            int top = (screen.height - 166) / 2;
            return new double[] {(left + target.x + 8) * scale, (top + target.y + 8) * scale};
        });
        context.getInput().setCursorPos(cursor[0], cursor[1]);
        context.waitTicks(3);
        log("screenshot: " + context.takeScreenshot("lidless-shulker-tooltip"));
        check(context.computeOnClient(client -> hasButton(Screens.current(client), "lidless.button.sort_inventory")),
                "inventory sort button missing");
        context.setScreen(() -> null);
        context.waitTicks(3);
    }

    private static void checkSettingsReset(ClientGameTestContext context) {
        LidlessConfig config = LidlessClient.config();
        config.setYPosition(10);
        context.setScreen(() -> new LidlessSettingsScreen(null, Minecraft.getInstance().options));
        context.waitForScreen(LidlessSettingsScreen.class);
        context.waitTicks(5);
        log("screenshot: " + context.takeScreenshot("lidless-settings"));
        context.getInput().setCursorPos(200.0, 200.0);
        context.getInput().scroll(-20.0);
        context.waitTicks(2);
        clickSettingsButton(context, "lidless.options.reset");
        context.waitTicks(5);
        check(config.yPosition() == LidlessPolicy.DEFAULT_Y_POSITION, "reset did not restore the vertical position");
        log("screenshot: " + context.takeScreenshot("lidless-settings-after-reset"));

        int sliders = context.computeOnClient(client -> countSliders(Screens.current(client), "Vertical"));
        check(sliders == 1, "reset left " + sliders + " vertical sliders on the screen");
        dragSliderToMax(context, "Vertical");
        context.waitTicks(5);
        check(config.yPosition() == LidlessPolicy.MAX_POSITION, "dragging the vertical slider after reset did not change the config");
        String shown = context.computeOnClient(client ->
                findSlider(Screens.current(client), "Vertical").getMessage().getString());
        check(shown.contains("100%"), "the vertical slider did not move after reset: " + shown);

        context.setScreen(() -> null);
        context.waitTicks(5);
        config.resetToDefaults();
        LidlessClient.saveConfig();
    }

    private static void clickSettingsButton(ClientGameTestContext context, String translationKey) {
        double[] center = context.computeOnClient(client -> {
            Button button = findSettingsButton(Screens.current(client), translationKey);
            check(button != null, "no button '" + translationKey + "' on the current screen");
            double scale = client.getWindow().getGuiScale();
            return new double[] {
                    (button.getX() + button.getWidth() / 2.0) * scale,
                    (button.getY() + button.getHeight() / 2.0) * scale};
        });
        context.getInput().setCursorPos(center[0], center[1]);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    private static void dragSliderToMax(ClientGameTestContext context, String captionText) {
        context.getInput().setCursorPos(200.0, 200.0);
        context.getInput().scroll(-20.0);
        context.waitTicks(2);
        double[] bounds = context.computeOnClient(client -> {
            AbstractSliderButton slider = findSlider(Screens.current(client), captionText);
            check(slider != null, "no slider '" + captionText + "' on the current screen");
            double scale = client.getWindow().getGuiScale();
            return new double[] {
                    (slider.getX() + slider.getWidth() - 2.0) * scale,
                    (slider.getY() + slider.getHeight() / 2.0) * scale};
        });
        context.getInput().setCursorPos(bounds[0], bounds[1]);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
    }

    private static Button findSettingsButton(GuiEventListener node, String translationKey) {
        if (node instanceof Button button
                && button.getMessage().getContents() instanceof TranslatableContents contents
                && contents.getKey().equals(translationKey)) {
            return button;
        }
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                Button found = findSettingsButton(child, translationKey);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractSliderButton findSlider(GuiEventListener node, String captionText) {
        if (node instanceof AbstractSliderButton slider && slider.getMessage().getString().contains(captionText)) {
            return slider;
        }
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                AbstractSliderButton found = findSlider(child, captionText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static int countSliders(GuiEventListener node, String captionText) {
        int count = node instanceof AbstractSliderButton slider && slider.getMessage().getString().contains(captionText) ? 1 : 0;
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                count += countSliders(child, captionText);
            }
        }
        return count;
    }

    private static boolean hasButton(Screen screen, String key) {
        return findButton(screen, key) != null;
    }

    private static AbstractWidget findButton(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget
                    && widget.getMessage().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(key)) {
                return widget;
            }
        }
        return null;
    }

    private static void clickButton(ClientGameTestContext context, String key) {
        double[] cursor = context.computeOnClient(client -> {
            AbstractWidget button = findButton(Screens.current(client), key);
            check(button != null, "no button " + key);
            double scale = client.getWindow().getGuiScale();
            return new double[] {(button.getX() + button.getWidth() / 2.0) * scale, (button.getY() + button.getHeight() / 2.0) * scale};
        });
        context.getInput().setCursorPos(cursor[0], cursor[1]);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.getInput().setCursorPos(0, 0);
    }

    private static EditBox searchBox(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof EditBox box) {
                return box;
            }
        }
        throw new AssertionError("no search box on " + screen);
    }

    private static List<String> expectedSort(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
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
        });
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static List<String> serverContents(ClientGameTestContext context, TestSingleplayerContext world) {
        return world.getServer().computeOnServer(server -> {
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
        });
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

    private static PeekTarget peek(ClientGameTestContext context) {
        return context.computeOnClient(client -> PeekResolver.resolve(client));
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
        System.out.println("[lidless-gametest] " + message);
    }
}
