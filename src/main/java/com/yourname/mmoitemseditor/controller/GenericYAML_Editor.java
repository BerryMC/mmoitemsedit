package com.yourname.mmoitemseditor.controller;

import com.yourname.mmoitemseditor.service.YAMLDataService;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

public class GenericYAML_Editor implements ComplexAttributeEditor {

    private final TextArea textArea;
    private final YAMLDataService dataService;

    public GenericYAML_Editor(YAMLDataService dataService, String promptText) {
        this.dataService = dataService;
        this.textArea = new TextArea();
        this.textArea.setPromptText(promptText);
        this.textArea.setPrefHeight(120);
    }

    @Override
    public void loadData(Object data) {
        if (data != null) {
            textArea.setText(dataService.getYaml().dump(data));
        } else {
            textArea.clear();
        }
    }

    @Override
    public Object saveData() {
        String text = textArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return dataService.getYaml().load(text);
        } catch (Exception e) {
            // The main controller will show an alert.
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Node getRoot() {
        return textArea;
    }

    @Override
    public void setUnsavedChangesListener(ChangeListener<Object> listener) {
        textArea.textProperty().addListener(listener);
    }

    @Override
    public void clear() {
        textArea.clear();
    }
}
