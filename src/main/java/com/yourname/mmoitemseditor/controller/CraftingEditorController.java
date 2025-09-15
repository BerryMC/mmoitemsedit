package com.yourname.mmoitemseditor.controller;

import com.yourname.mmoitemseditor.model.CraftingIngredient;
import com.yourname.mmoitemseditor.service.ConfigService;
import com.yourname.mmoitemseditor.service.YAMLDataService;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CraftingEditorController implements ComplexAttributeEditor {

    @FXML private VBox root;
    @FXML private TextField outputAmountField;
    @FXML private TableView<CraftingIngredient> shapelessIngredientsTable;
    @FXML private TableColumn<CraftingIngredient, String> shapelessMaterialCol;
    @FXML private TableColumn<CraftingIngredient, Integer> shapelessAmountCol;
    @FXML private GridPane craftingGrid;
    @FXML private GridPane ingredientKeysPane;

    private ConfigService configService;
    private YAMLDataService yamlDataService;
    private ChangeListener<Object> unsavedChangesListener;

    public void init(ConfigService configService, YAMLDataService yamlDataService) {
        this.configService = configService;
        this.yamlDataService = yamlDataService;
    }

    @FXML
    private void initialize() {
        // Initialization logic will go here
    }

    @Override
    public void loadData(Object data) {
        // TODO: Implement logic to parse the recipe map and populate the UI
        System.out.println("Loading crafting data...");
    }

    @Override
    public Object saveData() {
        // TODO: Implement logic to collect data from UI and build the YAML map
        System.out.println("Saving crafting data...");
        return new java.util.LinkedHashMap<>(); // Return empty map for now
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void setUnsavedChangesListener(ChangeListener<Object> listener) {
        this.unsavedChangesListener = listener;
        // TODO: Attach this listener to all relevant controls in this editor
        outputAmountField.textProperty().addListener(listener);
    }

    @Override
    public void clear() {
        outputAmountField.setText("1");
        shapelessIngredientsTable.getItems().clear();
        craftingGrid.getChildren().clear();
        ingredientKeysPane.getChildren().clear();
    }

    @FXML
    private void handleAddShapeless() {
        // TODO: Add a new ingredient to the shapeless table
    }

    @FXML
    private void handleRemoveShapeless() {
        // TODO: Logic to remove the selected ingredient
    }
}