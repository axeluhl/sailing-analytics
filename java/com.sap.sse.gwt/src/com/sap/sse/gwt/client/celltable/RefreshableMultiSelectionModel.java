package com.sap.sse.gwt.client.celltable;

import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;

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
        final Set<T> selectedSet = getSelectedSet();
        final boolean isNotEmpty = !selectedSet.isEmpty();
        clear();
        if (isNotEmpty) {
            for (T it : newObjects) {
                if (comp == null) {
                    setSelected(it, selectedSet.contains(it));
                } else {
                    boolean isSelected = false;
                    for (T selected : selectedSet) {
                        isSelected = comp.representSameEntity(selected, it);
                        if (isSelected) {
                            break;
                        }
                    }
                    setSelected(it, isSelected);
                }
            }
            SelectionChangeEvent.fire(this);
        }
    }
}