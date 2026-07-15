package com.fluxcraft.MiaoMenu.security;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class RateLimiterTest {
    @Test
    void blocksAfterThreshold() {
        RateLimiter limiter = new RateLimiter(Duration.ofSeconds(1), 2);
        UUID uuid = UUID.randomUUID();

        assertTrue(limiter.allow(uuid));
        assertTrue(limiter.allow(uuid));
        assertFalse(limiter.allow(uuid));
    }

    @Test
    void resetsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(Duration.ofMillis(80), 1);
        UUID uuid = UUID.randomUUID();

        assertTrue(limiter.allow(uuid));
        assertFalse(limiter.allow(uuid));

        Thread.sleep(120);

        assertTrue(limiter.allow(uuid));
        assertFalse(limiter.allow(uuid));
    }

    @Test
    void clearRemovesPlayerWindow() throws Exception {
        RateLimiter limiter = new RateLimiter(Duration.ofSeconds(1), 2);
        UUID uuid = UUID.randomUUID();

        limiter.allow(uuid);
        limiter.allow(uuid);
        assertEquals(1, windowSize(limiter));

        limiter.clear(uuid);
        assertEquals(0, windowSize(limiter));

        // 重新访问应被视为首次进入新窗口
        assertTrue(limiter.allow(uuid));
    }

    @Test
    void clearAllEmptiesWindows() throws Exception {
        RateLimiter limiter = new RateLimiter(Duration.ofSeconds(1), 2);
        limiter.allow(UUID.randomUUID());
        limiter.allow(UUID.randomUUID());

        assertEquals(2, windowSize(limiter));

        limiter.clearAll();
        assertEquals(0, windowSize(limiter));
    }

    @Test
    void periodicCleanupRemovesExpiredWindows() throws Exception {
        RateLimiter limiter = new RateLimiter(Duration.ofMillis(80), 5);

        // 写入两条即将过期的窗口
        UUID expired1 = UUID.randomUUID();
        UUID expired2 = UUID.randomUUID();
        limiter.allow(expired1);
        limiter.allow(expired2);

        Thread.sleep(120);

        // 用同一个活跃 UUID 推动访问计数到达下一个清扫触发点，触发过期回收
        UUID fresh = UUID.randomUUID();
        int needed = 256 - (accessCount(limiter).get() % 256);
        for (int i = 0; i < needed; i++) {
            limiter.allow(fresh);
        }

        Map<UUID, ?> windows = windowsMap(limiter);
        assertFalse(windows.containsKey(expired1));
        assertFalse(windows.containsKey(expired2));
        assertTrue(windows.containsKey(fresh));
    }

    private static int windowSize(RateLimiter limiter) throws Exception {
        return windowsMap(limiter).size();
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, ?> windowsMap(RateLimiter limiter) throws Exception {
        Field field = RateLimiter.class.getDeclaredField("windows");
        field.setAccessible(true);
        return (Map<UUID, ?>) field.get(limiter);
    }

    private static AtomicInteger accessCount(RateLimiter limiter) throws Exception {
        Field field = RateLimiter.class.getDeclaredField("accessCount");
        field.setAccessible(true);
        return (AtomicInteger) field.get(limiter);
    }
}
