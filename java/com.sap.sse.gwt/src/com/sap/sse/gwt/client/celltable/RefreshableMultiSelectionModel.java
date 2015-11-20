package com.sap.sse.gwt.client.celltable;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class RefreshableMultiSelectionModel<T> extends MultiSelectionModel<T> implements RefreshableSelectionModel<T> {
    private EntityIdentityComparator<T> comp;
    
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
        final Set<T> selectedSet = new HashSet<>(getSelectedSet());
        final boolean isSelected = !selectedSet.isEmpty();
        clear();
        for (final T it : newObjects) {
            if (isSelected) {
                for (final T selected : selectedSet) {
                    setSelected(it, comp == null ? selected.equals(it) : comp.representSameEntity(selected, it));
                }
            } else {
                setSelected(it, false);
            }
        }
        scheduleSelectionChangeEvent();
    }
}