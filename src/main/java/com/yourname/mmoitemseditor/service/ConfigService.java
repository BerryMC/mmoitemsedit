package com.yourname.mmoitemseditor.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads and manages configuration data like materials, enchantments, etc., for different versions.
 */
public class ConfigService {

    private final Map<String, List<String>> configLists = new HashMap<>();
    private String currentVersion;

    public void loadVersion(String version) throws DataAccessException {
        this.currentVersion = version;
        configLists.clear();
        
        // In the future, we can add more versions here.
        if ("1.12.2".equals(version)) {
            loadConfigList("materials");
            loadConfigList("enchantments");
            loadConfigList("potion_effects");
            loadConfigList("ability_modes");
            loadConfigList("ability_types");
        } else {
            throw new DataAccessException("Configuration for version '" + version + "' not found.", null);
        }
    }

    private void loadConfigList(String listName) throws DataAccessException {
        String versionPath = currentVersion.replace('.', '_'); // Convert "1.12.2" to "1_12_2"
        String resourcePath = String.format("configs/%s/%s.txt", versionPath, listName);
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) { // Used Thread.currentThread().getContextClassLoader()
            if (is == null) {
                throw new DataAccessException("Resource not found: " + resourcePath, null);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<String> values = reader.lines()
                                            .map(String::trim)
                                            .filter(line -> !line.isEmpty())
                                            .sorted()
                                            .collect(Collectors.toList());
                configLists.put(listName, values);
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to load config list: " + listName, e);
        }
    }

    public List<String> getMaterials() {
        return configLists.getOrDefault("materials", Collections.emptyList());
    }

    public List<String> getEnchantments() {
        return configLists.getOrDefault("enchantments", Collections.emptyList());
    }

    public List<String> getPotionEffects() {
        return configLists.getOrDefault("potion_effects", Collections.emptyList());
    }

    public List<String> getAbilityModes() {
        return configLists.getOrDefault("ability_modes", Collections.emptyList());
    }

    public List<String> getAbilityTypes() {
        return configLists.getOrDefault("ability_types", Collections.emptyList());
    }
}
