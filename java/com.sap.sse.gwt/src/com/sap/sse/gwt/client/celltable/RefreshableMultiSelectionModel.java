package com.sap.sse.gwt.client.celltable;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class RefreshableMultiSelectionModel<T> extends MultiSelectionModel<T> implements RefreshableSelectionModel<T> {
    private final EntityIdentityComparator<T> comp;
    
    public RefreshableMultiSelectionModel() {
        super();
        comp = null;
    }
    
    public RefreshableMultiSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp = comp;
    }
    
    public RefreshableMultiSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp = comp;
    }

    @Override
    public EntityIdentityComparator<T> getEntityIdentityComparator() {
        return comp;
    }

    @Override
    public void refreshSelectionModel(Iterable<T> newObjects) {
        final Set<T> selectedSet = new HashSet<>(getSelectedSet()); // TODO Lukas: a copy doesn't seem required (anymore?) here; the result of getSelectedSet() is not referenced beyond the scope of this method
        final boolean isNotEmpty = !selectedSet.isEmpty();
        clear();
        if (isNotEmpty) {
            for (T it : newObjects) {
                boolean isSelected = false;
                for (T selected : selectedSet) {
                    isSelected = (comp == null ? selected.equals(it) : comp.representSameEntity(selected, it));
                    if (isSelected) {
                        break;
                    }
                }
                setSelected(it, isSelected);
            }
            scheduleSelectionChangeEvent();
        }
    }
}