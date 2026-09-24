package dev.lidless.peek;

import dev.lidless.memory.ContainerMemory;
import dev.lidless.memory.InteractionTracker;
import dev.lidless.memory.MemoryKeys;
import dev.lidless.memory.Remembered;
import dev.lidless.mixin.HorseInventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PeekResolver {
    private static final LiveReads<PeekTarget> TARGETS = new LiveReads<>();
    private static final LiveReads<List<ItemStack>> ENDER = new LiveReads<>();

    private PeekResolver() {
    }

    public static PeekTarget resolve(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (mc.level == null || mc.player == null || hit == null) {
            return null;
        }
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return block(mc, mc.level, blockHit.getBlockPos());
        }
        if (hit instanceof EntityHitResult entityHit && hit.getType() == HitResult.Type.ENTITY) {
            return entity(mc, entityHit.getEntity());
        }
        return null;
    }

    public static PeekTarget block(Minecraft mc, ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Component name = state.getBlock().getName();
        ItemStack icon = new ItemStack(state.getBlock());
        if (state.getBlock() instanceof EnderChestBlock) {
            return enderChest(mc, name, icon);
        }
        if (!(level.getBlockEntity(pos) instanceof Container)) {
            return null;
        }

        String key = MemoryKeys.block(level.dimension(), pos);
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            ResourceKey<Level> dimension = level.dimension();
            BlockPos at = pos.immutable();
            return TARGETS.fetch(server, key, running -> {
                ServerLevel serverLevel = running.getLevel(dimension);
                return serverLevel == null ? null : liveBlock(serverLevel, at, name, icon);
            });
        }
        return remembered(name, icon, ContainerMemory.get(mc, key));
    }

    public static PeekTarget entity(Minecraft mc, Entity entity) {
        boolean storage = entity instanceof ContainerEntity
                || entity instanceof AbstractChestedHorse horse && horse.hasChest();
        if (!storage) {
            return null;
        }
        Component name = entity.getDisplayName();
        ItemStack pick = entity.getPickResult();
        ItemStack icon = pick == null ? ItemStack.EMPTY : pick;

        String key = MemoryKeys.entity(entity);
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null && mc.level != null) {
            ResourceKey<Level> dimension = mc.level.dimension();
            int id = entity.getId();
            return TARGETS.fetch(server, key, running -> {
                ServerLevel serverLevel = running.getLevel(dimension);
                return serverLevel == null ? null : liveEntity(serverLevel.getEntity(id), name, icon);
            });
        }
        return remembered(name, icon, ContainerMemory.get(mc, key));
    }

    private static PeekTarget liveEntity(Entity twin, Component name, ItemStack icon) {
        if (twin instanceof ContainerEntity container) {
            if (container.getContainerLootTable() != null) {
                return new PeekTarget(name, icon, List.of(), PeekTarget.Status.LOOT, 0L);
            }
            return new PeekTarget(name, icon, copy(container), PeekTarget.Status.LIVE, 0L);
        }
        if (twin instanceof AbstractChestedHorse horse) {
            Container inventory = ((HorseInventoryAccessor) horse).lidless$inventory();
            return new PeekTarget(name, icon, copy(inventory), PeekTarget.Status.LIVE, 0L);
        }
        return null;
    }

    public static List<ItemStack> enderItems(Minecraft mc) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null && mc.player != null) {
            UUID uuid = mc.player.getUUID();
            return ENDER.fetch(server, MemoryKeys.ENDER_CHEST, running -> {
                ServerPlayer player = running.getPlayerList().getPlayer(uuid);
                return player == null ? null : copy(player.getEnderChestInventory());
            });
        }
        Remembered remembered = ContainerMemory.get(mc, MemoryKeys.ENDER_CHEST);
        return remembered == null ? null : remembered.items();
    }

    private static PeekTarget enderChest(Minecraft mc, Component name, ItemStack icon) {
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) {
            List<ItemStack> items = enderItems(mc);
            return items == null ? null : new PeekTarget(name, icon, items, PeekTarget.Status.LIVE, 0L);
        }
        return remembered(name, icon, ContainerMemory.get(mc, MemoryKeys.ENDER_CHEST));
    }

    private static PeekTarget liveBlock(ServerLevel level, BlockPos pos, Component name, ItemStack icon) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        if (blockEntity instanceof Nameable nameable && nameable.hasCustomName()) {
            name = nameable.getCustomName();
        }

        if (state.getBlock() instanceof ChestBlock chest) {
            BlockPos other = InteractionTracker.otherHalf(state, pos);
            if (lootPending(blockEntity) || other != null && lootPending(level.getBlockEntity(other))) {
                return new PeekTarget(name, icon, List.of(), PeekTarget.Status.LOOT, 0L);
            }
            Container combined = ChestBlock.getContainer(chest, state, level, pos, true);
            if (combined != null) {
                container = combined;
                if (other != null && name.equals(state.getBlock().getName())) {
                    name = Component.translatable("container.chestDouble");
                }
            }
        } else if (lootPending(blockEntity)) {
            return new PeekTarget(name, icon, List.of(), PeekTarget.Status.LOOT, 0L);
        }
        return new PeekTarget(name, icon, copy(container), PeekTarget.Status.LIVE, 0L);
    }

    private static boolean lootPending(BlockEntity blockEntity) {
        return blockEntity instanceof RandomizableContainer randomizable && randomizable.getLootTable() != null;
    }

    private static PeekTarget remembered(Component name, ItemStack icon, Remembered remembered) {
        if (remembered == null) {
            return new PeekTarget(name, icon, List.of(), PeekTarget.Status.UNKNOWN, 0L);
        }
        Component title = remembered.title() == null ? name : remembered.title();
        return new PeekTarget(title, icon, remembered.items(), PeekTarget.Status.REMEMBERED, remembered.seenAt());
    }

    private static List<ItemStack> copy(Container container) {
        List<ItemStack> items = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) {
            items.add(container.getItem(i).copy());
        }
        return items;
    }
}
