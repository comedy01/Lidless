package dev.lidless.memory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerMemory {
    private static final Logger LOGGER = LoggerFactory.getLogger("lidless");
    private static final Gson GSON = new GsonBuilder().create();

    private static Path directory;
    private static String worldId;
    private static final Map<String, Remembered> entries = new HashMap<>();

    private ContainerMemory() {
    }

    public static void init(Path configDir) {
        directory = configDir.resolve("lidless").resolve("memory");
    }

    public static Remembered get(Minecraft mc, String key) {
        if (!sync(mc)) {
            return null;
        }
        return entries.get(key);
    }

    public static void put(Minecraft mc, List<String> keys, Remembered entry) {
        if (keys.isEmpty() || !sync(mc)) {
            return;
        }
        for (String key : keys) {
            entries.put(key, entry);
        }
        save(mc);
    }

    public static void forget(Minecraft mc, String key) {
        if (sync(mc) && entries.remove(key) != null) {
            save(mc);
        }
    }

    public static String worldId(Minecraft mc) {
        if (mc.level == null || mc.hasSingleplayerServer()) {
            return null;
        }
        ServerData server = mc.getCurrentServer();
        String address = server == null || server.ip == null || server.ip.isBlank() ? "unknown" : server.ip;
        return FileNames.safe(address);
    }

    private static boolean sync(Minecraft mc) {
        String id = worldId(mc);
        if (id == null) {
            return false;
        }
        if (!id.equals(worldId)) {
            worldId = id;
            entries.clear();
            load(mc);
        }
        return true;
    }

    private static Path file() {
        return directory == null || worldId == null ? null : directory.resolve(worldId + ".json");
    }

    private static void load(Minecraft mc) {
        Path file = file();
        if (file == null || !Files.isRegularFile(file) || mc.level == null) {
            return;
        }
        DynamicOps<JsonElement> ops = ops(mc.level.registryAccess());
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                ItemContainerContents contents = ItemContainerContents.CODEC.parse(ops, value.get("items"))
                        .result().orElse(ItemContainerContents.EMPTY);
                int size = value.has("size") ? value.get("size").getAsInt() : 0;
                NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
                contents.copyInto(items);
                Component title = value.has("title")
                        ? ComponentSerialization.CODEC.parse(ops, value.get("title")).result().orElse(null)
                        : null;
                long seen = value.has("seen") ? value.get("seen").getAsLong() : 0L;
                entries.put(entry.getKey(), new Remembered(title, items, seen));
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read container memory {}: {}", file, e.toString());
        }
    }

    private static void save(Minecraft mc) {
        Path file = file();
        if (file == null || mc.level == null) {
            return;
        }
        DynamicOps<JsonElement> ops = ops(mc.level.registryAccess());
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Remembered> entry : entries.entrySet()) {
            Remembered remembered = entry.getValue();
            JsonObject value = new JsonObject();
            value.addProperty("seen", remembered.seenAt());
            value.addProperty("size", remembered.items().size());
            ItemContainerContents.CODEC.encodeStart(ops, ItemContainerContents.fromItems(remembered.items()))
                    .result().ifPresent(json -> value.add("items", json));
            if (remembered.title() != null) {
                ComponentSerialization.CODEC.encodeStart(ops, remembered.title())
                        .result().ifPresent(json -> value.add("title", json));
            }
            root.add(entry.getKey(), value);
        }
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not save container memory {}: {}", file, e.toString());
        }
    }

    private static DynamicOps<JsonElement> ops(HolderLookup.Provider registries) {
        return registries.createSerializationContext(JsonOps.INSTANCE);
    }
}
