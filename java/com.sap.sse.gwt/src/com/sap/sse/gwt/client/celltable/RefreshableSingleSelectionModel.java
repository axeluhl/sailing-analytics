package com.sap.sse.gwt.client.celltable;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;

public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T> {
    private EntityIdentityComparator<T> comp;
    
    public RefreshableSingleSelectionModel() {
         super();
         comp = null;
     }
    
    public RefreshableSingleSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp=comp;
    }
    
    public RefreshableSingleSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp =comp;
    }

    @Override
    public EntityIdentityComparator<T> getEntityIdentityComparator() {
        return comp;
    }

    @Override
    public void refreshSelectionModel(Iterable<T> newObjects) {
        final Set<T> selectedSet = new HashSet<>(getSelectedSet());
        final T selected = selectedSet.isEmpty() ? null : selectedSet.iterator().next();
        clear();
        for (final T it : newObjects) {
            if (selected != null) {
                setSelected(it, comp == null ? selected.equals(it) : comp.representSameEntity(selected, it));
            } else {
                setSelected(it, false);
            }
        }
        scheduleSelectionChangeEvent();
    }
}