package com.yourname.mmoitemseditor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbilityEntry {
    private final StringProperty id;
    private final StringProperty type;
    private final StringProperty mode;
    private final StringProperty summary;
    private Map<String, Object> modifiers;

    // A list of common modifiers to display in the summary, in order of importance.
    private static final List<String> SUMMARY_MODIFIERS = Arrays.asList(
        "damage", "duration", "cooldown", "amplifier", "radius", "mana", "stamina"
    );

    public AbilityEntry(String id, String type, String mode, Map<String, Object> modifiers) {
        this.id = new SimpleStringProperty(id);
        this.type = new SimpleStringProperty(type);
        this.mode = new SimpleStringProperty(mode);
        this.summary = new SimpleStringProperty();
        this.modifiers = modifiers;
        updateSummary();
    }

    public void updateSummary() {
        String summaryText = SUMMARY_MODIFIERS.stream()
            .filter(modifiers::containsKey)
            .map(key -> {
                String value = modifiers.get(key).toString();
                // Capitalize first letter of the key for display
                String formattedKey = key.substring(0, 1).toUpperCase() + key.substring(1);
                return formattedKey + ": " + value;
            })
            .collect(Collectors.joining(", "));
        this.summary.set(summaryText);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getMode() {
        return mode.get();
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode.set(mode);
    }

    public String getSummary() {
        return summary.get();
    }

    public StringProperty summaryProperty() {
        return summary;
    }

    public Map<String, Object> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Map<String, Object> modifiers) {
        this.modifiers = modifiers;
        updateSummary(); // Update summary when modifiers change
    }
}