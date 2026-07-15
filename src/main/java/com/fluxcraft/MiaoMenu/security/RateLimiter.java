package com.fluxcraft.MiaoMenu.security;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class RateLimiter {
    // 每隔这么多次访问触发一次过期窗口扫描，避免无界增长的同时不增加每次调用成本
    private static final int CLEANUP_INTERVAL = 256;

    private final long windowMillis;
    private final int maxEvents;
    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();
    private final AtomicInteger accessCount = new AtomicInteger();

    public RateLimiter(Duration window, int maxEvents) {
        this.windowMillis = window.toMillis();
        this.maxEvents = maxEvents;
    }

    public boolean allow(UUID uuid) {
        long now = System.currentTimeMillis();
        maybeCleanup(now);

        AtomicBoolean allowed = new AtomicBoolean(false);
        windows.compute(uuid, (_, current) -> {
            if (current == null || now - current.windowStart() >= windowMillis) {
                allowed.set(true);
                return new Window(now, 1);
            }
            if (current.count() >= maxEvents) {
                return current;
            }
            allowed.set(true);
            return new Window(current.windowStart(), current.count() + 1);
        });
        return allowed.get();
    }

    // 移除指定玩家的限流窗口，供玩家离线事件调用
    public void clear(UUID uuid) {
        windows.remove(uuid);
    }

    public void clearAll() {
        windows.clear();
    }

    // 机会式回收：仅在固定访问间隔触发，删除所有已过期窗口。
    // 使用 remove(key, value) 按值比对删除，避免误删并发更新后的新窗口。
    private void maybeCleanup(long now) {
        if (accessCount.incrementAndGet() % CLEANUP_INTERVAL != 0) {
            return;
        }
        windows.forEach((uuid, window) -> {
            if (now - window.windowStart() >= windowMillis) {
                windows.remove(uuid, window);
            }
        });
    }

    private record Window(long windowStart, int count) {
    }
}
