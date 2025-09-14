package com.yourname.mmoitemseditor;

import com.yourname.mmoitemseditor.model.EnchantmentEntry;
import com.yourname.mmoitemseditor.model.MMOItemEntry;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EditorController {

    //<editor-fold desc="FXML Declarations">
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
    //</editor-fold>

    //<editor-fold desc="Minecraft 1.12.2 Data & Translations">
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static final List<String> MC_1_12_2_MATERIALS;
    private static final List<String> MC_1_12_2_ENCHANTMENTS;
    private static final List<String> BASIC_ATTRIBUTES = Arrays.asList("material", "name", "tier");
    private static final List<String> STATS_ATTRIBUTES = Arrays.asList("attack-damage", "attack-speed", "critical-strike-chance", "critical-strike-power", "max-health", "health-regeneration", "armor", "armor-toughness", "movement-speed", "knockback-resistance", "pve-damage", "pvp-damage", "magic-damage", "skill-damage", "damage-reduction", "fall-damage-reduction", "required-level", "required-class");
    private static final List<String> ADVANCED_ATTRIBUTES_BOOLEAN = Arrays.asList("unbreakable", "two-handed", "hide-enchants", "disable-interaction", "disable-repairing", "disable-enchanting", "disable-crafting", "disable-smelting", "can-identify", "can-deconstruct");
    private static final List<String> ADVANCED_ATTRIBUTES_TEXT = Arrays.asList("max-durability", "consume-cooldown", "gem-sockets", "soulbound-level", "soulbinding-chance", "soulbound-break-chance", "potion-color", "skull-texture");
    private static final String LORE_ATTRIBUTE = "lore";
    private static final List<String> COMPLEX_ATTRIBUTES = Arrays.asList("ability", "perm-effects", "effects", "restore", "element", "craft", "advanced-craft", "shield-pattern", "commands");

    static {
        TRANSLATIONS.put("material", "材质");
        TRANSLATIONS.put("name", "名称");
        TRANSLATIONS.put("tier", "阶级");
        TRANSLATIONS.put("attack-damage", "攻击伤害");
        TRANSLATIONS.put("attack-speed", "攻击速度");
        TRANSLATIONS.put("critical-strike-chance", "暴击几率");
        TRANSLATIONS.put("critical-strike-power", "暴击伤害");
        TRANSLATIONS.put("max-health", "最大生命值");
        TRANSLATIONS.put("health-regeneration", "生命恢复");
        TRANSLATIONS.put("armor", "护甲");
        TRANSLATIONS.put("armor-toughness", "护甲韧性");
        TRANSLATIONS.put("movement-speed", "移动速度");
        TRANSLATIONS.put("knockback-resistance", "击退抗性");
        TRANSLATIONS.put("pve-damage", "PvE 伤害 (%)");
        TRANSLATIONS.put("pvp-damage", "PvP 伤害 (%)");
        TRANSLATIONS.put("magic-damage", "魔法伤害");
        TRANSLATIONS.put("skill-damage", "技能伤害 (%)");
        TRANSLATIONS.put("damage-reduction", "伤害减免 (%)");
        TRANSLATIONS.put("fall-damage-reduction", "掉落伤害减免 (%)");
        TRANSLATIONS.put("required-level", "需要等级");
        TRANSLATIONS.put("required-class", "需要职业");
        TRANSLATIONS.put("max-durability", "最大耐久");
        TRANSLATIONS.put("consume-cooldown", "消耗冷却(秒)");
        TRANSLATIONS.put("gem-sockets", "宝石插槽");
        TRANSLATIONS.put("soulbound-level", "灵魂绑定等级");
        TRANSLATIONS.put("soulbinding-chance", "灵魂绑定几率");
        TRANSLATIONS.put("soulbound-break-chance", "灵魂绑定破坏几率");
        TRANSLATIONS.put("potion-color", "药水颜色(RGB)");
        TRANSLATIONS.put("skull-texture", "头颅材质(Base64)");
        TRANSLATIONS.put("unbreakable", "无法破坏");
        TRANSLATIONS.put("two-handed", "双手持有");
        TRANSLATIONS.put("hide-enchants", "隐藏附魔");
        TRANSLATIONS.put("disable-interaction", "禁用交互");
        TRANSLATIONS.put("disable-repairing", "禁用修复");
        TRANSLATIONS.put("disable-enchanting", "禁用附魔");
        TRANSLATIONS.put("disable-crafting", "禁用合成");
        TRANSLATIONS.put("disable-smelting", "禁用熔炼");
        TRANSLATIONS.put("can-identify", "可鉴定");
        TRANSLATIONS.put("can-deconstruct", "可分解");
        TRANSLATIONS.put("lore", "描述 (Lore)");
        TRANSLATIONS.put("enchants", "附魔");
        TRANSLATIONS.put("ability", "技能");
        TRANSLATIONS.put("perm-effects", "永久药水效果");
        TRANSLATIONS.put("effects", "命中/消耗效果");
        TRANSLATIONS.put("restore", "恢复属性");
        TRANSLATIONS.put("element", "元素");
        TRANSLATIONS.put("craft", "原版合成配方");
        TRANSLATIONS.put("advanced-craft", "高级合成配方");
        TRANSLATIONS.put("shield-pattern", "盾牌图案");
        TRANSLATIONS.put("commands", "执行指令");

        MC_1_12_2_MATERIALS = Arrays.asList(
            "STONE", "GRASS", "DIRT", "COBBLESTONE", "WOOD", "SAPLING", "BEDROCK", "WATER", "LAVA", "SAND", "GRAVEL", "GOLD_ORE",
            "IRON_ORE", "COAL_ORE", "LOG", "LEAVES", "SPONGE", "GLASS", "LAPIS_ORE", "LAPIS_BLOCK", "DISPENSER", "SANDSTONE",
            "NOTE_BLOCK", "BED", "POWERED_RAIL", "DETECTOR_RAIL", "STICKY_PISTON", "WEB", "LONG_GRASS", "DEAD_BUSH", "PISTON_BASE",
            "WOOL", "YELLOW_FLOWER", "RED_ROSE", "BROWN_MUSHROOM", "RED_MUSHROOM", "GOLD_BLOCK", "IRON_BLOCK", "DOUBLE_STEP",
            "STEP", "BRICK", "TNT", "BOOKSHELF", "MOSSY_COBBLESTONE", "OBSIDIAN", "TORCH", "FIRE", "MOB_SPAWNER", "WOOD_STAIRS",
            "CHEST", "DIAMOND_ORE", "DIAMOND_BLOCK", "WORKBENCH", "CROPS", "SOIL", "FURNACE", "SIGN_POST", "WOODEN_DOOR", "LADDER",
            "RAILS", "COBBLESTONE_STAIRS", "WALL_SIGN", "LEVER", "STONE_PLATE", "IRON_DOOR_BLOCK", "WOOD_PLATE", "REDSTONE_ORE",
            "REDSTONE_TORCH_ON", "STONE_BUTTON", "SNOW", "ICE", "SNOW_BLOCK", "CACTUS", "CLAY", "SUGAR_CANE_BLOCK", "JUKEBOX",
            "FENCE", "PUMPKIN", "NETHERRACK", "SOUL_SAND", "GLOWSTONE", "PORTAL", "JACK_O_LANTERN", "CAKE_BLOCK", "DIODE_BLOCK_OFF",
            "STAINED_GLASS", "TRAP_DOOR", "MONSTER_EGG", "SMOOTH_BRICK", "HUGE_MUSHROOM_1", "HUGE_MUSHROOM_2", "IRON_FENCE",
            "THIN_GLASS", "MELON_BLOCK", "PUMPKIN_STEM", "MELON_STEM", "VINE", "FENCE_GATE", "BRICK_STAIRS", "SMOOTH_STAIRS",
            "MYCEL", "WATER_LILY", "NETHER_BRICK", "NETHER_FENCE", "NETHER_BRICK_STAIRS", "NETHER_WARTS", "ENCHANTMENT_TABLE",
            "BREWING_STAND", "CAULDRON", "ENDER_PORTAL", "ENDER_PORTAL_FRAME", "ENDER_STONE", "DRAGON_EGG", "REDSTONE_LAMP_OFF",
            "WOOD_DOUBLE_STEP", "WOOD_STEP", "COCOA", "SANDSTONE_STAIRS", "EMERALD_ORE", "ENDER_CHEST", "TRIPWIRE_HOOK", "TRIPWIRE",
            "EMERALD_BLOCK", "SPRUCE_WOOD_STAIRS", "BIRCH_WOOD_STAIRS", "JUNGLE_WOOD_STAIRS", "COMMAND", "BEACON", "COBBLE_WALL",
            "FLOWER_POT", "CARROT", "POTATO", "WOOD_BUTTON", "SKULL", "ANVIL", "TRAPPED_CHEST", "GOLD_PLATE", "IRON_PLATE",
            "REDSTONE_COMPARATOR_OFF", "DAYLIGHT_DETECTOR", "REDSTONE_BLOCK", "QUARTZ_ORE", "HOPPER", "QUARTZ_BLOCK", "QUARTZ_STAIRS",
            "ACTIVATOR_RAIL", "DROPPER", "STAINed_CLAY", "STAINED_GLASS_PANE", "LEAVES_2", "LOG_2", "ACACIA_STAIRS", "DARK_OAK_STAIRS",
            "SLIME_BLOCK", "BARRIER", "IRON_TRAPDOOR", "PRISMARINE", "SEA_LANTERN", "HAY_BLOCK", "CARPET", "HARD_CLAY", "COAL_BLOCK",
            "PACKED_ICE", "DOUBLE_PLANT", "STANDING_BANNER", "WALL_BANNER", "DAYLIGHT_DETECTOR_INVERTED", "RED_SANDSTONE",
            "RED_SANDSTONE_STAIRS", "DOUBLE_STONE_SLAB2", "STONE_SLAB2", "SPRUCE_FENCE_GATE", "BIRCH_FENCE_GATE", "JUNGLE_FENCE_GATE",
            "DARK_OAK_FENCE_GATE", "ACACIA_FENCE_GATE", "SPRUCE_FENCE", "BIRCH_FENCE", "JUNGLE_FENCE", "DARK_OAK_FENCE", "ACACIA_FENCE",
            "END_ROD", "CHORUS_PLANT", "CHORUS_FLOWER", "PURPUR_BLOCK", "PURPUR_PILLAR", "PURPUR_STAIRS", "PURPUR_DOUBLE_SLAB",
            "PURPUR_SLAB", "END_BRICKS", "BEETROOT_BLOCK", "GRASS_PATH", "END_GATEWAY", "COMMAND_REPEATING", "COMMAND_CHAIN",
            "FROSTED_ICE", "MAGMA", "NETHER_WART_BLOCK", "RED_NETHER_BRICK", "BONE_BLOCK", "STRUCTURE_VOID", "OBSERVER",
            "WHITE_SHULKER_BOX", "ORANGE_SHULKER_BOX", "MAGENTA_SHULKER_BOX", "LIGHT_BLUE_SHULKER_BOX", "YELLOW_SHULKER_BOX",
            "LIME_SHULKER_BOX", "PINK_SHULKER_BOX", "GRAY_SHULKER_BOX", "SILVER_SHULKER_BOX", "CYAN_SHULKER_BOX", "PURPLE_SHULKER_BOX",
            "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX", "GREEN_SHULKER_BOX", "RED_SHULKER_BOX", "BLACK_SHULKER_BOX",
            "CONCRETE", "CONCRETE_POWDER", "STRUCTURE_BLOCK",
            "IRON_SPADE", "IRON_PICKAXE", "IRON_AXE", "FLINT_AND_STEEL", "APPLE", "BOW", "ARROW", "COAL", "DIAMOND", "IRON_INGOT",
            "GOLD_INGOT", "IRON_SWORD", "WOOD_SWORD", "WOOD_SPADE", "WOOD_PICKAXE", "WOOD_AXE", "STONE_SWORD", "STONE_SPADE",
            "STONE_PICKAXE", "STONE_AXE", "DIAMOND_SWORD", "DIAMOND_SPADE", "DIAMOND_PICKAXE", "DIAMOND_AXE", "STICK", "BOWL",
            "MUSHROOM_SOUP", "GOLD_SWORD", "GOLD_SPADE", "GOLD_PICKAXE", "GOLD_AXE", "STRING", "FEATHER", "SULPHUR", "WOOD_HOE",
            "STONE_HOE", "IRON_HOE", "DIAMOND_HOE", "GOLD_HOE", "SEEDS", "WHEAT", "BREAD", "LEATHER_HELMET", "LEATHER_CHESTPLATE",
            "LEATHER_LEGGINGS", "LEATHER_BOOTS", "CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS",
            "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS", "DIAMOND_HELMET", "DIAMOND_CHESTPLATE",
            "DIAMOND_LEGGINGS", "DIAMOND_BOOTS", "GOLD_HELMET", "GOLD_CHESTPLATE", "GOLD_LEGGINGS", "GOLD_BOOTS", "FLINT", "PORK",
            "GRILLED_PORK", "PAINTING", "GOLDEN_APPLE", "SIGN", "WOOD_DOOR", "BUCKET", "WATER_BUCKET", "LAVA_BUCKET", "MINECART",
            "SADDLE", "IRON_DOOR", "REDSTONE", "SNOW_BALL", "BOAT", "LEATHER", "MILK_BUCKET", "CLAY_BRICK", "CLAY_BALL", "SUGAR_CANE",
            "PAPER", "BOOK", "SLIME_BALL", "STORAGE_MINECART", "POWERED_MINECART", "EGG", "COMPASS", "FISHING_ROD", "WATCH",
            "GLOWSTONE_DUST", "RAW_FISH", "COOKED_FISH", "INK_SACK", "BONE", "SUGAR", "CAKE", "BED", "DIODE", "COOKIE", "MAP",
            "SHEARS", "MELON", "PUMPKIN_SEEDS", "MELON_SEEDS", "RAW_BEEF", "COOKED_BEEF", "RAW_CHICKEN", "COOKED_CHICKEN",
            "ROTTEN_FLESH", "ENDER_PEARL", "BLAZE_ROD", "GHAST_TEAR", "GOLD_NUGGET", "NETHER_STALK", "POTION", "GLASS_BOTTLE",
            "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "BLAZE_POWDER", "MAGMA_CREAM", "BREWING_STAND", "CAULDRON", "EYE_OF_ENDER",
            "SPECKLED_MELON", "MONSTER_EGG", "EXP_BOTTLE", "FIREBALL", "BOOK_AND_QUILL", "WRITTEN_BOOK", "EMERALD", "ITEM_FRAME",
            "FLOWER_POT", "CARROT_ITEM", "POTATO_ITEM", "BAKED_POTATO", "POISONOUS_POTATO", "EMPTY_MAP", "GOLDEN_CARROT", "SKULL_ITEM",
            "CARROT_STICK", "NETHER_STAR", "PUMPKIN_PIE", "FIREWORK", "FIREWORK_CHARGE", "ENCHANTED_BOOK", "REDSTONE_COMPARATOR",
            "NETHER_BRICK_ITEM", "QUARTZ", "EXPLOSIVE_MINECART", "HOPPER_MINECART", "PRISMARINE_SHARD", "PRISMARINE_CRYSTALS", "RABBIT",
            "COOKED_RABBIT", "RABBIT_STEW", "RABBIT_FOOT", "RABBIT_HIDE", "ARMOR_STAND", "IRON_BARDING", "GOLD_BARDING", "DIAMOND_BARDING",
            "LEASH", "NAME_TAG", "COMMAND_MINECART", "MUTTON", "COOKED_MUTTON", "BANNER", "END_CRYSTAL", "SPRUCE_DOOR_ITEM",
            "BIRCH_DOOR_ITEM", "JUNGLE_DOOR_ITEM", "ACACIA_DOOR_ITEM", "DARK_OAK_DOOR_ITEM", "CHORUS_FRUIT", "CHORUS_FRUIT_POPPED",
            "BEETROOT", "BEETROOT_SEEDS", "BEETROOT_SOUP", "DRAGONS_BREATH", "SPLASH_POTION", "SPECTRAL_ARROW", "TIPPED_ARROW",
            "LINGERING_POTION", "SHIELD", "ELYTRA", "BOAT_SPRUCE", "BOAT_BIRCH", "BOAT_JUNGLE", "BOAT_ACACIA", "BOAT_DARK_OAK",
            "TOTEM", "SHULKER_SHELL", "IRON_NUGGET", "KNOWLEDGE_BOOK",
            "GOLD_RECORD", "GREEN_RECORD", "RECORD_3", "RECORD_4", "RECORD_5", "RECORD_6", "RECORD_7", "RECORD_8", "RECORD_9", "RECORD_10", "RECORD_11", "RECORD_12"
        );

        MC_1_12_2_ENCHANTMENTS = Arrays.asList(
            "ARROW_DAMAGE", "ARROW_FIRE", "ARROW_INFINITE", "ARROW_KNOCKBACK", "DAMAGE_ALL", "DAMAGE_ARTHROPODS", "DAMAGE_UNDEAD",
            "DEPTH_STRIDER", "DIG_SPEED", "DURABILITY", "FIRE_ASPECT", "FROST_WALKER", "KNOCKBACK", "LOOT_BONUS_BLOCKS",
            "LOOT_BONUS_MOBS", "LUCK", "LURE", "MENDING", "OXYGEN", "PROTECTION_ENVIRONMENTAL", "PROTECTION_EXPLOSIONS",
            "PROTECTION_FALL", "PROTECTION_FIRE", "PROTECTION_PROJECTILE", "SILK_TOUCH", "SWEEPING_EDGE", "THORNS",
            "WATER_WORKER", "BINDING_CURSE", "VANISHING_CURSE"
        );

        Collections.sort(MC_1_12_2_MATERIALS);
        Collections.sort(MC_1_12_2_ENCHANTMENTS);
    }
    //</editor-fold>

    private final Map<String, Node> attributeControls = new HashMap<>();
    private final Map<File, Map<String, Map<String, Object>>> loadedFilesData = new LinkedHashMap<>();
    private final Yaml yaml;

    private Stage stage;
    private boolean hasUnsavedChanges = false;
    private File selectedMMOItemsFolder;
    private File currentSelectedYmlFile;
    private MMOItemEntry currentEditingItem;

    private final ChangeListener<Object> unsavedChangesListener = (obs, oldVal, newVal) -> setUnsavedChanges(true);

    public EditorController() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setAllowUnicode(true);
        this.yaml = new Yaml(options);
    }

    @FXML
    private void initialize() {
        setupFileTreeView();
        buildAttributeUI();
        updateButtonsState();
        showEditor(false);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //<editor-fold desc="UI Building and Handlers">
    private void buildAttributeUI() {
        ComboBox<String> materialComboBox = new ComboBox<>(FXCollections.observableArrayList(MC_1_12_2_MATERIALS));
        materialComboBox.setEditable(true);
        addAttributeControl(basicAttributesPane, "material", materialComboBox, 0);
        addAttributeControl(basicAttributesPane, "name", new TextField(), 1);
        addAttributeControl(basicAttributesPane, "tier", new TextField(), 2);

        int rowIndex = 0;
        for (String attr : STATS_ATTRIBUTES) {
            addAttributeControl(statsAttributesPane, attr, new TextField(), rowIndex++);
        }

        rowIndex = 0;
        for (String attr : ADVANCED_ATTRIBUTES_TEXT) {
            addAttributeControl(advancedAttributesPane, attr, new TextField(), rowIndex++);
        }
        for (String attr : ADVANCED_ATTRIBUTES_BOOLEAN) {
            addAttributeControl(advancedAttributesPane, attr, new CheckBox(), rowIndex++);
        }

        addComplexAttributeControl(loreEnchantsPane, "lore");

        VBox enchantsBox = new VBox(5.0);
        enchantsBox.getChildren().add(new Label(String.format("%s (%s):", getTranslation("enchants"), "enchants")));
        TableView<EnchantmentEntry> enchantmentsTable = new TableView<>();
        TableColumn<EnchantmentEntry, String> nameCol = new TableColumn<>("附魔 (Enchantment)");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<EnchantmentEntry, Integer> levelCol = new TableColumn<>("等级 (Level)");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        enchantmentsTable.getColumns().addAll(nameCol, levelCol);
        enchantmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        enchantsBox.getChildren().add(enchantmentsTable);

        HBox enchantButtons = new HBox(5.0);
        Button addEnchantButton = new Button("添加附魔");
        addEnchantButton.setOnAction(e -> handleAddEnchantment(enchantmentsTable));
        Button removeEnchantButton = new Button("移除选中");
        removeEnchantButton.setOnAction(e -> {
            EnchantmentEntry selected = enchantmentsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                enchantmentsTable.getItems().remove(selected);
                setUnsavedChanges(true);
            }
        });
        enchantButtons.getChildren().addAll(addEnchantButton, removeEnchantButton);
        enchantsBox.getChildren().add(enchantButtons);
        loreEnchantsPane.getChildren().add(enchantsBox);
        attributeControls.put("enchants_table", enchantmentsTable);

        for (String attr : COMPLEX_ATTRIBUTES) {
            addComplexAttributeControl(complexAttributesPane, attr);
        }
    }

    private void handleAddEnchantment(TableView<EnchantmentEntry> table) {
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("添加附魔");
        dialog.setHeaderText("选择一个附魔并指定其等级");

        ButtonType okButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> enchantmentComboBox = new ComboBox<>(FXCollections.observableArrayList(MC_1_12_2_ENCHANTMENTS));
        enchantmentComboBox.setPromptText("选择附魔");
        Spinner<Integer> levelSpinner = new Spinner<>(1, 255, 1);
        levelSpinner.setEditable(true);

        grid.add(new Label("附魔:"), 0, 0);
        grid.add(enchantmentComboBox, 1, 0);
        grid.add(new Label("等级:"), 0, 1);
        grid.add(levelSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);
        enchantmentComboBox.valueProperty().addListener((obs, oldVal, newVal) -> okButton.setDisable(newVal == null || newVal.trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(enchantmentComboBox.getValue(), levelSpinner.getValue());
            }
            return null;
        });

        Optional<Pair<String, Integer>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            for (EnchantmentEntry entry : table.getItems()) {
                if (entry.getName().equalsIgnoreCase(pair.getKey())) {
                    showAlert("提示", "该附魔已存在。请先移除旧的条目再添加。");
                    return;
                }
            }
            table.getItems().add(new EnchantmentEntry(pair.getKey(), pair.getValue()));
            setUnsavedChanges(true);
        });
    }

    private void addAttributeControl(GridPane pane, String attributeName, Node control, int rowIndex) {
        String labelText = String.format("%s (%s):", getTranslation(attributeName), attributeName);
        pane.add(new Label(labelText), 0, rowIndex);
        pane.add(control, 1, rowIndex);
        attributeControls.put(attributeName, control);
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).textProperty().addListener(unsavedChangesListener);
        } else if (control instanceof CheckBox) {
            ((CheckBox) control).selectedProperty().addListener(unsavedChangesListener);
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).valueProperty().addListener(unsavedChangesListener);
        }
    }

    private void addComplexAttributeControl(VBox pane, String attributeName) {
        pane.getChildren().add(new Label(String.format("%s (%s):", getTranslation(attributeName), attributeName)));
        TextArea textArea = new TextArea();
        textArea.setPromptText("在此编辑 " + attributeName + " 的YAML内容...");
        textArea.setPrefHeight(120);
        textArea.textProperty().addListener(unsavedChangesListener);
        pane.getChildren().add(textArea);
        attributeControls.put(attributeName, textArea);
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
                 // Revert selection if user cancels.
                 // Platform.runLater is needed to avoid issues with changing selection during a selection change event.
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
            editingItemIdLabel.setText("文件: " + currentSelectedYmlFile.getName());
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


    private void clearEditor() {
        editingItemIdLabel.setText("");
        for (Node control : attributeControls.values()) {
            if (control instanceof TextInputControl) {
                ((TextInputControl) control).clear();
            } else if (control instanceof CheckBox) {
                ((CheckBox) control).setSelected(false);
            } else if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setValue(null);
            } else if (control instanceof TableView) {
                ((TableView<?>) control).getItems().clear();
            }
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
        directoryChooser.setTitle("选择 MMOItems 的 item 文件夹");
        if (selectedMMOItemsFolder != null && selectedMMOItemsFolder.exists()) {
            directoryChooser.setInitialDirectory(selectedMMOItemsFolder);
        }

        File selectedDirectory = directoryChooser.showDialog(statusLabel.getScene().getWindow());
        if (selectedDirectory != null) {
            selectedMMOItemsFolder = selectedDirectory;
            statusLabel.setText("已加载: " + selectedDirectory.getAbsolutePath());
            clearEditor();
            showEditor(false);
            loadAllYmlFiles(selectedDirectory);

            TreeItem<Object> rootItem = new TreeItem<>(selectedDirectory);
            rootItem.setExpanded(true);
            populateTreeView(selectedDirectory, rootItem);
            fileTreeView.setRoot(rootItem);
        }
    }

    private void loadAllYmlFiles(File dir) {
        loadedFilesData.clear();
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadAllYmlFiles(file);
            } else if (file.getName().toLowerCase().endsWith(".yml")) {
                try (FileReader reader = new FileReader(file)) {
                    Object obj = yaml.load(reader);
                    if (obj == null) {
                        loadedFilesData.put(file, new LinkedHashMap<>());
                        continue;
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
                        System.err.println("警告: 文件 " + file.getName() + " 的根节点不是Map，已忽略。");
                    }
                } catch (Exception e) {
                    showAlert("加载错误", "无法读取文件 " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private void populateTreeView(File dir, TreeItem<Object> parent) {
        File[] files = dir.listFiles();
        if (files == null) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            if (file.isDirectory()) {
                TreeItem<Object> item = new TreeItem<>(file);
                parent.getChildren().add(item);
                populateTreeView(file, item);
            } else if (file.getName().toLowerCase().endsWith(".yml")) {
                TreeItem<Object> fileItem = new TreeItem<>(file);
                parent.getChildren().add(fileItem);
                Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(file);
                if (itemsInFile != null) {
                    itemsInFile.keySet().stream().sorted().forEach(itemId -> {
                        fileItem.getChildren().add(new TreeItem<>(new MMOItemEntry(itemId, file)));
                    });
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadItemContent(MMOItemEntry itemEntry) {
        clearEditor();
        editingItemIdLabel.setText(itemEntry.getItemId() + " (文件: " + itemEntry.getParentFile().getName() + ")");
        Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(itemEntry.getParentFile());
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
                } else if (control instanceof TextArea) {
                    ((TextArea) control).setText(yaml.dump(value));
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
        }
        setUnsavedChanges(false);
    }

    @FXML
    private void saveChanges() {
        if (currentEditingItem == null) return;

        Map<String, Object> newItemData = new LinkedHashMap<>();

        for (Map.Entry<String, Node> entry : attributeControls.entrySet()) {
            String attrName = entry.getKey();
            Node control = entry.getValue();

            if (attrName.equals("enchants_table")) continue;

            if (control instanceof CheckBox) {
                if (((CheckBox) control).isSelected()) newItemData.put(attrName, true);
            } else if (control instanceof TextField) {
                String text = ((TextField) control).getText();
                if (text != null && !text.trim().isEmpty()) newItemData.put(attrName, smartConvert(text));
            } else if (control instanceof ComboBox) {
                Object value = ((ComboBox<?>) control).getValue();
                if (value != null && !value.toString().trim().isEmpty()) newItemData.put(attrName, value.toString());
            } else if (control instanceof TextArea) {
                String text = ((TextArea) control).getText();
                if (text != null && !text.trim().isEmpty()) {
                    try {
                        newItemData.put(attrName, yaml.load(text));
                    } catch (Exception e) {
                        showAlert("YAML语法错误", "属性 '" + attrName + "' 中的YAML格式不正确，已忽略。");
                    }
                }
            }
        }

        TableView<EnchantmentEntry> enchantmentsTable = (TableView<EnchantmentEntry>) attributeControls.get("enchants_table");
        if (!enchantmentsTable.getItems().isEmpty()) {
            Map<String, Integer> enchantsMap = new LinkedHashMap<>();
            for (EnchantmentEntry ench : enchantmentsTable.getItems()) {
                enchantsMap.put(ench.getName(), ench.getLevel());
            }
            newItemData.put("enchants", enchantsMap);
        }

        Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(currentEditingItem.getParentFile());
        if (itemsInFile != null) {
            itemsInFile.put(currentEditingItem.getItemId(), newItemData);
            saveAllToFile(currentEditingItem.getParentFile());
            setUnsavedChanges(false);
            showAlert("成功", "物品 '" + currentEditingItem.getItemId() + "' 已成功保存！");
        }
    }

    private void saveAllToFile(File fileToSave) {
        Map<String, Map<String, Object>> data = loadedFilesData.get(fileToSave);
        if (data == null) return;
        try (FileWriter writer = new FileWriter(fileToSave)) {
            if (!data.isEmpty()) {
                yaml.dump(data, writer);
            } else {
                writer.write("");
            }
        } catch (IOException e) {
            showAlert("保存文件错误", "无法保存文件 " + fileToSave.getName() + ": " + e.getMessage());
        }
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
        alert.setTitle("未保存的更改");
        alert.setHeaderText("您有未保存的更改。您想在关闭前保存吗？");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == ButtonType.YES) {
                saveChanges();
                return !hasUnsavedChanges;
            } else return result.get() != ButtonType.CANCEL;
        }
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="Item Management Methods">
    @FXML
    private void handleNewItem() {
        if (currentSelectedYmlFile == null) {
            showAlert("警告", "请先在左侧选择一个 .yml 文件。");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("NEW_ITEM_ID");
        dialog.setTitle("新建物品");
        dialog.setHeaderText("请输入新物品的ID (例如: MY_AWESOME_SWORD)");
        dialog.setContentText("物品ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newId -> {
            newId = newId.trim().toUpperCase().replace(" ", "_");
            if (newId.isEmpty()) {
                showAlert("警告", "物品ID不能为空。");
                return;
            }

            Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(currentSelectedYmlFile);
            if (itemsInFile != null && itemsInFile.containsKey(newId)) {
                showAlert("警告", "物品ID '" + newId + "' 已存在。");
                return;
            }

            Map<String, Object> newItemData = new LinkedHashMap<>();
            newItemData.put("material", "STONE");
            newItemData.put("name", "&f" + newId.replace("_", " "));

            if (itemsInFile == null) {
                itemsInFile = new LinkedHashMap<>();
                loadedFilesData.put(currentSelectedYmlFile, itemsInFile);
            }
            itemsInFile.put(newId, newItemData);

            TreeItem<Object> fileTreeItem = findTreeItem(fileTreeView.getRoot(), currentSelectedYmlFile);
            if (fileTreeItem != null) {
                MMOItemEntry newEntry = new MMOItemEntry(newId, currentSelectedYmlFile);
                TreeItem<Object> newTreeItem = new TreeItem<>(newEntry);
                fileTreeItem.getChildren().add(newTreeItem);
                fileTreeItem.getChildren().sort(Comparator.comparing(t -> t.getValue().toString()));
                fileTreeView.getSelectionModel().select(newTreeItem);
            }

            saveAllToFile(currentSelectedYmlFile);
            setUnsavedChanges(false);
            showAlert("成功", "物品 '" + newId + "' 已创建并保存。");
        });
    }

    @FXML
    private void handleDuplicateItem() {
        if (currentEditingItem == null) return;

        TextInputDialog dialog = new TextInputDialog(currentEditingItem.getItemId() + "_COPY");
        dialog.setTitle("复制物品");
        dialog.setHeaderText("请输入新物品的ID");
        dialog.setContentText("新物品ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newId -> {
            newId = newId.trim().toUpperCase().replace(" ", "_");
            if (newId.isEmpty()) {
                showAlert("警告", "物品ID不能为空。");
                return;
            }

            File parentFile = currentEditingItem.getParentFile();
            Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(parentFile);

            if (itemsInFile != null && itemsInFile.containsKey(newId)) {
                showAlert("警告", "物品ID '" + newId + "' 已存在。");
                return;
            }

            Map<String, Object> originalItemData = itemsInFile.get(currentEditingItem.getItemId());
            Map<String, Object> duplicatedItemData = deepCopyMap(originalItemData);

            itemsInFile.put(newId, duplicatedItemData);

            TreeItem<Object> fileTreeItem = findTreeItem(fileTreeView.getRoot(), parentFile);
            if (fileTreeItem != null) {
                MMOItemEntry newEntry = new MMOItemEntry(newId, parentFile);
                TreeItem<Object> newTreeItem = new TreeItem<>(newEntry);
                fileTreeItem.getChildren().add(newTreeItem);
                fileTreeItem.getChildren().sort(Comparator.comparing(t -> t.getValue().toString()));
                fileTreeView.getSelectionModel().select(newTreeItem);
            }

            saveAllToFile(parentFile);
            setUnsavedChanges(false);
            showAlert("成功", "物品已成功复制为 '" + newId + "'。");
        });
    }

    @FXML
    private void handleDeleteItem() {
        if (currentEditingItem == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("您确定要删除物品 '" + currentEditingItem.getItemId() + "' 吗？");
        alert.setContentText("此操作不可逆！");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            File parentFile = currentEditingItem.getParentFile();
            String itemIdToRemove = currentEditingItem.getItemId();

            Map<String, Map<String, Object>> itemsInFile = loadedFilesData.get(parentFile);
            if (itemsInFile != null) {
                itemsInFile.remove(itemIdToRemove);

                TreeItem<Object> fileTreeItem = findTreeItem(fileTreeView.getRoot(), parentFile);
                if (fileTreeItem != null) {
                    fileTreeItem.getChildren().removeIf(item -> item.getValue().equals(currentEditingItem));
                }

                clearEditor();
                showEditor(false);
                saveAllToFile(parentFile);
                setUnsavedChanges(false);
                showAlert("成功", "物品 '" + itemIdToRemove + "' 已删除。");
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    private String getTranslation(String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }

    private TreeItem<Object> findTreeItem(TreeItem<Object> root, Object value) {
        if (root.getValue().equals(value)) return root;
        for (TreeItem<Object> child : root.getChildren()) {
            TreeItem<Object> found = findTreeItem(child, value);
            if (found != null) return found;
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Object smartConvert(String text) {
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) { /* ignore */ }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) { /* ignore */ }
        return text;
    }

    private Map<String, Object> deepCopyMap(Map<String, Object> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private List<Object> deepCopyList(List<Object> original) {
        List<Object> copy = new ArrayList<>();
        for (Object item : original) {
            copy.add(deepCopyValue(item));
        }
        return copy;
    }
    
    private Object deepCopyValue(Object value) {
        if (value instanceof Map) {
            return deepCopyMap((Map<String, Object>) value);
        } else if (value instanceof List) {
            return deepCopyList((List<Object>) value);
        }
        return value;
    }
    //</editor-fold>
}
