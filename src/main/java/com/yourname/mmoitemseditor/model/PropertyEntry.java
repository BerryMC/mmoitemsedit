package com.yourname.mmoitemseditor.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class PropertyEntry {
    private final SimpleStringProperty key;
    private final SimpleObjectProperty<Object> value;

    public PropertyEntry(String key, Object value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleObjectProperty<>(value);
    }

    public String getKey() {
        return key.get();
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public Object getValue() {
        return value.get();
    }

    public SimpleObjectProperty<Object> valueProperty() {
        return value;
    }

    public void setValue(Object value) {
        this.value.set(value);
    }
}
