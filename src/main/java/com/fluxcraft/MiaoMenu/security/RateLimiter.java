package com.fluxcraft.MiaoMenu.security;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {
    private final long windowMillis;
    private final int maxEvents;
    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(Duration window, int maxEvents) {
        this.windowMillis = window.toMillis();
        this.maxEvents = maxEvents;
    }

    public boolean allow(UUID uuid) {
        long now = System.currentTimeMillis();
        Window current = windows.get(uuid);
        if (current == null || now - current.windowStart() >= windowMillis) {
            windows.put(uuid, new Window(now, 1));
            return true;
        }
        if (current.count() >= maxEvents) {
            return false;
        }
        windows.put(uuid, new Window(current.windowStart(), current.count() + 1));
        return true;
    }

    public void clearAll() {
        windows.clear();
    }

    private record Window(long windowStart, int count) {
    }
}
