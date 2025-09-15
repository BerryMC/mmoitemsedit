package com.yourname.mmoitemseditor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ModifierEntry {
    private final StringProperty modifier;
    private final StringProperty value;

    public ModifierEntry(String modifier, String value) {
        this.modifier = new SimpleStringProperty(modifier);
        this.value = new SimpleStringProperty(value);
    }

    public String getModifier() {
        return modifier.get();
    }

    public StringProperty modifierProperty() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier.set(modifier);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}