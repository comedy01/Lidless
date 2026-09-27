package dev.lidless.selftest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Properties;

final class Worlds {
    private Worlds() {
    }

    static void createFlat(Minecraft mc, String name) {
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), DataPackConfig.DEFAULT);
        RegistryAccess.RegistryHolder registries = RegistryAccess.builtin();
        Properties properties = new Properties();
        properties.setProperty("level-type", "flat");
        mc.createLevel(name, settings, registries, WorldGenSettings.create(registries, properties));
    }
}
