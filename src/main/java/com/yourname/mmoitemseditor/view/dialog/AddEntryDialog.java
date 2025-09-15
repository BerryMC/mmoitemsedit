package com.yourname.mmoitemseditor.view.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.List;
import java.util.ResourceBundle;

public class AddEntryDialog extends Dialog<Pair<String, Integer>> {

    public AddEntryDialog(String title, String header, String prompt, String label, List<String> choices, ResourceBundle translations) {
        setTitle(title);
        setHeaderText(header);

        ButtonType okButtonType = new ButtonType(translations.getString("confirm"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ObservableList<String> observableChoices = (choices == null) ? FXCollections.observableArrayList() : FXCollections.observableArrayList(choices);
        ComboBox<String> comboBox = new ComboBox<>(observableChoices);
        comboBox.setPromptText(prompt);
        Spinner<Integer> levelSpinner = new Spinner<>(1, 255, 1);
        levelSpinner.setEditable(true);

        grid.add(new Label(label + ":"), 0, 0);
        grid.add(comboBox, 1, 0);
        grid.add(new Label(translations.getString("label.level") + ":"), 0, 1);
        grid.add(levelSpinner, 1, 1);

        getDialogPane().setContent(grid);

        Node okButton = getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> okButton.setDisable(newVal == null || newVal.trim().isEmpty()));

        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new Pair<>(comboBox.getValue(), levelSpinner.getValue());
            }
            return null;
        });
    }
}
