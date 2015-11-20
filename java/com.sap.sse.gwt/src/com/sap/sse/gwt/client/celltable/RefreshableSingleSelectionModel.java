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
    public EntityIdentityComparator<T> getHasEqualIdentity() {
        return comp;
    }

    @Override
    public void setHasEqualIdentity(EntityIdentityComparator<T> comp) {
        this.comp = comp;       
    }

    @Override
    public void refreshSelectionModel(Iterable<T> newObjects) {
        Set<T> selectedSet = getSelectedSet();
        EntityIdentityComparator<T> comp = getHasEqualIdentity();
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
