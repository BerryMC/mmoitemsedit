package com.yourname.mmoitemseditor.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PotionEffectEntry {

    private final SimpleStringProperty name;
    private final SimpleIntegerProperty level;

    public PotionEffectEntry(String name, Integer level) {
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(level);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getLevel() {
        return level.get();
    }

    public SimpleIntegerProperty levelProperty() {
        return level;
    }

    public void setLevel(int level) {
        this.level.set(level);
    }
}
