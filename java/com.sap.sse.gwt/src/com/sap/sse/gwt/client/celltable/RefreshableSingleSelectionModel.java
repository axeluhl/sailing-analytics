package com.sap.sse.gwt.client.celltable;

import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T> {
    private final EntityIdentityComparator<T> comp;
    
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
        final T selected = getSelectedObject();
        clear();
        if (selected != null) {
            for (final T it : newObjects) {
                boolean isEqual = comp == null ? selected.equals(it) : comp.representSameEntity(selected, it);
                if (isEqual) {
                    setSelected(it, true);
                }
            }
        }
        SelectionChangeEvent.fire(this);
    }
}