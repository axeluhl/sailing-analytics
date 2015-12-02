package com.sap.sse.gwt.client.celltable;

import java.util.Set;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * TODO Lukas: Add Javadoc
 *
 * @param <T>
 */
public class RefreshableMultiSelectionModel<T> extends MultiSelectionModel<T> implements RefreshableSelectionModel<T> {
    private final EntityIdentityComparator<T> comp;

    /**
     * TODO Lukas: Add Javadoc
     */
    public RefreshableMultiSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp = comp;
    }
    
    /**
     * TODO Lukas: Add Javadoc
     */
    public RefreshableMultiSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp = comp;
    }

    @Override
    public EntityIdentityComparator<T> getEntityIdentityComparator() {
        return comp;
    }

    /*
     * TODO / FIXME: Need to redefine setSelected here: if an element is set selected for which another element is in
     * the current selection that is compared equal by the EntityIdentityComparator then it should be replaced
     */
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