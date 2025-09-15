package com.yourname.mmoitemseditor.view.builder;

import com.yourname.mmoitemseditor.model.AbilityEntry;
import com.yourname.mmoitemseditor.model.EnchantmentEntry;
import com.yourname.mmoitemseditor.service.ConfigService;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AttributePaneBuilder {

    private final ConfigService configService;
    private final ResourceBundle translations;
    private final Map<String, Node> attributeControls;
    private final ChangeListener<Object> unsavedChangesListener;

    // FXML Panes
    private final GridPane basicAttributesPane;
    private final GridPane statsAttributesPane;
    private final GridPane advancedAttributesPane;
    private final VBox loreEnchantsPane;
    private final VBox complexAttributesPane;

    public AttributePaneBuilder(ConfigService configService, ResourceBundle translations, Map<String, Node> attributeControls, ChangeListener<Object> unsavedChangesListener, GridPane basicAttributesPane, GridPane statsAttributesPane, GridPane advancedAttributesPane, VBox loreEnchantsPane, VBox complexAttributesPane) {
        this.configService = configService;
        this.translations = translations;
        this.attributeControls = attributeControls;
        this.unsavedChangesListener = unsavedChangesListener;
        this.basicAttributesPane = basicAttributesPane;
        this.statsAttributesPane = statsAttributesPane;
        this.advancedAttributesPane = advancedAttributesPane;
        this.loreEnchantsPane = loreEnchantsPane;
        this.complexAttributesPane = complexAttributesPane;
    }

    public void build() {
        buildBasicAttributes();
        buildStatsAttributes();
        buildAdvancedAttributes();
        buildLoreAndEnchantments();
        buildComplexAttributes();
    }

    private void buildBasicAttributes() {
        ComboBox<String> materialComboBox = new ComboBox<>(FXCollections.observableArrayList(configService.getMaterials()));
        materialComboBox.setEditable(true);
        addValidation(materialComboBox, configService.getMaterials());
        addAttributeControl(basicAttributesPane, "material", materialComboBox, 0);
        addAttributeControl(basicAttributesPane, "name", new TextField(), 1);
        addAttributeControl(basicAttributesPane, "tier", new TextField(), 2);
    }

    private void buildStatsAttributes() {
        // Define attribute lists locally or pass them in
        List<String> STATS_ATTRIBUTES = List.of("attack-damage", "attack-speed", "critical-strike-chance", "critical-strike-power", "max-health", "health-regeneration", "armor", "armor-toughness", "movement-speed", "knockback-resistance", "pve-damage", "pvp-damage", "magic-damage", "skill-damage", "damage-reduction", "fall-damage-reduction", "required-level", "required-class");

        int rowIndex = 0;
        for (String attr : STATS_ATTRIBUTES) {
            Node control;
            if (attr.equals("required-level")) {
                Spinner<Integer> spinner = new Spinner<>(0, 100, 0);
                spinner.setEditable(true);
                control = spinner;
            } else if (attr.equals("required-class")) {
                control = new TextField(); // This remains a TextField
            } else {
                Spinner<Double> spinner = new Spinner<>(0.0, 1000.0, 0.0, 0.1);
                spinner.setEditable(true);
                control = spinner;
            }
            addAttributeControl(statsAttributesPane, attr, control, rowIndex++);
        }
    }

    private void buildAdvancedAttributes() {
        List<String> ADVANCED_ATTRIBUTES_TEXT = List.of("max-durability", "consume-cooldown", "gem-sockets", "soulbound-level", "soulbinding-chance", "soulbound-break-chance", "potion-color", "skull-texture");
        List<String> ADVANCED_ATTRIBUTES_BOOLEAN = List.of("unbreakable", "two-handed", "hide-enchants", "disable-interaction", "disable-repairing", "disable-enchanting", "disable-crafting", "disable-smelting", "can-identify", "can-deconstruct");

        int rowIndex = 0;
        for (String attr : ADVANCED_ATTRIBUTES_TEXT) {
            addAttributeControl(advancedAttributesPane, attr, new TextField(), rowIndex++);
        }
        for (String attr : ADVANCED_ATTRIBUTES_BOOLEAN) {
            addAttributeControl(advancedAttributesPane, attr, new CheckBox(), rowIndex++);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildLoreAndEnchantments() {
        // Lore
        addComplexAttributeControl(loreEnchantsPane, "lore");

        // Enchantments
        VBox enchantsBox = new VBox(5.0);
        enchantsBox.getChildren().add(new Label(String.format(getTranslation("attribute.label.format"), getTranslation("label.enchants"), "enchants")));
        TableView<EnchantmentEntry> enchantmentsTable = new TableView<>();
        TableColumn<EnchantmentEntry, String> nameCol = new TableColumn<>(getTranslation("label.enchantment"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<EnchantmentEntry, Integer> levelCol = new TableColumn<>(getTranslation("label.level"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        enchantmentsTable.getColumns().addAll(nameCol, levelCol);
        enchantmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        enchantsBox.getChildren().add(enchantmentsTable);

        // Buttons are handled in the main controller, so we just add the table here
        loreEnchantsPane.getChildren().add(enchantsBox);
        attributeControls.put("enchants_table", enchantmentsTable);
    }

    private void buildComplexAttributes() {
        buildAbilityEditor();

        List<String> COMPLEX_ATTRIBUTES = List.of("effects", "restore", "element", "craft", "advanced-craft", "shield-pattern", "commands");
        for (String attr : COMPLEX_ATTRIBUTES) {
            addComplexAttributeControl(complexAttributesPane, attr);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildAbilityEditor() {
        VBox abilityBox = new VBox(5.0);
        abilityBox.getChildren().add(new Label(String.format(getTranslation("attribute.label.format"), getTranslation("label.abilities"), "ability")));

        TableView<AbilityEntry> abilityTable = new TableView<>();
        TableColumn<AbilityEntry, String> idCol = new TableColumn<>(getTranslation("label.ability.id"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<AbilityEntry, String> typeCol = new TableColumn<>(getTranslation("label.ability.type"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<AbilityEntry, String> modeCol = new TableColumn<>(getTranslation("label.ability.mode"));
        modeCol.setCellValueFactory(new PropertyValueFactory<>("mode"));
        TableColumn<AbilityEntry, String> summaryCol = new TableColumn<>(getTranslation("label.ability.summary"));
        summaryCol.setCellValueFactory(new PropertyValueFactory<>("summary"));

        abilityTable.getColumns().addAll(idCol, typeCol, modeCol, summaryCol);
        abilityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        abilityBox.getChildren().add(abilityTable);

        // Buttons are handled in the main controller
        complexAttributesPane.getChildren().add(abilityBox);
        attributeControls.put("ability_table", abilityTable);
    }

    private void addAttributeControl(GridPane pane, String attributeName, Node control, int rowIndex) {
        String labelText = String.format(getTranslation("attribute.label.format"), getTranslation(attributeName), attributeName);
        pane.add(new Label(labelText), 0, rowIndex);
        pane.add(control, 1, rowIndex);
        attributeControls.put(attributeName, control);
        attachListener(control);
    }

    private void addComplexAttributeControl(VBox pane, String attributeName) {
        pane.getChildren().add(new Label(String.format(getTranslation("attribute.label.format"), getTranslation(attributeName), attributeName)));
        TextArea textArea = new TextArea();
        textArea.setPromptText(String.format(getTranslation("attribute.yaml.prompt"), attributeName));
        textArea.setPrefHeight(120);
        attachListener(textArea);
        pane.getChildren().add(textArea);
        attributeControls.put(attributeName, textArea);
    }

    private void addValidation(ComboBox<String> comboBox, List<String> validValues) {
        String errorStyle = "-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 2px;";
        String validStyle = ""; // Or your default style

        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !validValues.contains(newVal.toUpperCase())) {
                comboBox.setStyle(errorStyle);
            } else {
                comboBox.setStyle(validStyle);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void attachListener(Node control) {
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).textProperty().addListener(unsavedChangesListener);
        } else if (control instanceof CheckBox) {
            ((CheckBox) control).selectedProperty().addListener(unsavedChangesListener);
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).valueProperty().addListener(unsavedChangesListener);
        } else if (control instanceof Spinner) {
            ((Spinner<?>) control).valueProperty().addListener(unsavedChangesListener);
        }
    }

    private String getTranslation(String key) {
        try {
            return translations.getString(key);
        } catch (Exception e) {
            System.err.println("Missing translation key: " + key);
            return "!" + key + "!";
        }
    }
}