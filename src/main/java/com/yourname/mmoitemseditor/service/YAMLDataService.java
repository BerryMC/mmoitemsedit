package com.yourname.mmoitemseditor.service;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class YAMLDataService {

    private final Map<File, Map<String, Map<String, Object>>> loadedFilesData = new LinkedHashMap<>();
    private final Yaml yaml;

    public YAMLDataService() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setAllowUnicode(true);
        this.yaml = new Yaml(options);
    }

    public Map<File, Map<String, Map<String, Object>>> getLoadedFilesData() {
        return loadedFilesData;
    }

    public void loadAllYmlFiles(File dir) throws DataAccessException {
        loadedFilesData.clear();
        loadFilesRecursively(dir);
    }

    private void loadFilesRecursively(File dir) throws DataAccessException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFilesRecursively(file);
            } else if (file.getName().toLowerCase().endsWith(".yml")) {
                loadSingleYmlFile(file);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadSingleYmlFile(File file) throws DataAccessException {
        if (file == null || !file.exists() || !file.getName().toLowerCase().endsWith(".yml")) return;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Object obj = yaml.load(reader);
            if (obj == null) {
                loadedFilesData.put(file, new LinkedHashMap<>());
                return;
            }
            if (obj instanceof Map) {
                Map<String, Map<String, Object>> sanitizedMap = new LinkedHashMap<>();
                ((Map<?, ?>) obj).forEach((key, value) -> {
                    if (key instanceof String && value instanceof Map) {
                        sanitizedMap.put((String) key, (Map<String, Object>) value);
                    }
                });
                loadedFilesData.put(file, sanitizedMap);
            } else {
                System.err.println("Warning: File " + file.getName() + " does not have a Map at its root, ignoring.");
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to read file " + file.getName(), e);
        }
    }

    public void saveToFile(File fileToSave) throws DataAccessException {
        Map<String, Map<String, Object>> data = loadedFilesData.get(fileToSave);
        if (data == null) return;
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {
            if (!data.isEmpty()) {
                yaml.dump(data, writer);
            } else {
                writer.write(""); // Clear the file if there's no data
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to save file " + fileToSave.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> deepCopyMap(Map<String, Object> original) {
        String dumped = yaml.dump(original);
        return (Map<String, Object>) yaml.load(dumped);
    }
    
    public Yaml getYaml() {
        return yaml;
    }
}