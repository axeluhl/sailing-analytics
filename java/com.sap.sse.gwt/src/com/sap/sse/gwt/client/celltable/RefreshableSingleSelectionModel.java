package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;

/**
 * TODO Lukas: Add Javadoc
 * 
 * *            TODO /FIXME Describe interaction with ListDataProvider
 */

public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T>, HasData<T> {
    private final EntityIdentityComparator<T> comp;
    private final List<T> elements;
    
    /**
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     */
    public RefreshableSingleSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp=comp;
        elements = new ArrayList<>();
    }
    
    /**
     * @param keyProvider
     *            {@link ProvidesKey} for the super class constructor
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     */
    public RefreshableSingleSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp =comp;
        elements = new ArrayList<>();
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
                    super.setSelected(it, true);
                    break;
                }
            }
        }
        SelectionChangeEvent.fire(this);
    }
    
    /**
     * refreshes the {@link RefreshableMultiSelectionModel SelectionModel}-Part from <code>start</code> to
     * <code>elements.size()</code>
     * 
     * @param start
     * @param newObjects
     */
    @Override
    public void refreshSelectionModel(int start, List<T> newObjects) {
        List<T> oldElements = elements.subList(start, elements.size());
        T selectedElement = null;
        for (T it : oldElements) {
            if (isSelected(it)) {
                selectedElement = it;
                super.setSelected(it, false);
                break;
            }
        }
        if (selectedElement != null) {
            if (comp != null) {
                for (T newElement : newObjects) {
                    if (comp.representSameEntity(selectedElement, newElement)) {
                        super.setSelected(newElement, true);
                        break;
                    }
                }
            } else if (newObjects.contains(selectedElement)) {
                super.setSelected(newObjects.get(newObjects.indexOf(selectedElement)), true);
            }
            SelectionChangeEvent.fire(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setRowData(int start, List<? extends T> values) {
        // refresh the selectionModel
        if (start > 0 && values.size() > 0) {
            refreshSelectionModel(start, (List<T>) values);
        } else {
            refreshSelectionModel((Iterable<T>) values);
        }
        // refresh the element list
        // TODO discuss with Axel if this depends to much of the implementation of ListDataProvider
        if (values.isEmpty()) {
            elements.clear();
        } else if (elements.isEmpty()) {
            elements.addAll(values);
        } else if (values.size() == 1) {
            elements.set(start, values.get(0));
        } else if (start == 0) {
            elements.clear();
            elements.addAll(values);
        } else {
            for (int i = start; i < elements.size(); i++) {
                elements.remove(i);
            }
            elements.addAll(values);
        }
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