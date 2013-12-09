package com.sap.sailing.gwt.ui.client;

import java.util.List;

public interface SelectionProvider<T> {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<T> getSelectedItems();

    void addSelectionChangeListener(SelectionChangeListener<T> listener);

    void removeSelectionChangeListener(SelectionChangeListener<T> listener);
    
    void setSelection(List<T> newSelection, SelectionChangeListener<T>... listenersNotToNotify);

    boolean hasMultiSelection();

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<T> getAllItems();
    
    void setAllItems(List<T> newAllItems, SelectionChangeListener<T>... listenersNotToNotify);
}
