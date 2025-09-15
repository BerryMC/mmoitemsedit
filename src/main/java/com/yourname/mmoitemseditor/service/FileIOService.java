package com.yourname.mmoitemseditor.service;

import com.yourname.mmoitemseditor.model.MMOItemEntry;
import com.yourname.mmoitemseditor.view.builder.TreeViewBuilder;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileIOService {

    private final YAMLDataService dataService;

    public FileIOService(YAMLDataService dataService) {
        this.dataService = dataService;
    }

    public Task<TreeItem<Object>> createLoadDirectoryTask(File directory) {
        return new Task<>() {
            @Override
            protected TreeItem<Object> call() throws Exception {
                updateMessage("Loading files..."); // A default message
                dataService.loadAllYmlFiles(directory);

                TreeItem<Object> rootItem = new TreeItem<>(directory);
                rootItem.setExpanded(true);
                TreeViewBuilder.populateTreeView(rootItem, directory, dataService);
                return rootItem;
            }
        };
    }

    public void saveItem(MMOItemEntry item, Map<String, Object> itemData) throws DataAccessException {
        if (item == null || itemData == null) {
            throw new IllegalArgumentException("Item and item data cannot be null.");
        }

        Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(item.getParentFile());
        if (itemsInFile != null) {
            itemsInFile.put(item.getItemId(), itemData);
            dataService.saveToFile(item.getParentFile());
        }
    }

    public void saveAll() throws DataAccessException {
        List<DataAccessException> exceptions = new ArrayList<>();
        for (File file : dataService.getLoadedFilesData().keySet()) {
            try {
                dataService.saveToFile(file);
            } catch (DataAccessException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            // In a real application, you might want to aggregate these exceptions
            throw new DataAccessException("Failed to save one or more files.", exceptions.get(0));
        }
    }

    public void saveItemAs(String itemId, Map<String, Object> itemData, File targetFile) throws DataAccessException {
        if (itemId == null || itemId.trim().isEmpty() || itemData == null || targetFile == null) {
            throw new IllegalArgumentException("Item ID, item data, and target file cannot be null or empty.");
        }

        if (!dataService.getLoadedFilesData().containsKey(targetFile)) {
            if (targetFile.exists()) {
                dataService.loadSingleYmlFile(targetFile);
            } else {
                dataService.getLoadedFilesData().put(targetFile, new LinkedHashMap<>());
            }
        }

        Map<String, Map<String, Object>> targetFileData = dataService.getLoadedFilesData().get(targetFile);
        targetFileData.put(itemId, itemData);

        dataService.saveToFile(targetFile);
    }
}
