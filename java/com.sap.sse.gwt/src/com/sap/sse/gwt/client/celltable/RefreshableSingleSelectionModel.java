package com.sap.sse.gwt.client.celltable;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;

/**
 * TODO Lukas: Add Javadoc
 */

public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T>, HasData<T> {
    private final EntityIdentityComparator<T> comp;
    private final ListDataProvider<T> provider;
    
    /**
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     * @param provider
     *            {@link ListDataProvider} to register this as listener and get the entries
     */
    public RefreshableSingleSelectionModel(EntityIdentityComparator<T> comp, ListDataProvider<T> provider) {
        super();
        this.comp=comp;
        this.provider = provider;
        if (this.provider != null) {
            this.provider.addDataDisplay(this);            
        }
    }
    
    /**
     * @param keyProvider
     *            {@link ProvidesKey} for the super class constructor
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     * @param provider
     *            {@link ListDataProvider} to register this as listener and get the entries
     */
    public RefreshableSingleSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp, ListDataProvider<T> provider) {
        super(keyProvider);
        this.comp =comp;
        this.provider = provider;
        if (this.provider != null) {
            this.provider.addDataDisplay(this);  
        }
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
            if(comp.representSameEntity(getSelectedObject(), item)) {
                super.setSelected(getSelectedObject(), false); //This old version of item will be deleted with the next clear()
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
                    break;
                }
            }
        }
        SelectionChangeEvent.fire(this);
    }
    
    @Override
    public void setRowData(int start, List<? extends T> values) {
        refreshSelectionModel(provider.getList());
    }
    
    @Override
    public int getRowCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Range getVisibleRange() {
        return new Range(0, Integer.MAX_VALUE);
    }

    @Override
    public int getVisibleItemCount() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        return null;
    }
    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        return null;
    }
    @Override
    public boolean isRowCountExact() {
        return false;
    }
    @Override
    public void setRowCount(int count) {
        return;
    }
    @Override
    public void setRowCount(int count, boolean isExact) {
        return;
    }
    @Override
    public void setVisibleRange(int start, int length) {
        return;
    }
    @Override
    public void setVisibleRange(Range range) {
        return;
    }
    @Override
    public HandlerRegistration addCellPreviewHandler(com.google.gwt.view.client.CellPreviewEvent.Handler<T> handler) {
        return null;
    }
    @Override
    public SelectionModel<? super T> getSelectionModel() {
        return null;
    }
    @Override
    public T getVisibleItem(int indexOnPage) {
        return null;
    }
    @Override
    public Iterable<T> getVisibleItems() {
        return null;
    }
    @Override
    public void setSelectionModel(SelectionModel<? super T> selectionModel) {
        return;
    }
    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        clear();
    }
}