package dev.lidless.selftest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

final class Worlds {
    private Worlds() {
    }

    static void createFlat(Minecraft mc, String name) {
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), DataPackConfig.DEFAULT);
        RegistryAccess registries = RegistryAccess.BUILTIN.get();
        WorldGenSettings generation = registries.registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldGenSettings(0L, false, false);
        mc.createWorldOpenFlows().createFreshLevel(name, settings, registries, generation);
    }
}
