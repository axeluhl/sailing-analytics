/*package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SelectionProviderImpl<T> implements SelectionProvider<T> {
    private final List<T> selection;
    
    private final List<T> allItems;
    
    private final Set<SelectionChangeListener<T>> listeners;
    
    private final boolean hasMultiSelection;

    public SelectionProviderImpl(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        this.selection = new ArrayList<T>();
        this.allItems = new ArrayList<T>();
        listeners = new HashSet<SelectionChangeListener<T>>();
    }

    @Override
    public List<T> getSelectedItems() {
        return Collections.unmodifiableList(selection);
    }

    @Override
    public void addSelectionChangeListener(SelectionChangeListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeSelectionChangeListener(SelectionChangeListener<T> listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void setSelection(List<T> newSelection) {
        setSelection(newSelection, null);
    }

    @Override
    public void setSelection(List<T> newSelection, SelectionChangeListener<T>[] listenersNotToNotify) {
        boolean notify = !selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    private void notifyListeners(SelectionChangeListener<T>[] listenersNotToNotify) {
        List<T> selectedItems = getSelectedItems();
        for (SelectionChangeListener<T> listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onSelectionChange(selectedItems);
            }
        }
    }
    
    *//**
     * Sets the universe of all items from which this selection model may select. An equal list will subsequently be
     * returned by {@link #getAllItems()}. Items from the {@link #getSelectedItems() selection} not in
     * <code>newAllItems</code> are removed from the selection. If this happens, the selection listeners are notified.
     *//*
    @Override
    public void setAllItems(List<T> newAllItems) {
        allItems.clear();
        for (T r : newAllItems) {
            allItems.add(r);
        }
        for (Iterator<T> i = selection.iterator(); i.hasNext(); ) {
            T selectedItem = i.next();
            if (!allItems.contains(selectedItem)) {
                i.remove();
            }
        }
        // when setting all items, the underlying items will usually have changed their identity and maybe also their state;
        // so notifying the selection listeners is necessary anyhow
        notifyListeners(null);
    }

    @Override
    public List<T> getAllItems() {
        return Collections.unmodifiableList(allItems);
    }

    @Override
    public boolean hasMultiSelection() {
        return hasMultiSelection;
    }
}
*/