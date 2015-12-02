package com.sap.sse.gwt.client.celltable;

import java.util.Set;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * TODO Lukas: Add Javadoc
 */

public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T> {
    private final EntityIdentityComparator<T> comp;
    
    /**
     * TODO Lukas: Add Javadoc
     */
    public RefreshableSingleSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp=comp;
    }
    /**
     * TODO Lukas: Add Javadoc
     */
    public RefreshableSingleSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp =comp;
    }

    @Override
    public EntityIdentityComparator<T> getEntityIdentityComparator() {
        return comp;
    }
    
    @Override
    public void setSelected(T item, boolean selected) {
        if (comp == null) {
            super.setSelected(item, selected);
        } else {
            T wasSelectedBefore = null;
            Set<T> selectedSet = getSelectedSet();
            for (T it : selectedSet) {
                if(comp.representSameEntity(it, item)) {
                    wasSelectedBefore = it;
                    break;
                }
            }
            if(wasSelectedBefore == null) {
                super.setSelected(wasSelectedBefore, false); //This old version of item will be deleted with the next clear()
                super.setSelected(item, selected);
            } else {
                super.setSelected(item, selected);
            }
        }
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