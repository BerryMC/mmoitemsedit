package com.yourname.mmoitemseditor.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CraftingIngredient {
    private final StringProperty material;
    private final IntegerProperty amount;

    public CraftingIngredient(String material, int amount) {
        this.material = new SimpleStringProperty(material);
        this.amount = new SimpleIntegerProperty(amount);
    }

    public String getMaterial() {
        return material.get();
    }

    public StringProperty materialProperty() {
        return material;
    }

    public void setMaterial(String material) {
        this.material.set(material);
    }

    public int getAmount() {
        return amount.get();
    }

    public IntegerProperty amountProperty() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }
}
