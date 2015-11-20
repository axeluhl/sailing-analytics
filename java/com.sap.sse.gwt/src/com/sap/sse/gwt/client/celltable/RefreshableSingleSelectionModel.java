package com.sap.sse.gwt.client.celltable;

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
    public void setHasEqualIdentity(EntityIdentityComparator<T> comp) {
        this.comp = comp;
        // TODO remove
    }

    @Override
    public void refreshSelectionModel(Iterable<T> newObjects) {
        final Set<T> selectedSet = getSelectedSet();
        final EntityIdentityComparator<T> comp = getEntityIdentityComparator();
        clear();
        for (final T it : newObjects) {
            for (final T selected : selectedSet) {
                setSelected(it, comp == null ? selected.equals(it) : comp.representSameEntity(selected, it));
            }
        }
    }
}
