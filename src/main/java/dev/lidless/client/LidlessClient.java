package dev.lidless.client;

import dev.lidless.config.LidlessConfig;
import dev.lidless.memory.ContainerMemory;

import java.nio.file.Path;

public final class LidlessClient {
    public static final String MOD_ID = "lidless";

    private static LidlessConfig config = new LidlessConfig();
    private static Path configPath;

    private LidlessClient() {
    }

    public static void init(Path configDir) {
        configPath = configDir.resolve(LidlessConfig.FILE_NAME);
        config = LidlessConfig.load(configPath);
        ContainerMemory.init(configDir);
    }

    public static LidlessConfig config() {
        return config;
    }

    public static void saveConfig() {
        config.saveQuietly(configPath);
    }
}
