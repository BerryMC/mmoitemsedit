package com.yourname.mmoitemseditor.controller;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

/**
 * An interface for custom attribute editors to allow them to be treated uniformly.
 */
public interface ComplexAttributeEditor {

    /**
     * Loads the data from the item's YAML into the editor's UI.
     * @param data The data object, typically a Map or a List.
     */
    void loadData(Object data);

    /**
     * Retrieves the edited data from the editor's UI in a format ready for YAML serialization.
     * @return A Map or List representing the edited data.
     */
    Object saveData();

    /**
     * Returns the root JavaFX Node of the editor.
     * @return The root Node.
     */
    Node getRoot();

    /**
     * Allows the main controller to listen for changes within this editor.
     * @param listener The listener to add.
     */
    void setUnsavedChangesListener(ChangeListener<Object> listener);
    
    /**
     * Clears all data from the editor UI.
     */
    void clear();
}
