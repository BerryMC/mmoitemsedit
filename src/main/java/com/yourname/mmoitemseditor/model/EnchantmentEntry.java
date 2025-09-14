package com.yourname.mmoitemseditor.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EnchantmentEntry {
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty level;

    public EnchantmentEntry(String name, int level) {
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(level);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public int getLevel() {
        return level.get();
    }

    public SimpleIntegerProperty levelProperty() {
        return level;
    }
}
