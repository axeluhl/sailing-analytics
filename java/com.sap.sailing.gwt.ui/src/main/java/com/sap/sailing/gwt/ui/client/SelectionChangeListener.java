package com.sap.sailing.gwt.ui.client;

import java.util.List;

/**
 * Allows UI components to observe a selector, such as a drop-down box showing a list of items.
 * 
 * 
 */
public interface SelectionChangeListener<T> {

    /**
     * The first element is the first one selected
     * 
     * @param selectedItems a non-<code>null</code> list which is empty if nothing is selected
     */
    void onSelectionChange(List<T> selectedItems);

}
