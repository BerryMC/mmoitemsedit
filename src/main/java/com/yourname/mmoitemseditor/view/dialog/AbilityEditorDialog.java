package com.yourname.mmoitemseditor.view.dialog;

import com.yourname.mmoitemseditor.model.AbilityEntry;
import com.yourname.mmoitemseditor.model.ModifierEntry;
import com.yourname.mmoitemseditor.service.ConfigService;
import com.yourname.mmoitemseditor.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AbilityEditorDialog extends Dialog<AbilityEntry> {

    private final AbilityEntry ability;
    private final ResourceBundle translations;

    @SuppressWarnings("unchecked")
    public AbilityEditorDialog(AbilityEntry existingAbility, ConfigService configService, ResourceBundle translations) {
        this.ability = (existingAbility == null)
                ? new AbilityEntry("", "", "", new LinkedHashMap<>())
                : existingAbility;
        this.translations = translations;

        setTitle(existingAbility == null ? getTranslation("dialog.add.ability.title") : getTranslation("dialog.edit.ability.title"));
        setHeaderText(getTranslation("dialog.ability.header"));

        ButtonType okButtonType = new ButtonType(getTranslation("confirm"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20, 20, 10, 10));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField idField = new TextField(ability.getId());
        idField.setPromptText(getTranslation("dialog.ability.id.prompt"));
        ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(configService.getAbilityTypes()));
        typeComboBox.setEditable(true);
        typeComboBox.setPromptText(getTranslation("dialog.ability.type.prompt"));
        typeComboBox.setValue(ability.getType());
        ComboBox<String> modeComboBox = new ComboBox<>(FXCollections.observableArrayList(configService.getAbilityModes()));
        modeComboBox.setPromptText(getTranslation("dialog.ability.mode.prompt"));
        modeComboBox.setValue(ability.getMode());

        grid.add(new Label(getTranslation("label.ability.id")), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label(getTranslation("label.ability.type")), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label(getTranslation("label.ability.mode")), 0, 2);
        grid.add(modeComboBox, 1, 2);

        // --- Modifier Table ---
        Label modifierLabel = new Label(getTranslation("label.ability.modifiers"));
        TableView<ModifierEntry> modifierTable = new TableView<>();
        modifierTable.setEditable(true);
        modifierTable.setPrefHeight(150);

        TableColumn<ModifierEntry, String> keyCol = new TableColumn<>(getTranslation("label.ability.modifier"));
        keyCol.setCellValueFactory(new PropertyValueFactory<>("modifier"));
        keyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        keyCol.setOnEditCommit(event -> event.getRowValue().setModifier(event.getNewValue()));

        TableColumn<ModifierEntry, String> valueCol = new TableColumn<>(getTranslation("label.value"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));

        modifierTable.getColumns().addAll(keyCol, valueCol);
        modifierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<ModifierEntry> modifierData = FXCollections.observableArrayList();
        if (ability.getModifiers() != null) {
            ability.getModifiers().forEach((key, value) -> {
                if (!key.equals("type") && !key.equals("mode")) {
                    modifierData.add(new ModifierEntry(key, value.toString()));
                }
            });
        }
        modifierTable.setItems(modifierData);

        HBox modifierButtons = new HBox(5);
        Button addModifierButton = new Button(getTranslation("add"));
        addModifierButton.setOnAction(e -> {
            modifierData.add(new ModifierEntry("new-modifier", "value"));
            modifierTable.edit(modifierData.size() - 1, keyCol);
        });
        Button removeModifierButton = new Button(getTranslation("remove"));
        removeModifierButton.setOnAction(e -> {
            ModifierEntry selected = modifierTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                modifierData.remove(selected);
            }
        });
        modifierButtons.getChildren().addAll(addModifierButton, removeModifierButton);
        // --- End of Modifier Table ---

        mainLayout.getChildren().addAll(grid, modifierLabel, modifierTable, modifierButtons);
        VBox.setVgrow(modifierTable, Priority.ALWAYS);

        getDialogPane().setContent(mainLayout);
        getDialogPane().setPrefWidth(450);

        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String id = idField.getText();
                String type = typeComboBox.getValue();
                String mode = modeComboBox.getValue();

                if (id == null || id.trim().isEmpty() || type == null || mode == null) {
                    return null;
                }

                Map<String, Object> modifiers = new LinkedHashMap<>();
                modifiers.put("type", type);
                modifiers.put("mode", mode);
                for (ModifierEntry entry : modifierData) {
                    if (entry.getModifier() != null && !entry.getModifier().trim().isEmpty()) {
                        modifiers.put(entry.getModifier(), Utils.smartConvert(entry.getValue()));
                    }
                }

                ability.setId(id);
                ability.setType(type);
                ability.setMode(mode);
                ability.setModifiers(modifiers);
                return ability;
            }
            return null;
        });
    }

    private String getTranslation(String key) {
        return translations.getString(key);
    }
}
