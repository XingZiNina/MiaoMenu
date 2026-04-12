package com.fluxcraft.MiaoMenu.security;

import java.time.Duration;
import java.util.UUID;

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
}
