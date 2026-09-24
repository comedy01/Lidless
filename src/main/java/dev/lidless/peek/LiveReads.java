package dev.lidless.peek;

import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class LiveReads<T> {
    private static final int MAX_ENTRIES = 64;

    private final Map<String, Optional<T>> results = new ConcurrentHashMap<>();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private MinecraftServer server;

    T fetch(MinecraftServer current, String key, Function<MinecraftServer, T> read) {
        if (current != server) {
            server = current;
            results.clear();
        }
        if (pending.add(key)) {
            if (results.size() > MAX_ENTRIES) {
                results.clear();
            }
            current.execute(() -> {
                try {
                    results.put(key, Optional.ofNullable(read.apply(current)));
                } catch (RuntimeException e) {
                    results.put(key, Optional.empty());
                } finally {
                    pending.remove(key);
                }
            });
        }
        Optional<T> result = results.get(key);
        return result == null ? null : result.orElse(null);
    }
}
