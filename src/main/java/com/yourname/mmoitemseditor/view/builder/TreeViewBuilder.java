package com.yourname.mmoitemseditor.view.builder;

import com.yourname.mmoitemseditor.model.MMOItemEntry;
import com.yourname.mmoitemseditor.service.YAMLDataService;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public final class TreeViewBuilder {

    private TreeViewBuilder() {}

    public static void populateTreeView(TreeItem<Object> parent, File dir, YAMLDataService dataService) {
        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            if (file.isDirectory()) {
                TreeItem<Object> item = new TreeItem<>(file);
                parent.getChildren().add(item);
                populateTreeView(item, file, dataService); // Recursive call
            } else if (file.getName().toLowerCase().endsWith(".yml")) {
                TreeItem<Object> fileItem = new TreeItem<>(file);
                parent.getChildren().add(fileItem);
                Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(file);
                if (itemsInFile != null) {
                    itemsInFile.keySet().stream().sorted().forEach(itemId -> {
                        fileItem.getChildren().add(new TreeItem<>(new MMOItemEntry(itemId, file)));
                    });
                }
            }
        }
    }
}
