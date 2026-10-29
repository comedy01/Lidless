package dev.lidless.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lidless.client.LidlessClient;
import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.config.LidlessConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class LidlessShowcase implements FabricClientGameTest {
    private static final Path OUT = Path.of(System.getProperty("lidless.showcase.dir", "showcase/raw")).toAbsolutePath();
    private static final int Y = -60;

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!Boolean.getBoolean("lidless.showcase")) {
            return;
        }
        context.getInput().resizeWindow(1920, 1080);
        context.waitTicks(5);
        guiScale(context);

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            LidlessConfig config = LidlessClient.config();
            config.resetToDefaults();
            Consumer<String> cmd = world.getServer()::runCommand;
            cmd.accept("gamemode survival @p");
            cmd.accept("time set 5000");
            buildRoom(cmd);
            world.getConnection().waitForChunksRender();
            context.waitTicks(20);
            guiScale(context);

            peekShots(context, cmd, config);
            mountShots(context, cmd);
            screenShots(context, cmd);
            inventoryShots(context, cmd);
            settingsShot(context);
            config.resetToDefaults();
        }

        rememberedShots(context);
        LidlessClient.config().resetToDefaults();
        LidlessClient.saveConfig();
    }

    private static void buildRoom(Consumer<String> cmd) {
        cmd.accept("fill -36 -61 -4 26 -61 9 minecraft:spruce_planks");
        cmd.accept("fill -36 -60 8 26 -55 8 minecraft:stone_bricks");
        cmd.accept("fill -36 -60 -4 -36 -55 8 minecraft:stone_bricks");
        cmd.accept("fill 26 -60 -4 26 -55 8 minecraft:stone_bricks");
        cmd.accept("fill -35 -60 7 25 -59 7 minecraft:bookshelf");
        cmd.accept("fill -35 -58 7 25 -58 7 minecraft:spruce_slab[type=bottom]");
        for (int x = -35; x <= 25; x += 3) {
            cmd.accept("fill " + x + " -60 7 " + x + " -56 7 minecraft:stripped_spruce_log");
            cmd.accept("setblock " + x + " -60 6 minecraft:lantern");
        }
        cmd.accept("fill -34 -60 6 -33 -60 6 minecraft:barrel[facing=up]");
        cmd.accept("setblock -21 -60 6 minecraft:crafting_table");
        cmd.accept("setblock -8 -60 6 minecraft:cauldron");
        cmd.accept("setblock 4 -60 6 minecraft:potted_fern");
        cmd.accept("setblock 16 -60 6 minecraft:anvil");

        cmd.accept("setblock -30 -60 3 minecraft:barrel[facing=up]{CustomName:\"Ores\",Items:["
                + stack(0, "raw_iron", 64) + "," + stack(1, "raw_iron", 41) + "," + stack(2, "raw_gold", 23) + ","
                + stack(3, "raw_copper", 64) + "," + stack(4, "coal", 52) + "," + stack(5, "redstone", 38) + ","
                + stack(6, "lapis_lazuli", 27) + "," + stack(7, "diamond", 11) + "," + stack(8, "emerald", 6) + ","
                + stack(9, "ancient_debris", 2) + "," + stack(10, "quartz", 30) + "," + stack(11, "amethyst_shard", 14) + "]}");
        cmd.accept("setblock -24 -60 3 minecraft:furnace[facing=north,lit=true]{Items:["
                + stack(0, "raw_iron", 18) + "," + stack(1, "coal", 9) + "," + stack(2, "iron_ingot", 24) + "]}");
        cmd.accept("setblock -18 -60 3 minecraft:chest[facing=north,type=left]{Items:["
                + stack(0, "oak_log", 64) + "," + stack(1, "oak_log", 64) + "," + stack(2, "spruce_log", 64) + ","
                + stack(3, "birch_log", 48) + "," + stack(4, "oak_planks", 64) + "," + stack(5, "stick", 40) + ","
                + stack(9, "cobblestone", 64) + "," + stack(10, "cobblestone", 64) + "," + stack(11, "cobblestone", 64) + ","
                + stack(12, "stone", 64) + "," + stack(13, "deepslate", 45) + "," + stack(14, "granite", 20) + ","
                + stack(18, "glass", 32) + "," + stack(19, "white_wool", 16) + "," + stack(20, "bricks", 64) + "]}");
        cmd.accept("setblock -17 -60 3 minecraft:chest[facing=north,type=right]{Items:["
                + stack(0, "sand", 64) + "," + stack(1, "gravel", 27) + "," + stack(2, "clay_ball", 30) + ","
                + stack(9, "dirt", 64) + "," + stack(10, "dirt", 12) + "," + stack(11, "moss_block", 8) + ","
                + stack(18, "torch", 64) + "," + stack(19, "lantern", 6) + "," + stack(20, "chain", 12) + "]}");
        cmd.accept("setblock -11 -60 3 minecraft:chest[facing=north]{Items:["
                + stack(0, "diamond", 12) + "," + stack(1, "iron_ingot", 64) + "," + stack(2, "gold_ingot", 23) + ","
                + stack(4, "emerald", 9) + "," + stack(5, "redstone", 48) + "," + stack(7, "lapis_lazuli", 31) + ","
                + stack(9, "bread", 16) + "," + stack(10, "cooked_beef", 24) + "," + stack(11, "golden_carrot", 20) + ","
                + stack(13, "iron_ingot", 30) + "," + stack(15, "ender_pearl", 8) + "," + stack(17, "experience_bottle", 16) + ","
                + stack(19, "torch", 64) + "," + stack(21, "arrow", 64) + "," + stack(22, "arrow", 27) + ","
                + stack(24, "name_tag", 2) + "," + stack(26, "saddle", 1) + "]}");
        cmd.accept("setblock -5 -60 3 minecraft:purple_shulker_box[facing=up]{CustomName:\"Tools\",Items:["
                + stack(0, "diamond_pickaxe", 1) + "," + stack(1, "diamond_axe", 1) + "," + stack(2, "diamond_shovel", 1) + ","
                + stack(3, "iron_sword", 1) + "," + stack(4, "shears", 1) + "," + stack(5, "flint_and_steel", 1) + ","
                + stack(9, "bow", 1) + "," + stack(10, "fishing_rod", 1) + "," + stack(11, "spyglass", 1) + ","
                + stack(13, "shield", 1) + "," + stack(14, "bucket", 3) + "," + stack(15, "water_bucket", 1) + ","
                + stack(18, "compass", 1) + "," + stack(19, "clock", 1) + "]}");
        cmd.accept("setblock 1 -60 3 minecraft:ender_chest[facing=north]");
        cmd.accept("setblock 7 -60 3 minecraft:chest[facing=north]{LootTable:\"minecraft:chests/simple_dungeon\"}");
        cmd.accept("setblock 13 -60 3 minecraft:hopper{Items:[" + stack(0, "iron_nugget", 17) + "," + stack(2, "rotten_flesh", 5) + "]}");
        cmd.accept("setblock 19 -60 3 minecraft:brewing_stand{Items:["
                + stack(0, "potion", 1) + "," + stack(1, "potion", 1) + "," + stack(3, "nether_wart", 12) + "," + stack(4, "blaze_powder", 7) + "]}");

        cmd.accept("item replace entity @p enderchest.0 with minecraft:netherite_ingot 4");
        cmd.accept("item replace entity @p enderchest.1 with minecraft:elytra");
        cmd.accept("item replace entity @p enderchest.2 with minecraft:totem_of_undying 2");
        cmd.accept("item replace entity @p enderchest.3 with minecraft:enchanted_golden_apple 3");
        cmd.accept("item replace entity @p enderchest.4 with minecraft:shulker_shell 6");
        cmd.accept("item replace entity @p enderchest.9 with minecraft:diamond_block 5");
        cmd.accept("item replace entity @p enderchest.10 with minecraft:beacon");
        cmd.accept("item replace entity @p enderchest.11 with minecraft:end_crystal 4");
    }

    private static String stack(int slot, String id, int count) {
        return "{Slot:" + slot + "b,id:\"minecraft:" + id + "\",count:" + count + "}";
    }

    private static void peekShots(ClientGameTestContext context, Consumer<String> cmd, LidlessConfig config) {
        standBefore(context, cmd, -11);
        shoot(context, "peek-chest");

        config.setCompact(false);
        context.waitTicks(2);
        shoot(context, "peek-chest-slots");
        config.setRows(6);
        standAt(context, cmd, "@p", -17.0);
        shoot(context, "peek-double-chest-slots");
        config.setRows(3);
        config.setCompact(true);
        context.waitTicks(2);
        shoot(context, "peek-double-chest");

        standBefore(context, cmd, -30);
        shoot(context, "peek-barrel");
        standBefore(context, cmd, -24);
        shoot(context, "peek-furnace");
        config.setCompact(false);
        standBefore(context, cmd, -5);
        shoot(context, "peek-shulker");
        config.setCompact(true);
        standBefore(context, cmd, 1);
        shoot(context, "peek-ender-chest");
        standBefore(context, cmd, 7);
        shoot(context, "peek-loot");
        standBefore(context, cmd, 13);
        shoot(context, "peek-hopper");
        standBefore(context, cmd, 19);
        shoot(context, "peek-brewing-stand");

        config.setXPosition(50);
        config.setYPosition(20);
        standBefore(context, cmd, -11);
        shoot(context, "peek-position-top");
        config.setXPosition(100);
        config.setYPosition(50);

        config.setRequireSneak(true);
        standBefore(context, cmd, -11);
        shoot(context, "peek-sneak-off");
        context.getInput().holdShift();
        context.waitTicks(8);
        shoot(context, "peek-sneak-on");
        context.getInput().releaseShift();
        config.setRequireSneak(false);
        context.waitTicks(5);
    }

    private static void mountShots(ClientGameTestContext context, Consumer<String> cmd) {
        cmd.accept("tp @p 0.5 -60 -1.5 180 10");
        context.waitTicks(5);
        cmd.accept("summon donkey 0.5 -60 -4.5 {NoAI:1b,Tame:1b,ChestedHorse:1b,Rotation:[90f,0f],Items:["
                + stack(0, "apple", 12) + "," + stack(1, "bread", 20) + "," + stack(2, "hay_block", 9) + ","
                + stack(5, "oak_sapling", 16) + "," + stack(6, "wheat_seeds", 40) + "," + stack(10, "cooked_porkchop", 18) + "]}");
        context.waitTicks(20);
        context.getInput().lookAt(180f, 12f);
        context.waitTicks(5);
        shoot(context, "peek-donkey");
        cmd.accept("tp @e[type=donkey] ~ -200 ~");
        context.waitTicks(5);

        cmd.accept("summon oak_chest_boat 0.5 -60 -4 {Rotation:[90f,0f],Items:["
                + stack(0, "oak_log", 32) + "," + stack(1, "cod", 14) + "," + stack(2, "salmon", 9) + ","
                + stack(3, "fishing_rod", 1) + "," + stack(4, "lily_pad", 6) + "," + stack(9, "prismarine_shard", 11) + "]}");
        context.waitTicks(15);
        context.getInput().lookAt(180f, 30f);
        context.waitTicks(5);
        shoot(context, "peek-chest-boat");
        cmd.accept("kill @e[type=oak_chest_boat]");
        cmd.accept("kill @e[type=item]");
        context.waitTicks(5);
    }

    private static void screenShots(ClientGameTestContext context, Consumer<String> cmd) {
        cmd.accept("setblock -11 -60 3 minecraft:chest[facing=north]{Items:["
                + stack(0, "stick", 10) + "," + stack(2, "dirt", 30) + "," + stack(3, "iron_ingot", 5) + ","
                + stack(5, "cobblestone", 50) + "," + stack(7, "bread", 3) + "," + stack(8, "diamond", 2) + ","
                + stack(9, "stick", 20) + "," + stack(11, "oak_log", 17) + "," + stack(12, "dirt", 40) + ","
                + stack(14, "iron_ingot", 12) + "," + stack(16, "cobblestone", 30) + "," + stack(17, "torch", 16) + ","
                + stack(19, "bread", 6) + "," + stack(20, "oak_log", 9) + "," + stack(22, "gold_ingot", 7) + ","
                + stack(23, "raw_iron", 11) + "," + stack(25, "torch", 20) + "," + stack(26, "iron_pickaxe", 1) + "]}");
        cmd.accept("clear @p");
        cmd.accept("item replace entity @p hotbar.0 with minecraft:iron_sword");
        cmd.accept("item replace entity @p hotbar.1 with minecraft:iron_pickaxe");
        cmd.accept("item replace entity @p hotbar.2 with minecraft:torch 32");
        cmd.accept("item replace entity @p hotbar.8 with minecraft:bread 12");
        cmd.accept("item replace entity @p inventory.0 with minecraft:cobblestone 64");
        cmd.accept("item replace entity @p inventory.1 with minecraft:dirt 21");
        cmd.accept("item replace entity @p inventory.5 with minecraft:raw_iron 9");
        cmd.accept("item replace entity @p inventory.9 with minecraft:andesite 33");
        cmd.accept("item replace entity @p inventory.12 with minecraft:oak_log 8");
        cmd.accept("item replace entity @p inventory.20 with minecraft:rotten_flesh 7");
        cmd.accept("item replace entity @p inventory.24 with minecraft:string 4");
        standBefore(context, cmd, -11);

        context.getInput().pressKey(options -> options.keyUse);
        context.waitForScreen(ContainerScreen.class);
        context.waitTicks(5);
        shoot(context, "screen-chest");

        clickButton(context, "lidless.button.sort");
        context.waitTicks(10);
        shoot(context, "screen-chest-sorted");

        context.getInput().holdControl();
        context.getInput().pressKey(InputConstants.KEY_F);
        context.getInput().releaseControl();
        context.getInput().typeChars("iron");
        context.waitTicks(3);
        shoot(context, "screen-chest-search");
        context.getInput().pressKey(InputConstants.KEY_ESCAPE);
        context.waitTicks(2);

        hoverButton(context, "lidless.button.deposit");
        context.waitTicks(5);
        shoot(context, "screen-chest-deposit-hover");
        clickButton(context, "lidless.button.deposit");
        context.waitTicks(10);
        shoot(context, "screen-chest-deposited");

        context.setScreen(() -> null);
        context.waitTicks(5);
    }

    private static void inventoryShots(ClientGameTestContext context, Consumer<String> cmd) {
        cmd.accept("clear @p");
        cmd.accept("give @p minecraft:purple_shulker_box[minecraft:custom_name=\"Tools\",minecraft:container=["
                + slot(0, "diamond_pickaxe", 1) + "," + slot(1, "diamond_axe", 1) + "," + slot(2, "diamond_shovel", 1) + ","
                + slot(3, "iron_sword", 1) + "," + slot(4, "shears", 1) + "," + slot(9, "bow", 1) + ","
                + slot(10, "arrow", 64) + "," + slot(11, "spyglass", 1) + "," + slot(13, "shield", 1) + ","
                + slot(18, "water_bucket", 1) + "," + slot(19, "torch", 64) + "," + slot(20, "golden_carrot", 32) + "]]");
        cmd.accept("give @p minecraft:lime_shulker_box[minecraft:custom_name=\"Farm\",minecraft:container=["
                + slot(0, "wheat_seeds", 64) + "," + slot(1, "carrot", 48) + "," + slot(2, "potato", 40) + ","
                + slot(3, "beetroot_seeds", 22) + "," + slot(4, "bone_meal", 64) + "," + slot(5, "sugar_cane", 30) + ","
                + slot(9, "wheat", 64) + "," + slot(10, "pumpkin", 12) + "," + slot(11, "melon_slice", 36) + ","
                + slot(12, "egg", 16) + "," + slot(13, "bamboo", 50) + "]]");
        cmd.accept("give @p minecraft:shulker_box[minecraft:container=["
                + slot(0, "redstone", 64) + "," + slot(1, "repeater", 12) + "," + slot(2, "comparator", 6) + ","
                + slot(3, "piston", 16) + "," + slot(4, "sticky_piston", 8) + "," + slot(5, "observer", 10) + "]]");
        cmd.accept("give @p minecraft:ender_chest");
        cmd.accept("give @p minecraft:diamond_pickaxe");
        cmd.accept("give @p minecraft:cobblestone 40");
        cmd.accept("give @p minecraft:oak_log 12");
        cmd.accept("give @p minecraft:bread 9");
        context.waitTicks(5);
        context.getInput().pressKey(options -> options.keyInventory);
        context.waitForScreen(InventoryScreen.class);
        context.waitTicks(5);

        hoverSlot(context, Items.DYED_SHULKER_BOX.pick(DyeColor.PURPLE));
        shoot(context, "tooltip-shulker-tools");
        hoverSlot(context, Items.DYED_SHULKER_BOX.pick(DyeColor.LIME));
        shoot(context, "tooltip-shulker-farm");
        hoverSlot(context, Items.SHULKER_BOX);
        shoot(context, "tooltip-shulker-redstone");
        hoverSlot(context, Items.ENDER_CHEST);
        shoot(context, "tooltip-ender-chest");
        hoverButton(context, "lidless.button.sort_inventory");
        context.waitTicks(3);
        shoot(context, "inventory-sort-hover");

        context.setScreen(() -> null);
        context.waitTicks(3);
    }

    private static String slot(int slot, String id, int count) {
        return "{slot:" + slot + ",item:{id:\"minecraft:" + id + "\",count:" + count + "}}";
    }

    private static void settingsShot(ClientGameTestContext context) {
        context.setScreen(() -> new LidlessSettingsScreen(null, Minecraft.getInstance().options));
        context.waitForScreen(LidlessSettingsScreen.class);
        context.waitTicks(5);
        context.getInput().setCursorPos(0, 0);
        shoot(context, "settings");
        context.getInput().setCursorPos(960, 540);
        context.getInput().scroll(-20.0);
        context.waitTicks(3);
        context.getInput().setCursorPos(0, 0);
        shoot(context, "settings-scrolled");
        context.setScreen(() -> null);
        context.waitTicks(3);
    }

    private static void rememberedShots(ClientGameTestContext context) {
        if (!eulaAccepted()) {
            System.out.println("[lidless-showcase] skipping server shots: eula.txt in " + Path.of("").toAbsolutePath() + " is not accepted");
            return;
        }
        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            Consumer<String> cmd = server::runCommand;
            try (TestDedicatedServerConnection connection = server.connect()) {
                connection.waitForChunksRender();
                cmd.accept("gamemode survival @a");
                cmd.accept("time set 5000");
                buildRoom(cmd);
                context.waitTicks(40);

                standBefore(context, cmd, "@a", -11);
                shoot(context, "server-peek-unknown");

                context.getInput().pressKey(options -> options.keyUse);
                context.waitForScreen(ContainerScreen.class);
                context.waitTicks(10);
                context.setScreen(() -> null);
                context.waitTicks(100);
                shoot(context, "server-peek-remembered");

                standAt(context, cmd, "@a", -17.0);
                context.getInput().pressKey(options -> options.keyUse);
                context.waitForScreen(ContainerScreen.class);
                context.waitTicks(10);
                context.setScreen(() -> null);
                context.waitTicks(40);
                shoot(context, "server-peek-remembered-double");
            }
        }
    }

    private static void guiScale(ClientGameTestContext context) {
        context.runOnClient(client -> {
            client.options.guiScale().set(4);
            client.resizeGui();
        });
        context.waitTicks(2);
        System.out.println("[lidless-showcase] gui scale " + context.computeOnClient(client -> client.getWindow().getGuiScale()));
    }

    private static boolean eulaAccepted() {
        try {
            return Files.readString(Path.of("eula.txt")).contains("eula=true");
        } catch (IOException e) {
            return false;
        }
    }

    private static void standBefore(ClientGameTestContext context, Consumer<String> cmd, int x) {
        standBefore(context, cmd, "@p", x);
    }

    private static void standBefore(ClientGameTestContext context, Consumer<String> cmd, String who, int x) {
        standAt(context, cmd, who, x + 0.5);
    }

    private static void standAt(ClientGameTestContext context, Consumer<String> cmd, String who, double x) {
        cmd.accept("tp " + who + " " + x + " " + Y + " 0.2 0 0");
        context.waitTicks(4);
        context.getInput().lookAt(0f, 18.7f);
        context.waitTicks(4);
    }

    private static void hoverSlot(ClientGameTestContext context, Item item) {
        double[] cursor = context.computeOnClient(client -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Screens.current(client);
            Slot target = null;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.getItem().is(item)) {
                    target = slot;
                }
            }
            if (target == null) {
                throw new AssertionError("no " + item + " in the inventory");
            }
            double scale = client.getWindow().getGuiScale();
            int left = (screen.width - 176) / 2;
            int top = (screen.height - 166) / 2;
            return new double[] {(left + target.x + 8) * scale, (top + target.y + 8) * scale};
        });
        context.getInput().setCursorPos(cursor[0], cursor[1]);
        context.waitTicks(3);
    }

    private static double[] buttonCenter(ClientGameTestContext context, String key) {
        return context.computeOnClient(client -> {
            for (GuiEventListener child : Screens.current(client).children()) {
                if (child instanceof AbstractWidget widget
                        && widget.getMessage().getContents() instanceof TranslatableContents contents
                        && contents.getKey().equals(key)) {
                    double scale = client.getWindow().getGuiScale();
                    return new double[] {(widget.getX() + widget.getWidth() / 2.0) * scale, (widget.getY() + widget.getHeight() / 2.0) * scale};
                }
            }
            throw new AssertionError("no button " + key);
        });
    }

    private static void hoverButton(ClientGameTestContext context, String key) {
        double[] center = buttonCenter(context, key);
        context.getInput().setCursorPos(center[0], center[1]);
    }

    private static void clickButton(ClientGameTestContext context, String key) {
        hoverButton(context, key);
        context.waitTick();
        context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.getInput().setCursorPos(0, 0);
    }

    private static void shoot(ClientGameTestContext context, String name) {
        context.runOnClient(client -> {
            client.gui.toastManager().clear();
            client.gui.hud.getChat().clearMessages(false);
        });
        context.waitTicks(2);
        Path file = context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix().withDestinationDir(OUT));
        System.out.println("[lidless-showcase] " + file);
    }
}
