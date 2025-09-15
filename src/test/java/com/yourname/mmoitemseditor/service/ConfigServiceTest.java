package com.yourname.mmoitemseditor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ConfigServiceTest {

    private ConfigService configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigService();
    }

    @Test
    void testLoadValidVersion() {
        assertDoesNotThrow(() -> configService.loadVersion("1.12.2"));
        
        List<String> materials = configService.getMaterials();
        assertNotNull(materials);
        assertFalse(materials.isEmpty());
        assertTrue(materials.contains("STONE"));

        List<String> enchants = configService.getEnchantments();
        assertNotNull(enchants);
        assertFalse(enchants.isEmpty());
        assertTrue(enchants.contains("SHARPNESS"));
    }

    @Test
    void testLoadInvalidVersion() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            configService.loadVersion("invalid-version");
        });
        assertEquals("Configuration for version 'invalid-version' not found.", exception.getMessage());
    }

    @Test
    void testGettersBeforeLoading() {
        assertTrue(configService.getMaterials().isEmpty());
        assertTrue(configService.getEnchantments().isEmpty());
        assertTrue(configService.getPotionEffects().isEmpty());
    }
}
