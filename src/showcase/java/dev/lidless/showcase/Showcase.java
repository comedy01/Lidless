package dev.lidless.showcase;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.lidless.client.LidlessClient;
import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.config.LidlessConfig;
import dev.lidless.storage.ContainerScreenAccess;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class Showcase {
    private static final String WORLD = "lidless-showcase";
    private static final long SETTLE_NANOS = 3_000_000_000L;
    private static final int ROW = 4;
    private static final int STASH = -4;
    private static final int GLIDE = 24;
    private static final String[] ARROW = {
            "B",
            "BB",
            "BWB",
            "BWWB",
            "BWWWB",
            "BWWWWB",
            "BWWWWWB",
            "BWWWWWWB",
            "BWWWWWWWB",
            "BWWWWWWWWB",
            "BWWWWWWWWWB",
            "BWWWWWWBBBBB",
            "BWWWBWWB",
            "BWWB BWWB",
            "BWB  BWWB",
            "BB    BWWB",
            "B     BWWB",
            "       BWWB",
            "        BB"};
    private static Showcase instance;

    private final Path out;
    private final Recorder recorder;
    private final long seed;
    private final List<Action> actions = new ArrayList<>();
    private final AtomicReference<BlockPos> site = new AtomicReference<>();
    private boolean started;
    private boolean finished;
    private int idle;
    private int index;
    private int frame;
    private long actionStart;
    private Path pendingStill;
    private boolean cursorShown;
    private double cursorX;
    private double cursorY;

    private interface Action {
        boolean run(int frame);
    }

    private record Key(int move, int hold, Vec3 feet, Vec3 target) {
    }

    private record Stop(int frame, Supplier<double[]> where) {
    }

    private Showcase(Path out, String ffmpeg, long seed) {
        this.out = out;
        this.recorder = new Recorder(ffmpeg);
        this.seed = seed;
    }

    public static void start() {
        String ffmpeg = System.getProperty("lidless.ffmpeg", "ffmpeg");
        long seed = Long.parseLong(System.getProperty("lidless.seed", "20260926"));
        instance = new Showcase(Path.of(System.getProperty("lidless.showcase")), ffmpeg, seed);
    }

    public static boolean hideHand() {
        return instance != null && instance.started;
    }

    public static void frame() {
        if (instance != null) {
            instance.onFrame();
        }
    }

    public static void drawCursor(GuiGraphicsExtractor graphics) {
        if (instance == null || !instance.started || !instance.cursorShown) {
            return;
        }
        float cell = 2.0F / (float) Minecraft.getInstance().getWindow().getGuiScale();
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) instance.cursorX, (float) instance.cursorY);
        graphics.pose().scale(cell, cell);
        for (int row = 0; row < ARROW.length; row++) {
            String line = ARROW[row];
            int x = 0;
            while (x < line.length()) {
                char c = line.charAt(x);
                int end = x;
                while (end < line.length() && line.charAt(end) == c) {
                    end++;
                }
                if (c != ' ') {
                    graphics.fill(x, row, end, row + 1, c == 'B' ? 0xFF000000 : 0xFFFFFFFF);
                }
                x = end;
            }
        }
        graphics.pose().popMatrix();
    }

    private void onFrame() {
        Minecraft mc = Minecraft.getInstance();
        if (finished) {
            return;
        }
        if (!started) {
            if (mc.level == null && Screens.overlay(mc) == null && Screens.current(mc) != null && ++idle > 120) {
                started = true;
                setUpOptions(mc);
                plan(mc);
                log("creating world with seed " + seed);
                deleteWorld(mc);
                Worlds.create(mc, WORLD, false, seed);
            }
            return;
        }
        Clock.step();
        mc.gui.toastManager().clear();
        try {
            while (index < actions.size()) {
                if (frame == 0) {
                    actionStart = System.nanoTime();
                }
                if (!actions.get(index).run(frame++)) {
                    if (cursorShown && Screens.current(mc) != null) {
                        double scale = mc.getWindow().getGuiScale();
                        Ui.moveMouse(mc, cursorX * scale, cursorY * scale);
                    }
                    return;
                }
                index++;
                frame = 0;
            }
        } catch (Throwable e) {
            log("FAILED: " + e);
            e.printStackTrace();
        }
        finish(mc);
    }

    private void finish(Minecraft mc) {
        finished = true;
        Clock.release();
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(90_000L);
            } catch (InterruptedException e) {
                return;
            }
            log("the game did not close, forcing it");
            Runtime.getRuntime().halt(1);
        }, "lidless-showcase-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        LidlessClient.config().resetToDefaults();
        LidlessClient.saveConfig();
        log("showcase done");
        mc.stop();
    }

    private static void setUpOptions(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        mc.options.tutorialStep = TutorialSteps.NONE;
        mc.options.chatVisibility().set(ChatVisiblity.HIDDEN);
        mc.options.enableVsync().set(false);
        mc.options.framerateLimit().set(260);
        mc.options.renderDistance().set(12);
        mc.options.bobView().set(false);
        mc.options.setCameraType(CameraType.FIRST_PERSON);
    }

    private void once(Runnable action) {
        actions.add(f -> {
            action.run();
            return true;
        });
    }

    private void until(BooleanSupplier ready) {
        actions.add(f -> ready.getAsBoolean());
    }

    private void holdFor(int frames, Runnable hold) {
        actions.add(f -> {
            hold.run();
            return f >= frames;
        });
    }

    private void settle(Minecraft mc, Runnable hold) {
        actions.add(f -> {
            hold.run();
            return f >= 90 && System.nanoTime() - actionStart > SETTLE_NANOS
                    && (mc.levelRenderer.hasRenderedAllSections() || System.nanoTime() - actionStart > 20 * SETTLE_NANOS);
        });
    }

    private void record(String name, int frames, IntConsumer script) {
        actions.add(f -> {
            if (f == 0) {
                log("recording " + name);
                recorder.start(out.resolve("clips").resolve(name + ".mp4"));
            } else {
                Path still = pendingStill;
                pendingStill = null;
                recorder.capture(still);
            }
            if (f < frames) {
                script.accept(f);
                return false;
            }
            return true;
        });
        until(recorder::drained);
        once(recorder::stop);
    }

    private void shot(String name) {
        pendingStill = out.resolve("stills").resolve(name + ".png");
    }

    static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return a.add(b.subtract(a).scale(t));
    }

    static void run(Minecraft mc, String command) {
        IntegratedServer server = mc.getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
    }

    private static void cmd(Minecraft mc, String format, Object... args) {
        run(mc, String.format(Locale.ROOT, format, args));
    }

    private static void face(Minecraft mc, double yaw, double pitch) {
        LocalPlayer player = mc.player;
        player.setYRot((float) yaw);
        player.setXRot((float) pitch);
        player.yRotO = (float) yaw;
        player.xRotO = (float) pitch;
        player.setYHeadRot((float) yaw);
        player.yHeadRotO = (float) yaw;
    }

    private static void lookAt(Minecraft mc, Vec3 target) {
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        face(mc, Math.toDegrees(Math.atan2(-dx, dz)), -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
    }

    private static void stand(Minecraft mc, Vec3 feet) {
        LocalPlayer player = mc.player;
        player.setPos(feet.x, feet.y, feet.z);
        player.xo = player.xOld = feet.x;
        player.yo = player.yOld = feet.y;
        player.zo = player.zOld = feet.z;
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void follow(Minecraft mc, int f, Key... keys) {
        Vec3 feet = keys[0].feet();
        Vec3 target = keys[0].target();
        int t = f - keys[0].hold();
        for (int i = 1; i < keys.length && t >= 0; i++) {
            Key key = keys[i];
            if (t < key.move()) {
                double s = smooth(t / (double) key.move());
                feet = lerp(keys[i - 1].feet(), key.feet(), s);
                target = lerp(keys[i - 1].target(), key.target(), s);
                break;
            }
            t -= key.move();
            feet = key.feet();
            target = key.target();
            t -= key.hold();
        }
        stand(mc, feet);
        lookAt(mc, target);
    }

    private void cursor(int f, Stop... stops) {
        double[] at = stops[0].where().get();
        for (int i = 1; i < stops.length; i++) {
            Stop stop = stops[i];
            if (f >= stop.frame()) {
                at = stop.where().get();
            } else if (f >= stop.frame() - GLIDE) {
                double[] to = stop.where().get();
                double s = smooth((f - (stop.frame() - GLIDE)) / (double) GLIDE);
                at = new double[] {lerp(at[0], to[0], s), lerp(at[1], to[1], s)};
                break;
            } else {
                break;
            }
        }
        cursorShown = true;
        cursorX = at[0];
        cursorY = at[1];
    }

    private static void select(Minecraft mc, int slot) {
        mc.player.getInventory().setSelectedSlot(slot);
    }

    private static void closeScreen(Minecraft mc) {
        if (mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu) {
            mc.player.closeContainer();
        }
        Screens.open(mc, null);
    }

    private static boolean wanted(String scene) {
        String only = System.getProperty("lidless.scenes");
        return only == null || List.of(only.split(",")).contains(scene);
    }

    private Vec3 at(double dx, double dy, double dz) {
        BlockPos c = site.get();
        return new Vec3(c.getX() + dx + 0.5, c.getY() + dy, c.getZ() + dz + 0.5);
    }

    private BlockPos block(int dx, int dy, int dz) {
        return site.get().offset(dx, dy, dz);
    }

    private void locate(Minecraft mc) {
        once(() -> {
            IntegratedServer server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerLevel level = server.overworld();
                Pair<BlockPos, Holder<Biome>> pair = level.findClosestBiome3d(h -> h.is(Biomes.PLAINS),
                        BlockPos.ZERO, 6400, 32, 64);
                BlockPos found = pair == null ? BlockPos.ZERO : pair.getFirst();
                int y = level.getChunk(found.getX() >> 4, found.getZ() >> 4)
                        .getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, found.getX() & 15, found.getZ() & 15);
                site.set(new BlockPos(found.getX(), y + 1, found.getZ()));
                log("plains at " + site.get().toShortString());
            });
        });
        until(() -> site.get() != null);
    }

    private static void fill(Minecraft mc, int x1, int y1, int z1, int x2, int y2, int z2, String block) {
        int layers = Math.max(1, 32768 / ((Math.abs(x2 - x1) + 1) * (Math.abs(z2 - z1) + 1)));
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y += layers) {
            cmd(mc, "fill %d %d %d %d %d %d %s", x1, y, z1, x2, Math.min(y + layers - 1, Math.max(y1, y2)), z2, block);
        }
    }

    private void set(Minecraft mc, int dx, int dy, int dz, String block) {
        BlockPos c = site.get();
        cmd(mc, "setblock %d %d %d minecraft:%s", c.getX() + dx, c.getY() + dy, c.getZ() + dz, block);
    }

    private void reset(Minecraft mc, int dx, int dy, int dz, String block) {
        set(mc, dx, dy, dz, "air");
        set(mc, dx, dy, dz, block);
    }

    private static String item(int slot, String id, int count) {
        return "{Slot:" + slot + "b,id:\"minecraft:" + id + "\",count:" + count + "}";
    }

    private static String items(String... stacks) {
        return "Items:[" + String.join(",", stacks) + "]";
    }

    private static String boxed(int slot, String id, int count) {
        return "{slot:" + slot + ",item:{id:\"minecraft:" + id + "\",count:" + count + "}}";
    }

    private static String shulker(String color, String name, String... stacks) {
        String id = color == null ? "shulker_box" : color + "_shulker_box";
        String named = name == null ? "" : "minecraft:custom_name=\"" + name + "\",";
        return "minecraft:" + id + "[" + named + "minecraft:container=[" + String.join(",", stacks) + "]]";
    }

    private static final String ORES = items(
            item(0, "raw_iron", 64), item(1, "raw_iron", 41), item(2, "raw_gold", 23), item(3, "raw_copper", 64),
            item(4, "coal", 52), item(5, "redstone", 38), item(6, "lapis_lazuli", 27), item(7, "diamond", 11),
            item(8, "emerald", 6), item(9, "ancient_debris", 2), item(10, "quartz", 30), item(11, "amethyst_shard", 14));
    private static final String SUPPLIES = items(
            item(0, "diamond", 12), item(1, "iron_ingot", 64), item(2, "gold_ingot", 23), item(4, "emerald", 9),
            item(5, "redstone", 48), item(7, "lapis_lazuli", 31), item(9, "bread", 16), item(10, "cooked_beef", 24),
            item(11, "golden_carrot", 20), item(13, "iron_ingot", 30), item(15, "ender_pearl", 8),
            item(17, "experience_bottle", 16), item(19, "torch", 64), item(21, "arrow", 64), item(22, "arrow", 27),
            item(24, "name_tag", 2), item(26, "saddle", 1));
    private static final String TOOLS = items(
            item(0, "diamond_pickaxe", 1), item(1, "diamond_axe", 1), item(2, "diamond_shovel", 1),
            item(3, "iron_sword", 1), item(4, "shears", 1), item(5, "flint_and_steel", 1), item(9, "bow", 1),
            item(10, "fishing_rod", 1), item(11, "spyglass", 1), item(13, "shield", 1), item(14, "bucket", 3),
            item(15, "water_bucket", 1), item(18, "compass", 1), item(19, "clock", 1));
    private static final String FARM = items(
            item(0, "wheat_seeds", 64), item(1, "carrot", 48), item(2, "potato", 40), item(3, "beetroot_seeds", 22),
            item(4, "bone_meal", 64), item(5, "sugar_cane", 30), item(9, "wheat", 64), item(10, "pumpkin", 12),
            item(11, "melon_slice", 36), item(12, "egg", 16), item(13, "bamboo", 50));
    private static final String LEFT_HALF = items(
            item(0, "oak_log", 64), item(1, "dark_oak_log", 30), item(2, "spruce_log", 64), item(3, "birch_log", 48),
            item(4, "oak_planks", 64), item(5, "stick", 40), item(6, "cherry_log", 20), item(9, "cobblestone", 64),
            item(10, "cobblestone", 30), item(11, "cobbled_deepslate", 64), item(12, "stone", 64), item(13, "deepslate", 45),
            item(14, "granite", 20), item(15, "andesite", 33), item(16, "diorite", 12), item(18, "glass", 32),
            item(19, "white_wool", 16), item(20, "bricks", 64), item(21, "terracotta", 28), item(23, "stone_bricks", 64));
    private static final String RIGHT_HALF = items(
            item(0, "sand", 64), item(1, "gravel", 27), item(2, "clay_ball", 30), item(4, "sandstone", 40),
            item(9, "dirt", 64), item(10, "dirt", 12), item(11, "moss_block", 8), item(13, "mud_bricks", 22),
            item(18, "torch", 64), item(19, "lantern", 6), item(20, "chain", 12), item(22, "ladder", 18),
            item(24, "oak_fence", 20), item(25, "oak_door", 3));
    private static final String MESSY = items(
            item(0, "stick", 10), item(2, "dirt", 30), item(3, "iron_ingot", 5), item(5, "cobblestone", 50),
            item(7, "bread", 3), item(8, "diamond", 2), item(9, "stick", 20), item(11, "oak_log", 17),
            item(12, "dirt", 40), item(14, "iron_ingot", 12), item(15, "blue_shulker_box", 1),
            item(16, "cobblestone", 30), item(17, "torch", 16), item(19, "bread", 6), item(20, "oak_log", 9),
            item(22, "gold_ingot", 7), item(23, "raw_iron", 11), item(25, "torch", 20), item(26, "iron_pickaxe", 1))
            .replace("{Slot:15b,id:\"minecraft:blue_shulker_box\",count:1}",
                    "{Slot:15b,id:\"minecraft:blue_shulker_box\",count:1,components:{\"minecraft:custom_name\":\"Mining\","
                            + "\"minecraft:container\":[" + boxed(0, "iron_ore", 24) + "," + boxed(1, "coal", 40) + ","
                            + boxed(2, "deepslate_iron_ore", 9) + "]}}");
    private static final String PARTIAL = items(
            item(0, "cobblestone", 64), item(1, "cobblestone", 20), item(2, "dirt", 32), item(9, "oak_log", 16),
            item(10, "torch", 12), item(11, "andesite", 40));

    private void buildSet(Minecraft mc) {
        BlockPos c = site.get();
        int x = c.getX();
        int z = c.getZ();
        int ground = c.getY() - 1;
        fill(mc, x - 22, ground + 1, z - 22, x + 22, ground + 40, z + 22, "minecraft:air");
        fill(mc, x - 22, ground - 6, z - 22, x + 22, ground - 1, z + 22, "minecraft:dirt");
        fill(mc, x - 22, ground, z - 22, x + 22, ground, z + 22, "minecraft:grass_block");
        String[] kinds = {"oak", "birch", "fancy_oak"};
        int[][] trees = {{-6, 13}, {4, 15}, {12, 11}, {-15, 10}, {19, 3}, {-19, -4}, {15, -15}, {-14, -17}, {2, -19},
                {20, 19}, {-4, 20}, {11, 20}, {-20, 16}};
        for (int i = 0; i < trees.length; i++) {
            cmd(mc, "place feature minecraft:%s %d %d %d", kinds[i % kinds.length], x + trees[i][0], ground + 1,
                    z + trees[i][1]);
        }
        Random random = new Random(seed);
        String[] flowers = {"dandelion", "poppy", "oxeye_daisy", "cornflower", "azure_bluet"};
        for (int dx = -21; dx <= 21; dx++) {
            for (int dz = -21; dz <= 21; dz++) {
                boolean yard = dx >= -8 && dx <= 7 && dz >= -2 && dz <= 6;
                boolean paddock = dx >= -12 && dx <= -5 && dz >= -5 && dz <= 4;
                if (yard || paddock) {
                    continue;
                }
                double roll = random.nextDouble();
                String block;
                if (roll < 0.12) {
                    block = "short_grass";
                } else if (roll < 0.14) {
                    block = "tall_grass";
                } else if (roll < 0.16) {
                    block = flowers[random.nextInt(flowers.length)];
                } else {
                    continue;
                }
                if (block.equals("tall_grass")) {
                    cmd(mc, "setblock %d %d %d minecraft:tall_grass[half=lower]", x + dx, ground + 1, z + dz);
                    cmd(mc, "setblock %d %d %d minecraft:tall_grass[half=upper]", x + dx, ground + 2, z + dz);
                } else {
                    cmd(mc, "setblock %d %d %d minecraft:%s", x + dx, ground + 1, z + dz, block);
                }
            }
        }
        fill(mc, x - 7, ground, z - 1, x + 6, ground, z + 6, "minecraft:spruce_planks");
        fill(mc, x - 7, ground + 1, z + 5, x + 5, ground + 2, z + 5, "minecraft:bookshelf");
        for (int dx : new int[] {-7, -3, 1, 5}) {
            fill(mc, x + dx, ground + 1, z + 5, x + dx, ground + 3, z + 5, "minecraft:stripped_spruce_log");
            set(mc, dx, 3, 5, "lantern[hanging=false]");
        }
        set(mc, -7, 0, 4, "potted_fern");
        set(mc, 6, 0, 4, "potted_red_tulip");

        set(mc, 4, 0, ROW, "furnace[facing=north]{" + items(item(0, "raw_iron", 18), item(2, "iron_ingot", 24)) + "}");
        set(mc, 3, 0, ROW, "chest[facing=north]{LootTable:\"minecraft:chests/simple_dungeon\"}");
        set(mc, 2, 0, ROW, "lime_shulker_box[facing=up]{CustomName:\"Farm\"," + FARM + "}");
        set(mc, 1, 0, ROW, "ender_chest[facing=north]");
        set(mc, 0, 0, ROW, "chest[facing=north,type=right]{" + RIGHT_HALF + "}");
        set(mc, -1, 0, ROW, "chest[facing=north,type=left]{" + LEFT_HALF + "}");
        set(mc, -2, 0, ROW, "hopper[facing=down]{" + items(item(0, "iron_nugget", 17), item(2, "rotten_flesh", 5),
                item(4, "bone", 3)) + "}");
        set(mc, -3, 0, ROW, "purple_shulker_box[facing=up]{CustomName:\"Tools\"," + TOOLS + "}");
        set(mc, STASH, 0, ROW, "chest[facing=north]{" + SUPPLIES + "}");
        set(mc, -5, 0, ROW, "barrel[facing=up]{CustomName:\"Ores\"," + ORES + "}");

        String[] ender = {"netherite_ingot 4", "elytra", "totem_of_undying 2", "enchanted_golden_apple 3",
                "shulker_shell 6", null, null, null, null, "diamond_block 5", "beacon", "end_crystal 4"};
        for (int i = 0; i < ender.length; i++) {
            if (ender[i] != null) {
                cmd(mc, "item replace entity @p enderchest.%d with minecraft:%s", i, ender[i]);
            }
        }

        run(mc, "kill @e[type=!minecraft:player]");
        Vec3 donkey = at(-9, 0, 2);
        cmd(mc, "summon minecraft:donkey %.2f %.2f %.2f {NoAI:1b,Tame:1b,ChestedHorse:1b,PersistenceRequired:1b,"
                + "Tags:[\"set\"],Rotation:[0f,0f]}", donkey.x, donkey.y, donkey.z);
        String[] packed = {"apple 12", "bread 20", "hay_block 9", null, null, "oak_sapling 16", "wheat_seeds 40",
                null, null, null, "cooked_porkchop 18", "lead 2"};
        for (int i = 0; i < packed.length; i++) {
            if (packed[i] != null) {
                cmd(mc, "item replace entity @e[type=minecraft:donkey,tag=set] horse.%d with minecraft:%s", i, packed[i]);
            }
        }
        Vec3 boat = at(-9, 0, -0.5);
        cmd(mc, "summon minecraft:oak_chest_boat %.2f %.2f %.2f {Tags:[\"set\"],Rotation:[0f,0f],%s}",
                boat.x, boat.y, boat.z, items(item(0, "oak_log", 32), item(1, "cod", 14), item(2, "salmon", 9),
                        item(3, "fishing_rod", 1), item(4, "lily_pad", 6), item(9, "prismarine_shard", 11)));
        set(mc, -9, 0, -3, "rail[shape=north_south]");
        Vec3 cart = at(-9, 0, -3);
        cmd(mc, "summon minecraft:chest_minecart %.2f %.2f %.2f {Tags:[\"set\"],%s}", cart.x, cart.y + 0.1, cart.z,
                items(item(0, "coal", 64), item(1, "torch", 32), item(2, "rail", 48), item(3, "powered_rail", 12),
                        item(4, "iron_ingot", 10), item(5, "redstone", 20), item(9, "minecart", 1)));
        worldHotbar(mc);
    }

    private static void worldHotbar(Minecraft mc) {
        run(mc, "clear @p");
        run(mc, "item replace entity @p hotbar.0 with minecraft:diamond_pickaxe");
        run(mc, "item replace entity @p hotbar.1 with minecraft:diamond_axe");
        run(mc, "item replace entity @p hotbar.2 with minecraft:torch 64");
        run(mc, "item replace entity @p hotbar.3 with minecraft:spruce_planks 64");
        run(mc, "item replace entity @p hotbar.4 with minecraft:bread 16");
    }

    private void scene(Minecraft mc, LidlessConfig config, boolean survival, Vec3 feet, Runnable setup, Runnable hold) {
        once(() -> {
            closeScreen(mc);
            config.resetToDefaults();
            cursorShown = false;
            run(mc, survival ? "gamemode survival @p" : "gamemode creative @p");
            cmd(mc, "tp @p %.3f %.3f %.3f", feet.x, feet.y, feet.z);
            run(mc, "kill @e[type=minecraft:item]");
            run(mc, "kill @e[type=!minecraft:player,tag=!set]");
            if (!survival) {
                worldHotbar(mc);
            }
            select(mc, 8);
        });
        holdFor(20, () -> {
            stand(mc, feet);
            hold.run();
        });
        if (!survival) {
            once(() -> worldHotbar(mc));
        }
        once(setup);
        settle(mc, hold);
    }

    private void openStash(Minecraft mc) {
        BlockPos pos = block(STASH, 0, ROW);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0, -0.5), Direction.NORTH, pos, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }

    private void plan(Minecraft mc) {
        LidlessConfig config = LidlessClient.config();

        until(() -> mc.level != null && mc.player != null && Screens.current(mc) == null
                && mc.getSingleplayerServer() != null);
        once(() -> {
            log("world loaded");
            Clock.fix();
            config.resetToDefaults();
            run(mc, "time set 2500");
            run(mc, "weather clear 1000000");
            run(mc, "gamerule advance_time false");
            run(mc, "gamerule spawn_mobs false");
        });
        locate(mc);
        once(() -> {
            BlockPos c = site.get();
            cmd(mc, "forceload add %d %d %d %d", c.getX() - 30, c.getZ() - 30, c.getX() + 30, c.getZ() + 30);
            cmd(mc, "tp @p %d %d %d", c.getX(), c.getY() + 45, c.getZ());
            mc.player.getAbilities().flying = true;
            mc.player.onUpdateAbilities();
        });
        settle(mc, () -> {
        });
        once(() -> buildSet(mc));
        holdFor(60, () -> {
        });
        once(() -> {
            mc.player.getAbilities().flying = false;
            mc.player.onUpdateAbilities();
        });
        once(() -> film(mc, config));
    }

    private void film(Minecraft mc, LidlessConfig config) {
        if (wanted("intro")) {
            Key from = new Key(0, 0, at(4.9, 0, 1), at(4.1, 0.45, ROW));
            Key to = new Key(360, 0, at(-4.3, 0, 1), at(-5.1, 0.45, ROW));
            scene(mc, config, false, from.feet(), () -> {
            }, () -> follow(mc, 0, from, to));
            record("01-intro", 360, f -> {
                follow(mc, f, from, to);
                if (f == 150) {
                    shot("row-1");
                } else if (f == 290) {
                    shot("row-2");
                }
            });
        }

        if (wanted("look")) {
            Key barrel = new Key(0, 50, at(-3.6, 0, 1), at(-5, 0.45, ROW));
            Key tools = new Key(GLIDE, 50, at(-1.8, 0, 1), at(-3, 0.45, ROW));
            Key ender = new Key(GLIDE, 50, at(0.2, 0, 1), at(1, 0.45, ROW));
            Key loot = new Key(GLIDE, 80, at(1.8, 0, 1), at(3, 0.45, ROW));
            scene(mc, config, false, barrel.feet(), () -> {
            }, () -> follow(mc, 0, barrel));
            record("02-look", 300, f -> {
                follow(mc, f, barrel, tools, ender, loot);
                if (f == 40) {
                    shot("peek-barrel");
                } else if (f == 115) {
                    shot("peek-shulker");
                } else if (f == 190) {
                    shot("peek-ender-chest");
                } else if (f == 280) {
                    shot("peek-loot");
                }
            });
        }

        if (wanted("compact")) {
            Vec3 feet = at(-1.0, 0, 1.1);
            scene(mc, config, false, feet, () -> config.setRows(6), () -> lookAt(mc, at(-0.55, 0.45, ROW)));
            record("03-compact", 270, f -> {
                if (f == 135) {
                    config.setCompact(false);
                }
                stand(mc, feet);
                lookAt(mc, at(lerp(-0.62, -0.48, smooth(f / 270.0)), 0.45, ROW));
                if (f == 110) {
                    shot("peek-compact");
                } else if (f == 250) {
                    shot("peek-slots");
                }
            });
        }

        if (wanted("mounts")) {
            Vec3 donkey = at(-9, 0.9, 2);
            Vec3 boat = at(-9, 0.4, -0.5);
            Vec3 cart = at(-9, 0.45, -3);
            Key first = new Key(0, 70, at(-11.8, 0, 1.3), donkey);
            Key second = new Key(30, 70, at(-11.8, 0, -0.5), boat);
            Key third = new Key(30, 100, at(-11.8, 0, -2.3), cart);
            scene(mc, config, false, first.feet(), () -> {
            }, () -> follow(mc, 0, first));
            record("04-mounts", 300, f -> {
                follow(mc, f, first, second, third);
                if (f == 55) {
                    shot("peek-donkey");
                } else if (f == 155) {
                    shot("peek-chest-boat");
                } else if (f == 270) {
                    shot("peek-chest-minecart");
                }
            });
        }

        if (wanted("tooltips")) {
            Vec3 feet = at(STASH, 0, 1);
            scene(mc, config, true, feet, () -> {
                run(mc, "clear @p");
                run(mc, "item replace entity @p hotbar.0 with minecraft:iron_sword");
                run(mc, "item replace entity @p hotbar.1 with minecraft:iron_pickaxe");
                run(mc, "item replace entity @p hotbar.2 with minecraft:torch 32");
                run(mc, "item replace entity @p hotbar.8 with minecraft:bread 12");
                run(mc, "item replace entity @p inventory.1 with " + shulker("purple", "Tools",
                        boxed(0, "diamond_pickaxe", 1), boxed(1, "diamond_axe", 1), boxed(2, "diamond_shovel", 1),
                        boxed(3, "iron_sword", 1), boxed(4, "shears", 1), boxed(9, "bow", 1), boxed(10, "arrow", 64),
                        boxed(11, "spyglass", 1), boxed(13, "shield", 1), boxed(18, "water_bucket", 1),
                        boxed(19, "torch", 64), boxed(20, "golden_carrot", 32)));
                run(mc, "item replace entity @p inventory.4 with " + shulker("lime", "Farm",
                        boxed(0, "wheat_seeds", 64), boxed(1, "carrot", 48), boxed(2, "potato", 40),
                        boxed(3, "beetroot_seeds", 22), boxed(4, "bone_meal", 64), boxed(5, "sugar_cane", 30),
                        boxed(9, "wheat", 64), boxed(10, "pumpkin", 12), boxed(11, "melon_slice", 36),
                        boxed(12, "egg", 16), boxed(13, "bamboo", 50)));
                run(mc, "item replace entity @p inventory.7 with minecraft:ender_chest");
                run(mc, "item replace entity @p inventory.12 with " + shulker(null, null,
                        boxed(0, "redstone", 64), boxed(1, "repeater", 12), boxed(2, "comparator", 6),
                        boxed(3, "piston", 16), boxed(4, "sticky_piston", 8), boxed(5, "observer", 10)));
                run(mc, "item replace entity @p inventory.10 with minecraft:cobblestone 40");
                run(mc, "item replace entity @p inventory.19 with minecraft:oak_log 12");
                run(mc, "item replace entity @p inventory.23 with minecraft:diamond_pickaxe");
            }, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            once(() -> Screens.open(mc, new InventoryScreen(mc.player)));
            holdFor(20, () -> cursor(0, new Stop(0, () -> rest(mc))));
            record("05-tooltips", 330, f -> {
                cursor(f, new Stop(0, () -> rest(mc)),
                        new Stop(40, () -> slotAt(mc, "purple_shulker_box")),
                        new Stop(140, () -> slotAt(mc, "lime_shulker_box")),
                        new Stop(240, () -> slotAt(mc, "ender_chest")));
                if (f == 110) {
                    shot("tooltip-shulker-tools");
                } else if (f == 210) {
                    shot("tooltip-shulker-farm");
                } else if (f == 310) {
                    shot("tooltip-ender-chest");
                }
            });
        }

        if (wanted("search")) {
            Vec3 feet = at(STASH, 0, 1);
            scene(mc, config, true, feet, () -> {
                messyInventory(mc);
                reset(mc, STASH, 0, ROW, "chest[facing=north]{" + MESSY + "}");
            }, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            holdFor(10, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            once(() -> openStash(mc));
            until(() -> Screens.current(mc) instanceof AbstractContainerScreen<?>);
            holdFor(20, () -> cursor(0, new Stop(0, () -> rest(mc))));
            record("06-search", 300, f -> {
                cursor(f, new Stop(0, () -> rest(mc)));
                Screen screen = Screens.current(mc);
                if (f == 35) {
                    Ui.key(screen, InputConstants.KEY_F, InputConstants.MOD_CONTROL);
                } else if (f >= 55 && f <= 85 && (f - 55) % 10 == 0) {
                    searchBox(screen).insertText(String.valueOf("iron".charAt((f - 55) / 10)));
                }
                if (f == 200) {
                    shot("search");
                }
            });
        }

        if (wanted("sort")) {
            Vec3 feet = at(STASH, 0, 1);
            scene(mc, config, true, feet, () -> {
                messyInventory(mc);
                reset(mc, STASH, 0, ROW, "chest[facing=north]{" + MESSY + "}");
            }, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            holdFor(10, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            once(() -> openStash(mc));
            until(() -> Screens.current(mc) instanceof AbstractContainerScreen<?>);
            holdFor(20, () -> cursor(0, new Stop(0, () -> rest(mc))));
            record("07-sort", 330, f -> {
                cursor(f, new Stop(0, () -> rest(mc)),
                        new Stop(50, () -> widgetAt(mc, "lidless.button.sort")),
                        new Stop(185, () -> widgetAt(mc, "lidless.button.sort_inventory")));
                if (f == 80) {
                    clickWidget(mc, "lidless.button.sort");
                } else if (f == 215) {
                    clickWidget(mc, "lidless.button.sort_inventory");
                }
                if (f == 60) {
                    shot("sort-hover");
                } else if (f == 170) {
                    shot("sorted");
                } else if (f == 310) {
                    shot("sorted-inventory");
                }
            });
        }

        if (wanted("deposit")) {
            Vec3 feet = at(STASH, 0, 1);
            scene(mc, config, true, feet, () -> {
                run(mc, "clear @p");
                run(mc, "item replace entity @p hotbar.0 with minecraft:iron_sword");
                run(mc, "item replace entity @p hotbar.1 with minecraft:iron_pickaxe");
                run(mc, "item replace entity @p hotbar.2 with minecraft:torch 32");
                run(mc, "item replace entity @p hotbar.3 with minecraft:cobblestone 24");
                run(mc, "item replace entity @p hotbar.8 with minecraft:bread 12");
                run(mc, "item replace entity @p inventory.0 with minecraft:cobblestone 64");
                run(mc, "item replace entity @p inventory.3 with minecraft:dirt 21");
                run(mc, "item replace entity @p inventory.5 with minecraft:oak_log 8");
                run(mc, "item replace entity @p inventory.9 with minecraft:andesite 33");
                run(mc, "item replace entity @p inventory.12 with minecraft:cobblestone 40");
                run(mc, "item replace entity @p inventory.14 with minecraft:raw_iron 9");
                run(mc, "item replace entity @p inventory.16 with minecraft:torch 20");
                run(mc, "item replace entity @p inventory.20 with minecraft:rotten_flesh 7");
                run(mc, "item replace entity @p inventory.24 with minecraft:string 4");
                reset(mc, STASH, 0, ROW, "chest[facing=north]{" + PARTIAL + "}");
            }, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            holdFor(10, () -> lookAt(mc, at(STASH, 0.45, ROW)));
            once(() -> openStash(mc));
            until(() -> Screens.current(mc) instanceof AbstractContainerScreen<?>);
            holdFor(20, () -> cursor(0, new Stop(0, () -> rest(mc))));
            record("08-deposit", 300, f -> {
                cursor(f, new Stop(0, () -> rest(mc)),
                        new Stop(50, () -> widgetAt(mc, "lidless.button.deposit")),
                        new Stop(180, () -> widgetAt(mc, "lidless.button.take")));
                if (f == 75) {
                    clickWidget(mc, "lidless.button.deposit");
                } else if (f == 205) {
                    clickWidget(mc, "lidless.button.take");
                }
                if (f == 150) {
                    shot("deposited");
                } else if (f == 285) {
                    shot("took-all");
                }
            });
        }

        if (wanted("placement")) {
            Vec3 feet = at(STASH + 0.3, 0, 1.2);
            scene(mc, config, false, feet,
                    () -> reset(mc, STASH, 0, ROW, "chest[facing=north]{" + SUPPLIES + "}"),
                    () -> lookAt(mc, at(STASH, 0.45, ROW)));
            record("09-placement", 300, f -> {
                if (f == 75) {
                    config.setXPosition(50);
                    config.setYPosition(0);
                } else if (f == 150) {
                    config.setXPosition(0);
                    config.setYPosition(50);
                } else if (f == 225) {
                    config.setXPosition(0);
                    config.setYPosition(100);
                }
                stand(mc, feet);
                lookAt(mc, at(STASH + lerp(-0.08, 0.08, smooth(f / 300.0)), 0.45, ROW));
                if (f == 120) {
                    shot("position-top");
                } else if (f == 195) {
                    shot("position-left");
                }
            });
        }

        if (wanted("settings")) {
            Vec3 feet = at(0, 0, 1);
            scene(mc, config, false, feet, () -> {
            }, () -> lookAt(mc, at(-0.5, 0.45, ROW)));
            once(() -> Screens.open(mc, new LidlessSettingsScreen(null, mc.options)));
            holdFor(20, () -> cursor(0, new Stop(0, () -> corner(mc))));
            record("10-settings", 270, f -> {
                cursor(f, new Stop(0, () -> corner(mc)),
                        new Stop(35, () -> labelAt(mc, "Compact", 0.5)),
                        new Stop(80, () -> labelAt(mc, "Sort By", 0.5)),
                        new Stop(125, () -> sliderAt(mc, "Peek Rows", 0.8)),
                        new Stop(170, () -> sliderAt(mc, "Horizontal", 0.3)),
                        new Stop(215, () -> sliderAt(mc, "Vertical", 0.2)));
                if (f == 45) {
                    clickLabel(mc, "Compact", 0.5);
                } else if (f == 90) {
                    clickLabel(mc, "Sort By", 0.5);
                } else if (f == 135) {
                    clickSlider(mc, "Peek Rows", 0.8);
                } else if (f == 180) {
                    clickSlider(mc, "Horizontal", 0.3);
                } else if (f == 225) {
                    clickSlider(mc, "Vertical", 0.2);
                } else if (f == 255) {
                    shot("settings");
                }
            });
            once(() -> Screens.open(mc, null));
        }

        if (wanted("outro")) {
            once(() -> run(mc, "time set 12300"));
            Key from = new Key(0, 0, at(1.6, 0, 0.3), at(1.0, 0.45, ROW));
            Key to = new Key(330, 0, at(-1.6, 0, 0.3), at(-2.2, 0.45, ROW));
            scene(mc, config, false, from.feet(), () -> {
            }, () -> follow(mc, 0, from, to));
            record("11-outro", 330, f -> {
                follow(mc, f, from, to);
                if (f == 150) {
                    shot("sunset");
                }
            });
            once(() -> run(mc, "time set 2500"));
        }
    }

    private static void messyInventory(Minecraft mc) {
        run(mc, "clear @p");
        run(mc, "item replace entity @p hotbar.0 with minecraft:iron_sword");
        run(mc, "item replace entity @p hotbar.1 with minecraft:iron_pickaxe");
        run(mc, "item replace entity @p hotbar.2 with minecraft:torch 32");
        run(mc, "item replace entity @p hotbar.8 with minecraft:bread 12");
        run(mc, "item replace entity @p inventory.0 with minecraft:cobblestone 64");
        run(mc, "item replace entity @p inventory.1 with minecraft:dirt 21");
        run(mc, "item replace entity @p inventory.5 with minecraft:raw_iron 9");
        run(mc, "item replace entity @p inventory.9 with minecraft:andesite 33");
        run(mc, "item replace entity @p inventory.12 with minecraft:oak_log 8");
        run(mc, "item replace entity @p inventory.17 with minecraft:iron_ingot 6");
        run(mc, "item replace entity @p inventory.20 with minecraft:rotten_flesh 7");
        run(mc, "item replace entity @p inventory.22 with minecraft:cobblestone 17");
        run(mc, "item replace entity @p inventory.24 with minecraft:string 4");
        run(mc, "item replace entity @p inventory.26 with minecraft:dirt 9");
    }

    private static double[] rest(Minecraft mc) {
        Screen screen = Screens.current(mc);
        return new double[] {screen.width * 0.5 + 118, screen.height * 0.5 - 28};
    }

    private static double[] corner(Minecraft mc) {
        Screen screen = Screens.current(mc);
        return new double[] {screen.width - 36, screen.height - 30};
    }

    private static double[] slotAt(Minecraft mc, String item) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Screens.current(mc);
        ContainerScreenAccess access = (ContainerScreenAccess) screen;
        for (Slot slot : screen.getMenu().slots) {
            if (!slot.getItem().isEmpty()
                    && BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).getPath().equals(item)) {
                return new double[] {access.lidless$leftPos() + slot.x + 9, access.lidless$topPos() + slot.y + 9};
            }
        }
        throw new IllegalStateException("no " + item + " on screen");
    }

    private static AbstractWidget widget(Screen screen, String key) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget
                    && widget.getMessage().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(key)) {
                return widget;
            }
        }
        throw new IllegalStateException("no button " + key);
    }

    private static double[] widgetAt(Minecraft mc, String key) {
        AbstractWidget widget = widget(Screens.current(mc), key);
        return new double[] {widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0};
    }

    private static void clickWidget(Minecraft mc, String key) {
        double[] at = widgetAt(mc, key);
        Ui.click(Screens.current(mc), at[0], at[1]);
    }

    private static EditBox searchBox(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof EditBox box) {
                return box;
            }
        }
        throw new IllegalStateException("no search box on " + screen);
    }

    private static double[] labelAt(Minecraft mc, String label, double along) {
        AbstractWidget widget = find(Screens.current(mc), label);
        if (widget == null) {
            throw new IllegalStateException("no widget " + label);
        }
        return new double[] {widget.getX() + widget.getWidth() * along, widget.getY() + widget.getHeight() / 2.0};
    }

    private static double[] sliderAt(Minecraft mc, String label, double value) {
        AbstractWidget widget = find(Screens.current(mc), label);
        if (widget == null) {
            throw new IllegalStateException("no slider " + label);
        }
        return new double[] {widget.getX() + 4 + value * (widget.getWidth() - 8), widget.getY() + widget.getHeight() / 2.0};
    }

    private static void clickSlider(Minecraft mc, String label, double value) {
        double[] at = sliderAt(mc, label, value);
        Ui.click(Screens.current(mc), at[0], at[1]);
    }

    private static void clickLabel(Minecraft mc, String label, double along) {
        double[] at = labelAt(mc, label, along);
        Ui.click(Screens.current(mc), at[0], at[1]);
    }

    private static AbstractWidget find(GuiEventListener node, String label) {
        if (node instanceof AbstractWidget widget && widget.getMessage().getString().startsWith(label)) {
            return widget;
        }
        if (node instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                AbstractWidget found = find(child, label);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void deleteWorld(Minecraft mc) {
        Path dir = mc.gameDirectory.toPath().resolve("saves").resolve(WORLD);
        if (Files.exists(dir)) {
            try (var paths = Files.walk(dir)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static void log(String message) {
        System.out.println("[lidless-showcase] " + message);
    }
}
