package com.yourname.mmoitemseditor;

import com.yourname.mmoitemseditor.model.AbilityEntry;
import com.yourname.mmoitemseditor.model.EnchantmentEntry;
import com.yourname.mmoitemseditor.model.MMOItemEntry;
import com.yourname.mmoitemseditor.model.PotionEffectEntry;
import com.yourname.mmoitemseditor.service.ConfigService;
import com.yourname.mmoitemseditor.service.DataAccessException;
import com.yourname.mmoitemseditor.service.FileIOService;
import com.yourname.mmoitemseditor.service.YAMLDataService;
import com.yourname.mmoitemseditor.util.Utils;
import com.yourname.mmoitemseditor.view.builder.AttributePaneBuilder;
import com.yourname.mmoitemseditor.view.builder.TreeViewBuilder;
import com.yourname.mmoitemseditor.view.dialog.AbilityEditorDialog;
import com.yourname.mmoitemseditor.view.dialog.AddEntryDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditorController {

    //<editor-fold desc="FXML Declarations">
    @FXML private MenuBar menuBar;
    @FXML private ComboBox<String> versionComboBox;
    @FXML private TextField searchField;
    @FXML private TreeView<Object> fileTreeView;
    @FXML private Label statusLabel;
    @FXML private Label editingItemIdLabel;
    @FXML private VBox welcomePane;
    @FXML private VBox editorPane;
    @FXML private GridPane basicAttributesPane;
    @FXML private GridPane statsAttributesPane;
    @FXML private GridPane advancedAttributesPane;
    @FXML private VBox loreEnchantsPane;
    @FXML private VBox complexAttributesPane;
    @FXML private Button newItemButton;
    @FXML private Button duplicateItemButton;
    @FXML private Button deleteItemButton;
    @FXML private Button saveChangesButton;
    @FXML private TableView<PotionEffectEntry> permEffectsTable;
    @FXML private TableColumn<PotionEffectEntry, String> permEffectNameCol;
    @FXML private TableColumn<PotionEffectEntry, Integer> permEffectLevelCol;
    @FXML private Button addEnchantButton;
    @FXML private Button removeEnchantButton;
    @FXML private Button addAbilityButton;
    @FXML private Button editAbilityButton;
    @FXML private Button removeAbilityButton;

    private ResourceBundle translations;
    //</editor-fold>

    private final Map<String, Node> attributeControls = new HashMap<>();
    private final YAMLDataService dataService;
    private final ConfigService configService;
    private final FileIOService fileIOService;

    private Stage stage;
    private Class<Main> mainAppClass;
    private TreeItem<Object> originalRoot;
    private boolean hasUnsavedChanges = false;
    private File selectedMMOItemsFolder;
    private File currentSelectedYmlFile;
    private MMOItemEntry currentEditingItem;

    private final ChangeListener<Object> unsavedChangesListener = (obs, oldVal, newVal) -> setUnsavedChanges(true);

    public EditorController() {
        this.dataService = new YAMLDataService();
        this.configService = new ConfigService();
        this.fileIOService = new FileIOService(dataService);
    }

    @FXML
    private void initialize() {
        this.translations = ResourceBundle.getBundle("com.yourname.mmoitemseditor.messages", Locale.getDefault());

        setupFileTreeView();
        buildLanguageMenu();
        setupVersionComboBox();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTreeView(newVal));
        buildAttributeUI();
        setupActionButtons();
        updateButtonsState();
        showEditor(false);

        permEffectNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        permEffectLevelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setMainApp(Class<Main> mainAppClass) {
        this.mainAppClass = mainAppClass;
    }

    //<editor-fold desc="UI Building and Handlers">

    private void setupVersionComboBox() {
        versionComboBox.setItems(FXCollections.observableArrayList("1.12.2"));
        versionComboBox.setValue("1.12.2");
        loadConfigForVersion("1.12.2");

        versionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVersion, newVersion) -> {
            if (newVersion != null && !newVersion.equals(oldVersion)) {
                loadConfigForVersion(newVersion);
                rebuildAttributeUI();
            }
        });
    }

    private void loadConfigForVersion(String version) {
        try {
            configService.loadVersion(version);
        } catch (DataAccessException e) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.startup"), String.format(getTranslation("dialog.error.startup.message"), e.getMessage()));
            });
        }
    }

    private void rebuildAttributeUI() {
        attributeControls.clear();
        basicAttributesPane.getChildren().clear();
        statsAttributesPane.getChildren().clear();
        advancedAttributesPane.getChildren().clear();
        loreEnchantsPane.getChildren().clear();
        complexAttributesPane.getChildren().clear();

        buildAttributeUI();

        if (currentEditingItem != null) {
            loadItemContent(currentEditingItem);
        }
    }

    private void buildAttributeUI() {
        AttributePaneBuilder builder = new AttributePaneBuilder(
                configService, translations, attributeControls, unsavedChangesListener,
                basicAttributesPane, statsAttributesPane, advancedAttributesPane,
                loreEnchantsPane, complexAttributesPane
        );
        builder.build();
    }

    private void setupActionButtons() {
        addEnchantButton.setOnAction(e -> handleAddEnchantment());
        removeEnchantButton.setOnAction(e -> handleRemoveEnchantment());
        addAbilityButton.setOnAction(e -> handleAddAbility());
        editAbilityButton.setOnAction(e -> handleEditAbility());
        removeAbilityButton.setOnAction(e -> handleRemoveAbility());
    }

    private void buildLanguageMenu() {
        Menu langMenu = new Menu(getTranslation("language"));
        ToggleGroup langToggleGroup = new ToggleGroup();

        RadioMenuItem enMenuItem = new RadioMenuItem("English");
        enMenuItem.setToggleGroup(langToggleGroup);
        enMenuItem.setSelected(Locale.getDefault().getLanguage().equals("en"));
        enMenuItem.setOnAction(e -> switchLanguage(Locale.ENGLISH));

        RadioMenuItem zhMenuItem = new RadioMenuItem("中文");
        zhMenuItem.setToggleGroup(langToggleGroup);
        zhMenuItem.setSelected(Locale.getDefault().getLanguage().equals("zh"));
        zhMenuItem.setOnAction(e -> switchLanguage(Locale.CHINESE));

        langMenu.getItems().addAll(enMenuItem, zhMenuItem);
        menuBar.getMenus().add(langMenu);
    }

    private void switchLanguage(Locale locale) {
        if (mainAppClass != null) {
            try {
                Main.loadScene(locale);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not reload the user interface for the new language.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAddAbility() {
        TableView<AbilityEntry> table = (TableView<AbilityEntry>) attributeControls.get("ability_table");
        if (table == null) return;
        new AbilityEditorDialog(null, configService, translations).showAndWait().ifPresent(newAbility -> {
            table.getItems().add(newAbility);
            setUnsavedChanges(true);
        });
    }

    @SuppressWarnings("unchecked")
    private void handleEditAbility() {
        TableView<AbilityEntry> table = (TableView<AbilityEntry>) attributeControls.get("ability_table");
        if (table == null) return;
        AbilityEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, getTranslation("information"), getTranslation("dialog.warn.ability.select"));
            return;
        }

        new AbilityEditorDialog(selected, configService, translations).showAndWait().ifPresent(editedAbility -> {
            editedAbility.updateSummary(); // Manually trigger summary update
            table.refresh();
            setUnsavedChanges(true);
        });
    }

    @SuppressWarnings("unchecked")
    private void handleRemoveAbility() {
        TableView<AbilityEntry> table = (TableView<AbilityEntry>) attributeControls.get("ability_table");
        if (table == null) return;
        AbilityEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            table.getItems().remove(selected);
            setUnsavedChanges(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleAddEnchantment() {
        TableView<EnchantmentEntry> table = (TableView<EnchantmentEntry>) attributeControls.get("enchants_table");
        if (table == null) return;

        Dialog<Pair<String, Integer>> dialog = new AddEntryDialog(
                getTranslation("dialog.add.enchant.title"),
                getTranslation("dialog.add.enchant.header"),
                getTranslation("dialog.add.enchant.prompt"),
                getTranslation("label.enchantment"),
                configService.getEnchantments(),
                translations
        );

        dialog.showAndWait().ifPresent(pair -> {
            for (EnchantmentEntry entry : table.getItems()) {
                if (entry.getName().equalsIgnoreCase(pair.getKey())) {
                    showAlert(Alert.AlertType.WARNING, getTranslation("warning"), getTranslation("dialog.warn.enchant.exists"));
                    return;
                }
            }
            table.getItems().add(new EnchantmentEntry(pair.getKey(), pair.getValue()));
            setUnsavedChanges(true);
        });
    }

    @SuppressWarnings("unchecked")
    private void handleRemoveEnchantment() {
        TableView<EnchantmentEntry> table = (TableView<EnchantmentEntry>) attributeControls.get("enchants_table");
        if (table == null) return;
        EnchantmentEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            table.getItems().remove(selected);
            setUnsavedChanges(true);
        }
    }


    @FXML
    private void handleAddPermEffect() {
        Dialog<Pair<String, Integer>> dialog = new AddEntryDialog(
                getTranslation("dialog.add.effect.title"),
                getTranslation("dialog.add.effect.header"),
                getTranslation("dialog.add.effect.prompt"),
                getTranslation("label.effect"),
                configService.getPotionEffects(),
                translations
        );

        dialog.showAndWait().ifPresent(pair -> {
            for (PotionEffectEntry entry : permEffectsTable.getItems()) {
                if (entry.getName().equalsIgnoreCase(pair.getKey())) {
                    showAlert(Alert.AlertType.WARNING, getTranslation("warning"), getTranslation("dialog.warn.effect.exists"));
                    return;
                }
            }
            permEffectsTable.getItems().add(new PotionEffectEntry(pair.getKey(), pair.getValue()));
            setUnsavedChanges(true);
        });
    }

    @FXML
    private void handleRemovePermEffect() {
        PotionEffectEntry selected = permEffectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            permEffectsTable.getItems().remove(selected);
            setUnsavedChanges(true);
        }
    }

    private void setupFileTreeView() {
        fileTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : (item instanceof File) ? ((File) item).getName() : item.toString());
            }
        });
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            handleTreeSelection(oldVal, newVal);
        });
    }

    private void handleTreeSelection(TreeItem<Object> oldVal, TreeItem<Object> newVal) {
        if (hasUnsavedChanges) {
            if (!canClose()) {
                 javafx.application.Platform.runLater(() -> fileTreeView.getSelectionModel().select(oldVal));
                return;
            }
        }
        
        setUnsavedChanges(false);

        if (newVal == null) {
            clearEditor();
            showEditor(false);
            return;
        }

        Object selectedValue = newVal.getValue();
        if (selectedValue instanceof File && !((File) selectedValue).isDirectory()) {
            currentSelectedYmlFile = (File) selectedValue;
            currentEditingItem = null;
            clearEditor();
            editingItemIdLabel.setText(String.format(getTranslation("main.window.editing.file"), currentSelectedYmlFile.getName()));
            showEditor(true);
        } else if (selectedValue instanceof MMOItemEntry) {
            currentEditingItem = (MMOItemEntry) selectedValue;
            currentSelectedYmlFile = currentEditingItem.getParentFile();
            loadItemContent(currentEditingItem);
            showEditor(true);
        } else {
            currentSelectedYmlFile = null;
            currentEditingItem = null;
            clearEditor();
            showEditor(false);
        }
        updateButtonsState();
    }


    @SuppressWarnings("unchecked")
    private void clearEditor() {
        editingItemIdLabel.setText("");
        for (Node control : attributeControls.values()) {
            if (control instanceof TextInputControl) {
                ((TextInputControl) control).clear();
            } else if (control instanceof CheckBox) {
                ((CheckBox) control).setSelected(false);
            } else if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setValue(null);
            } else if (control instanceof Spinner) {
                Spinner<?> spinner = (Spinner<?>) control;
                if (spinner.getValueFactory().getValue() instanceof Integer) {
                    ((Spinner<Integer>)spinner).getValueFactory().setValue(0);
                } else if (spinner.getValueFactory().getValue() instanceof Double) {
                    ((Spinner<Double>)spinner).getValueFactory().setValue(0.0);
                }
            } else if (control instanceof TableView) {
                if (!Objects.equals(control.getId(), "ability_table")) { // Don't clear the table itself
                    ((TableView<?>) control).getItems().clear();
                }
            }
        }
        permEffectsTable.getItems().clear();
        TableView<AbilityEntry> abilityTable = (TableView<AbilityEntry>) attributeControls.get("ability_table");
        if (abilityTable != null) {
            abilityTable.getItems().clear();
        }
        setUnsavedChanges(false);
    }

    private void updateButtonsState() {
        boolean itemSelected = currentEditingItem != null;
        boolean fileSelected = currentSelectedYmlFile != null;

        newItemButton.setDisable(!fileSelected);
        duplicateItemButton.setDisable(!itemSelected);
        deleteItemButton.setDisable(!itemSelected);
        saveChangesButton.setDisable(!itemSelected);
    }

    private void showEditor(boolean show) {
        editorPane.setVisible(show);
        welcomePane.setVisible(!show);
    }
    //</editor-fold>

    //<editor-fold desc="File Loading and Saving">
    @FXML
    private void loadDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(getTranslation("open.folder"));
        if (selectedMMOItemsFolder != null && selectedMMOItemsFolder.exists()) {
            directoryChooser.setInitialDirectory(selectedMMOItemsFolder);
        }

        File selectedDirectory = directoryChooser.showDialog(statusLabel.getScene().getWindow());
        if (selectedDirectory == null) {
            return;
        }

        selectedMMOItemsFolder = selectedDirectory;
        clearEditor();
        showEditor(false);
        fileTreeView.setRoot(null); // Clear the tree view while loading

        Task<TreeItem<Object>> loadTask = fileIOService.createLoadDirectoryTask(selectedDirectory);

        // Bind status label to task's message
        statusLabel.textProperty().bind(loadTask.messageProperty());
        loadTask.messageProperty().addListener((obs, oldMsg, newMsg) -> statusLabel.setText(getTranslation(newMsg)));

        // Disable UI elements that shouldn't be used during load
        menuBar.setDisable(true);
        fileTreeView.setDisable(true);
        searchField.setDisable(true);

        // Handle successful completion
        loadTask.setOnSucceeded(event -> {
            TreeItem<Object> rootItem = loadTask.getValue();
            originalRoot = rootItem;
            fileTreeView.setRoot(originalRoot);
            searchField.clear();
            statusLabel.textProperty().unbind();
            statusLabel.setText(String.format(getTranslation("main.window.status.loaded"), selectedDirectory.getAbsolutePath()));
            menuBar.setDisable(false);
            fileTreeView.setDisable(false);
            searchField.setDisable(false);
            setUnsavedChanges(false);
        });

        // Handle failure
        loadTask.setOnFailed(event -> {
            Throwable e = loadTask.getException();
            showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.load"), String.format(getTranslation("dialog.error.load.message"), e.getMessage()));
            e.printStackTrace();
            statusLabel.textProperty().unbind();
            statusLabel.setText(getTranslation("main.window.status.load.failed"));
            menuBar.setDisable(false);
            fileTreeView.setDisable(false);
            searchField.setDisable(false);
        });

        // Run the task on a new thread
        new Thread(loadTask).start();
    }

    @FXML
    private void saveChanges() {
        if (currentEditingItem == null) return;

        Map<String, Object> newItemData = collectItemDataFromControls();

        try {
            fileIOService.saveItem(currentEditingItem, newItemData);
            setUnsavedChanges(false);
            showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), String.format(getTranslation("dialog.success.save.item"), currentEditingItem.getItemId()));
        } catch (DataAccessException e) {
            showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.save"), String.format(getTranslation("dialog.error.save.item.message"), e.getMessage()));
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadItemContent(MMOItemEntry itemEntry) {
        clearEditor();
        editingItemIdLabel.setText(itemEntry.getItemId() + " (" + String.format(getTranslation("main.window.editing.file"), itemEntry.getParentFile().getName()) + ")");
        Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(itemEntry.getParentFile());
        if (itemsInFile != null) {
            Map<String, Object> itemData = itemsInFile.get(itemEntry.getItemId());
            if (itemData == null) return;

            for (Map.Entry<String, Node> entry : attributeControls.entrySet()) {
                String attrName = entry.getKey();
                Node control = entry.getValue();
                Object value = itemData.get(attrName);

                if (value == null) continue;

                if (control instanceof CheckBox) {
                    ((CheckBox) control).setSelected(Boolean.parseBoolean(value.toString()));
                } else if (control instanceof TextField) {
                    ((TextField) control).setText(value.toString());
                } else if (control instanceof ComboBox) {
                    ((ComboBox<String>) control).setValue(value.toString());
                } else if (control instanceof Spinner) {
                    if (value instanceof Number) {
                        if (((Spinner<?>) control).getValueFactory().getValue() instanceof Integer) {
                            ((Spinner<Integer>) control).getValueFactory().setValue(((Number) value).intValue());
                        } else {
                            ((Spinner<Double>) control).getValueFactory().setValue(((Number) value).doubleValue());
                        }
                    }
                } else if (control instanceof TextArea) {
                    if (!attrName.equals("perm-effects")) {
                        ((TextArea) control).setText(dataService.getYaml().dump(value));
                    }
                }
            }

            TableView<EnchantmentEntry> enchantmentsTable = (TableView<EnchantmentEntry>) attributeControls.get("enchants_table");
            enchantmentsTable.getItems().clear();
            Object enchantsObj = itemData.get("enchants");
            if (enchantsObj instanceof Map) {
                Map<String, Object> enchantsMap = (Map<String, Object>) enchantsObj;
                enchantsMap.forEach((name, level) -> {
                    if (level instanceof Number) {
                        enchantmentsTable.getItems().add(new EnchantmentEntry(name, ((Number) level).intValue()));
                    }
                });
            }

            permEffectsTable.getItems().clear();
            Object permEffectsObj = itemData.get("perm-effects");
            if (permEffectsObj instanceof Map) {
                Map<String, Object> permEffectsMap = (Map<String, Object>) permEffectsObj;
                permEffectsMap.forEach((name, level) -> {
                    if (level instanceof Number) {
                        permEffectsTable.getItems().add(new PotionEffectEntry(name, ((Number) level).intValue()));
                    }
                });
            }

            TableView<AbilityEntry> abilityTable = (TableView<AbilityEntry>) attributeControls.get("ability_table");
            abilityTable.getItems().clear();
            Object abilityObj = itemData.get("ability");
            if (abilityObj instanceof Map) {
                Map<String, Object> abilityMap = (Map<String, Object>) abilityObj;
                abilityMap.forEach((id, abilityData) -> {
                    if (abilityData instanceof Map) {
                        Map<String, Object> abilityDataMap = (Map<String, Object>) abilityData;
                        String type = String.valueOf(abilityDataMap.getOrDefault("type", ""));
                        String mode = String.valueOf(abilityDataMap.getOrDefault("mode", ""));
                        abilityTable.getItems().add(new AbilityEntry(id, type, mode, abilityDataMap));
                    }
                });
            }
        }
        setUnsavedChanges(false);
    }

    @FXML
    private void handleSaveAll() {
        // First, ensure the currently edited item is saved to the in-memory model
        if (currentEditingItem != null && hasUnsavedChanges) {
            Map<String, Object> currentItemData = collectItemDataFromControls();
            Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(currentEditingItem.getParentFile());
            if (itemsInFile != null) {
                itemsInFile.put(currentEditingItem.getItemId(), currentItemData);
            }
        }

        try {
            fileIOService.saveAll();
            setUnsavedChanges(false);
            showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), getTranslation("dialog.success.save.all"));
        } catch (DataAccessException e) {
            showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.save"), String.format(getTranslation("dialog.error.save.all.message"), e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveAs() {
        if (currentEditingItem == null) {
            showAlert(Alert.AlertType.INFORMATION, getTranslation("information"), getTranslation("dialog.warn.no.item.selected"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getTranslation("dialog.save.as.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yml"));
        if (selectedMMOItemsFolder != null) {
            fileChooser.setInitialDirectory(selectedMMOItemsFolder);
        }
        fileChooser.setInitialFileName(currentEditingItem.getItemId() + ".yml");

        File targetFile = fileChooser.showSaveDialog(stage);
        if (targetFile == null) {
            return;
        }

        Map<String, Object> itemDataToSave = collectItemDataFromControls();
        String itemId = currentEditingItem.getItemId();

        try {
            if (!dataService.getLoadedFilesData().containsKey(targetFile)) {
                if (targetFile.exists()) {
                    dataService.loadSingleYmlFile(targetFile);
                } else {
                    dataService.getLoadedFilesData().put(targetFile, new LinkedHashMap<>());
                }
            }

            Map<String, Map<String, Object>> targetFileData = dataService.getLoadedFilesData().get(targetFile);
            targetFileData.put(itemId, itemDataToSave);

            dataService.saveToFile(targetFile);

            refreshTreeView();
            showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), String.format(getTranslation("dialog.success.save.as"), itemId, targetFile.getName()));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.save.as"), e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        if (canClose()) {
            Platform.exit();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> collectItemDataFromControls() {
        Map<String, Object> itemData = new LinkedHashMap<>();

        for (Map.Entry<String, Node> entry : attributeControls.entrySet()) {
            String attrName = entry.getKey();
            Node control = entry.getValue();

            if (attrName.equals("enchants_table") || attrName.equals("ability_table")) continue;

            if (control instanceof CheckBox) {
                if (((CheckBox) control).isSelected()) itemData.put(attrName, true);
            } else if (control instanceof TextField) {
                String text = ((TextField) control).getText();
                if (text != null && !text.trim().isEmpty()) itemData.put(attrName, Utils.smartConvert(text));
            } else if (control instanceof ComboBox) {
                Object value = ((ComboBox<?>) control).getValue();
                if (value != null && !value.toString().trim().isEmpty()) itemData.put(attrName, value.toString());
            } else if (control instanceof Spinner) {
                Object value = ((Spinner<?>) control).getValue();
                if (value != null && ((Number) value).doubleValue() != 0.0) {
                    itemData.put(attrName, value);
                }
            } else if (control instanceof TextArea) {
                String text = ((TextArea) control).getText();
                if (text != null && !text.trim().isEmpty()) {
                    try {
                        Object loaded = dataService.getYaml().load(text);
                        if(loaded != null) {
                            itemData.put(attrName, loaded);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.WARNING, getTranslation("dialog.error.yaml.syntax"), String.format(getTranslation("dialog.error.yaml.syntax.message"), attrName));
                    }
                }
            }
        }

        TableView<EnchantmentEntry> enchantmentsTable = (TableView<EnchantmentEntry>) attributeControls.get("enchants_table");
        if (enchantmentsTable != null && !enchantmentsTable.getItems().isEmpty()) {
            Map<String, Integer> enchantsMap = new LinkedHashMap<>();
            for (EnchantmentEntry ench : enchantmentsTable.getItems()) {
                enchantsMap.put(ench.getName(), ench.getLevel());
            }
            itemData.put("enchants", enchantsMap);
        }

        if (permEffectsTable != null && !permEffectsTable.getItems().isEmpty()) {
            Map<String, Integer> permEffectsMap = new LinkedHashMap<>();
            for (PotionEffectEntry effect : permEffectsTable.getItems()) {
                permEffectsMap.put(effect.getName(), effect.getLevel());
            }
            itemData.put("perm-effects", permEffectsMap);
        }

        TableView<AbilityEntry> abilityTable = (TableView<AbilityEntry>) attributeControls.get("ability_table");
        if (abilityTable != null && !abilityTable.getItems().isEmpty()) {
            Map<String, Object> abilitiesMap = new LinkedHashMap<>();
            for (AbilityEntry ability : abilityTable.getItems()) {
                abilitiesMap.put(ability.getId(), ability.getModifiers());
            }
            itemData.put("ability", abilitiesMap);
        }

        return itemData;
    }
    //</editor-fold>

    //<editor-fold desc="Unsaved Changes Logic">
    private void setUnsavedChanges(boolean hasChanges) {
        if (this.hasUnsavedChanges == hasChanges) return;
        this.hasUnsavedChanges = hasChanges;

        if (stage == null) return;
        String title = stage.getTitle().replace(" *", "");
        if (hasChanges) {
            stage.setTitle(title + " *");
        } else {
            stage.setTitle(title);
        }
    }

    public boolean canClose() {
        if (!hasUnsavedChanges) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getTranslation("dialog.warn.unsaved.title"));
        alert.setHeaderText(getTranslation("dialog.warn.unsaved.header"));
        alert.getButtonTypes().setAll(new ButtonType(getTranslation("yes"), ButtonBar.ButtonData.YES), new ButtonType(getTranslation("no"), ButtonBar.ButtonData.NO), new ButtonType(getTranslation("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get().getButtonData() == ButtonBar.ButtonData.YES) {
                saveChanges();
                return !hasUnsavedChanges;
            } else return result.get().getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE;
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="Item Management Methods">
    @FXML
    private void handleNewItem() {
        if (currentSelectedYmlFile == null) {
            showAlert(Alert.AlertType.WARNING, getTranslation("warning"), getTranslation("dialog.warn.no.file.selected"));
            return;
        }

        TextInputDialog dialog = new TextInputDialog(getTranslation("dialog.new.item.default"));
        dialog.setTitle(getTranslation("dialog.new.item.title"));
        dialog.setHeaderText(getTranslation("dialog.new.item.header"));
        dialog.setContentText(getTranslation("dialog.new.item.content"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newId -> {
            newId = newId.trim().toUpperCase().replace(" ", "_");
            if (newId.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, getTranslation("warning"), getTranslation("dialog.warn.id.empty"));
                return;
            }

            Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(currentSelectedYmlFile);
            if (itemsInFile != null && itemsInFile.containsKey(newId)) {
                showAlert(Alert.AlertType.WARNING, getTranslation("warning"), String.format(getTranslation("dialog.warn.id.exists"), newId));
                return;
            }

            Map<String, Object> newItemData = new LinkedHashMap<>();
            newItemData.put("material", "STONE");
            newItemData.put("name", "&f" + newId.replace("_", " "));

            if (itemsInFile == null) {
                itemsInFile = new LinkedHashMap<>();
                dataService.getLoadedFilesData().put(currentSelectedYmlFile, itemsInFile);
            }
            itemsInFile.put(newId, newItemData);
            try {
                dataService.saveToFile(currentSelectedYmlFile);
            } catch (DataAccessException e) {
                showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.save"), String.format(getTranslation("dialog.error.save.item"), e.getMessage()));
                e.printStackTrace();
                return; // Don't continue if save failed
            }

            MMOItemEntry newEntry = new MMOItemEntry(newId, currentSelectedYmlFile);
            currentEditingItem = newEntry;
            refreshTreeView(); 
            loadItemContent(currentEditingItem);

            setUnsavedChanges(false);
            showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), String.format(getTranslation("dialog.success.new.item"), newId));
        });
    }

    @FXML
    private void handleDuplicateItem() {
        if (currentEditingItem == null) return;

        TextInputDialog dialog = new TextInputDialog(currentEditingItem.getItemId() + "_COPY");
        dialog.setTitle(getTranslation("dialog.duplicate.item.title"));
        dialog.setHeaderText(getTranslation("dialog.duplicate.item.header"));
        dialog.setContentText(getTranslation("dialog.duplicate.item.content"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newId -> {
            newId = newId.trim().toUpperCase().replace(" ", "_");
            if (newId.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, getTranslation("warning"), getTranslation("dialog.warn.id.empty"));
                return;
            }

            File parentFile = currentEditingItem.getParentFile();
            Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(parentFile);

            if (itemsInFile != null && itemsInFile.containsKey(newId)) {
                showAlert(Alert.AlertType.WARNING, getTranslation("warning"), String.format(getTranslation("dialog.warn.id.exists"), newId));
                return;
            }

            Map<String, Object> originalItemData = itemsInFile.get(currentEditingItem.getItemId());
            Map<String, Object> duplicatedItemData = dataService.deepCopyMap(originalItemData);

            itemsInFile.put(newId, duplicatedItemData);

            try {
                dataService.saveToFile(parentFile);
            } catch (DataAccessException e) {
                showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.save"), String.format(getTranslation("dialog.error.save.item"), e.getMessage()));
                e.printStackTrace();
                return; // Don't continue if save failed
            }

            MMOItemEntry newEntry = new MMOItemEntry(newId, parentFile);
            currentEditingItem = newEntry;
            refreshTreeView();
            loadItemContent(currentEditingItem);

            setUnsavedChanges(false);
            showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), String.format(getTranslation("dialog.success.duplicate.item"), newId));
        });
    }

    @FXML
    private void handleDeleteItem() {
        if (currentEditingItem == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getTranslation("dialog.delete.item.title"));
        alert.setHeaderText(String.format(getTranslation("dialog.delete.item.header"), currentEditingItem.getItemId()));
        alert.setContentText(getTranslation("dialog.delete.item.content"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            File parentFile = currentEditingItem.getParentFile();
            String itemIdToRemove = currentEditingItem.getItemId();

            Map<String, Map<String, Object>> itemsInFile = dataService.getLoadedFilesData().get(parentFile);
            if (itemsInFile != null) {
                itemsInFile.remove(itemIdToRemove);

                clearEditor();
                showEditor(false);
                try {
                    dataService.saveToFile(parentFile);
                    setUnsavedChanges(false);
                    showAlert(Alert.AlertType.INFORMATION, getTranslation("success"), String.format(getTranslation("dialog.success.delete.item"), itemIdToRemove));
                    refreshTreeView();
                } catch (DataAccessException e) {
                    showAlert(Alert.AlertType.ERROR, getTranslation("dialog.error.delete"), String.format(getTranslation("dialog.error.delete.message"), e.getMessage()));
                    e.printStackTrace();
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    private String getTranslation(String key) {
        try {
            return translations.getString(key);
        } catch (MissingResourceException e) {
            // Fallback for missing keys
            System.err.println("Missing translation key: " + key);
            return "!" + key + "!";
        }
    }

    private TreeItem<Object> findTreeItem(TreeItem<Object> root, Object value) {
        if (root == null || root.getValue() == null || value == null) return null;

        if (root.getValue() instanceof MMOItemEntry && value instanceof MMOItemEntry) {
            MMOItemEntry rootItem = (MMOItemEntry) root.getValue();
            MMOItemEntry valueItem = (MMOItemEntry) value;
            if (rootItem.getItemId().equals(valueItem.getItemId()) && rootItem.getParentFile().equals(valueItem.getParentFile())) {
                return root;
            }
        } else if (root.getValue().equals(value)) {
            return root;
        }

        for (TreeItem<Object> child : root.getChildren()) {
            TreeItem<Object> found = findTreeItem(child, value);
            if (found != null) return found;
        }
        return null;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void filterTreeView(String filter) {
        if (originalRoot == null) {
            return;
        }

        if (filter == null || filter.trim().isEmpty()) {
            fileTreeView.setRoot(originalRoot);
            return;
        }

        String lowerCaseFilter = filter.toLowerCase().trim();

        TreeItem<Object> filteredRoot = new TreeItem<>(originalRoot.getValue());
        filteredRoot.setExpanded(true);

        for (TreeItem<Object> fileItem : originalRoot.getChildren()) {
            TreeItem<Object> newFileItem = new TreeItem<>(fileItem.getValue());

            for (TreeItem<Object> itemEntryItem : fileItem.getChildren()) {
                if (itemEntryItem.getValue() instanceof MMOItemEntry) {
                    MMOItemEntry mmoItem = (MMOItemEntry) itemEntryItem.getValue();
                    if (mmoItem.getItemId().toLowerCase().contains(lowerCaseFilter)) {
                        newFileItem.getChildren().add(new TreeItem<>(mmoItem));
                    }
                }
            }

            if (!newFileItem.getChildren().isEmpty()) {
                newFileItem.setExpanded(true);
                filteredRoot.getChildren().add(newFileItem);
            }
        }
        fileTreeView.setRoot(filteredRoot);
    }

    private void refreshTreeView() {
        if (selectedMMOItemsFolder == null) return;

        TreeItem<Object> newRoot = new TreeItem<>(selectedMMOItemsFolder);
        newRoot.setExpanded(true);
        TreeViewBuilder.populateTreeView(newRoot, selectedMMOItemsFolder, dataService);
        this.originalRoot = newRoot;

        filterTreeView(searchField.getText());

        if (currentEditingItem != null) {
            TreeItem<Object> itemToSelect = findTreeItem(fileTreeView.getRoot(), currentEditingItem);
            if (itemToSelect != null) {
                fileTreeView.getSelectionModel().select(itemToSelect);
            }
        }
    }
    //</editor-fold>
}
