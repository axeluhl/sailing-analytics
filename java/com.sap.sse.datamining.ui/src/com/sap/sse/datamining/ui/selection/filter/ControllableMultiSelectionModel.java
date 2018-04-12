package com.sap.sse.datamining.ui.selection.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * A multi selection model, that allows to block the selection change notifications.
 * @author Lennart Hensler (D054527)
 */
public class ControllableMultiSelectionModel<T> extends MultiSelectionModel<T> {

    // Ensure one value per key
    private final Map<Object, T> selectedSet;
    /**
     * A map of keys to the item and its pending selection state.
     */
    private final Map<Object, SelectionChange<T>> selectionChanges;
    
    private boolean blockNotifications;
    
    /**
     * Constructs a MultiSelectionModel without a key provider.
     */
    public ControllableMultiSelectionModel() {
        this(null);
    }

    /**
     * Constructs a MultiSelectionModel with the given key provider.
     * 
     * @param keyProvider
     *            an instance of ProvidesKey<T>, or null if the item should act as its own key
     */
    public ControllableMultiSelectionModel(ProvidesKey<T> keyProvider) {
        super(keyProvider);
        this.selectedSet = new HashMap<Object, T>();
        this.selectionChanges = new HashMap<Object, SelectionChange<T>>();
        blockNotifications = false;
    }
    
    public void setBlockNotifications(boolean blockNotifications) {
        this.blockNotifications = blockNotifications;
    }

    /**
     * Deselect all selected values.
     */
    @Override
    public void clear() {
        // Clear the current list of pending changes.
        selectionChanges.clear();

        /*
         * Add a pending change to deselect each key that is currently selected. We cannot just clear the selected set,
         * because then we would not know which keys were selected before we cleared, which we need to know to determine
         * if we should fire an event.
         */
        for (T value : selectedSet.values()) {
            selectionChanges.put(getKey(value), new SelectionChange<T>(value, false));
        }
        scheduleSelectionChangeEvent();
    }

    /**
     * Get the set of selected items as a copy. If multiple selected items share the same key, only the last selected
     * item is included in the set.
     * 
     * @return the set of selected items
     */
    @Override
    public Set<T> getSelectedSet() {
        determineChanges();
        return new HashSet<T>(selectedSet.values());
    }

    @Override
    public boolean isSelected(T item) {
        determineChanges();
        return selectedSet.containsKey(getKey(item));
    }

    @Override
    public void setSelected(T item, boolean selected) {
        selectionChanges.put(getKey(item), new SelectionChange<T>(item, selected));
        scheduleSelectionChangeEvent();
    }
    
    @Override
    protected void scheduleSelectionChangeEvent() {
        if (!blockNotifications) {
            super.scheduleSelectionChangeEvent();
        } else {
            determineChanges();
        }
    }

    @Override
    protected void fireSelectionChangeEvent() {
        if (isEventScheduled()) {
            setEventCancelled(true);
        }
        determineChanges();
    }

    private void determineChanges() {
        if (selectionChanges.isEmpty()) {
            return;
        }

        boolean changed = false;
        for (Map.Entry<Object, SelectionChange<T>> entry : selectionChanges.entrySet()) {
            Object key = entry.getKey();
            SelectionChange<T> value = entry.getValue();
            boolean selected = value.isSelected();

            T oldValue = selectedSet.get(key);
            if (selected) {
                selectedSet.put(key, value.getItem());
                Object oldKey = getKey(oldValue);
                if (!changed) {
                    changed = (oldKey == null) ? (key != null) : !oldKey.equals(key);
                }
            } else {
                if (oldValue != null) {
                    selectedSet.remove(key);
                    changed = true;
                }
            }
        }
        selectionChanges.clear();

        // Fire a selection change event.
        if (changed && !blockNotifications) {
            SelectionChangeEvent.fire(this);
        }
    }

    /**
     * Stores an item and its pending selection state.
     * @param <T> the data type of the item
     */
    private static class SelectionChange<T> {
        private final T item;
        private final boolean isSelected;

        SelectionChange(T item, boolean isSelected) {
            this.item = item;
            this.isSelected = isSelected;
        }

        public T getItem() {
            return item;
        }

        public boolean isSelected() {
            return isSelected;
        }
    }

}