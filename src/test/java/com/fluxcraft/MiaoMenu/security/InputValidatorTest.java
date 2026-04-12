package com.fluxcraft.MiaoMenu.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class InputValidatorTest {
    @Test
    void acceptsSafeMenuName() {
        assertTrue(InputValidator.isSafeMenuName("menu.test_1"));
    }

    @Test
    void rejectsUnsafeCommandContent() {
        assertFalse(InputValidator.isSafeCommandContent("say hi && op me"));
    }
}
