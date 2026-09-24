package dev.lidless.memory;

import dev.lidless.client.LidlessClient;
import dev.lidless.peek.Mounts;
import dev.lidless.storage.StorageSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.List;

public final class InteractionTracker {
    private static final long BIND_WINDOW_MS = 3000L;

    private static BlockPos lastBlock;
    private static long lastBlockTime;
    private static Entity lastEntity;
    private static long lastEntityTime;
    private static AbstractContainerMenu boundMenu;
    private static List<String> boundKeys = List.of();

    private InteractionTracker() {
    }

    public static void onUseBlock(BlockPos pos) {
        lastBlock = pos.immutable();
        lastBlockTime = System.currentTimeMillis();
    }

    public static void onInteractEntity(Entity entity) {
        lastEntity = entity;
        lastEntityTime = System.currentTimeMillis();
    }

    public static void onScreenOpened(Minecraft mc, AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        if (menu == boundMenu) {
            return;
        }
        boundMenu = menu;
        boundKeys = keysFor(mc, menu);
    }

    public static void onScreenClosed(Minecraft mc, AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        if (menu != boundMenu) {
            return;
        }
        List<String> keys = boundKeys;
        boundMenu = null;
        boundKeys = List.of();
        if (keys.isEmpty() || !LidlessClient.config().remember() || ContainerMemory.worldId(mc) == null) {
            return;
        }
        List<Slot> slots = StorageSlots.container(menu);
        if (slots.isEmpty()) {
            return;
        }
        List<ItemStack> items = new ArrayList<>(slots.size());
        for (Slot slot : slots) {
            items.add(slot.getItem().copy());
        }
        ContainerMemory.put(mc, keys, new Remembered(screen.getTitle(), items, System.currentTimeMillis()));
    }

    private static List<String> keysFor(Minecraft mc, AbstractContainerMenu menu) {
        ClientLevel level = mc.level;
        if (level == null) {
            return List.of();
        }
        if (Mounts.isMountMenu(menu)) {
            Entity entity = Mounts.mount(menu);
            return Mounts.isChested(entity) ? List.of(MemoryKeys.entity(entity)) : List.of();
        }

        long now = System.currentTimeMillis();
        boolean entityRecent = lastEntity != null && now - lastEntityTime < BIND_WINDOW_MS;
        boolean blockRecent = lastBlock != null && now - lastBlockTime < BIND_WINDOW_MS;
        if (entityRecent && (!blockRecent || lastEntityTime >= lastBlockTime)) {
            return lastEntity instanceof ContainerEntity ? List.of(MemoryKeys.entity(lastEntity)) : List.of();
        }
        if (!blockRecent) {
            return List.of();
        }

        BlockState state = level.getBlockState(lastBlock);
        if (state.getBlock() instanceof EnderChestBlock) {
            return List.of(MemoryKeys.ENDER_CHEST);
        }
        BlockEntity blockEntity = level.getBlockEntity(lastBlock);
        if (!(blockEntity instanceof Container)) {
            return List.of();
        }
        List<String> keys = new ArrayList<>(2);
        keys.add(MemoryKeys.block(level.dimension(), lastBlock));
        BlockPos other = otherHalf(state, lastBlock);
        if (other != null) {
            keys.add(MemoryKeys.block(level.dimension(), other));
        }
        return keys;
    }

    public static BlockPos otherHalf(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            return pos.relative(ChestBlock.getConnectedDirection(state));
        }
        return null;
    }
}
