package com.sap.sse.gwt.client.celltable;

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
    public void setHasEqualIdentity(EntityIdentityComparator<T> comp) {
        this.comp = comp;
    }

    @Override
    public void refreshSelectionModel(Iterable<T> newObjects) {
        Set<T> selectedSet = getSelectedSet();
        EntityIdentityComparator<T> comp = getEntityIdentityComparator();
        clear();
        if (comp != null) {
            for (T it : newObjects) {
                for (T selected : selectedSet) {
                    if (comp.representSameEntity(selected, it)) {
                        setSelected(it, true);
                    } else {
                        setSelected(it, false);
                    }
                }
            }
        } else {
            for (T it : newObjects) {
                for (T selected : selectedSet) {
                    if (selected.equals(it)) {
                        setSelected(it, true);
                    } else {
                        setSelected(it, false);
                    }
                }
            }
        }
    }
}
