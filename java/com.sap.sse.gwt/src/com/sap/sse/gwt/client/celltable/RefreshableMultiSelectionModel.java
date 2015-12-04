package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * This {@link RefreshableMultiSelectionModel} have the property that it is refreshable. That means if the
 * {@link CellTable} have new Elements you can call
 * {@link RefreshableMultiSelectionModel#refreshSelectionModel(Iterable) refreshSelectionModel(Iterable)} and the
 * elements with {@link EntityIdentityComparator#representSameEntity(Object, Object) the same identity} will be
 * reselected.
 * 
 * @param <T>
 *            the type of {@link CellTable} entries
 * 
 *            TODO /FIXME Describe interaction with ListDataProvider
 */
public class RefreshableMultiSelectionModel<T> extends MultiSelectionModel<T>
        implements RefreshableSelectionModel<T>, HasData<T> {
    private final EntityIdentityComparator<T> comp;
    private final List<T> elements;
    private boolean dontcheckSelectionState = false;

    /**
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     */
    public RefreshableMultiSelectionModel(EntityIdentityComparator<T> comp) {
        super();
        this.comp = comp;
        elements = new ArrayList<>();
    }

    /**
     * @param keyProvider
     *            {@link ProvidesKey} for the super class constructor
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     */
    public RefreshableMultiSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp) {
        super(keyProvider);
        this.comp = comp;
        elements = new ArrayList<>();
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
        if (comp == null || dontcheckSelectionState) {
            super.setSelected(item, selected);
        } else {
            T wasSelectedBefore = null;
            Set<T> selectedSet = getSelectedSet();
            for (T it : selectedSet) {
                if (comp.representSameEntity(it, item)) {
                    wasSelectedBefore = it;
                    break;
                }
            }
            if (wasSelectedBefore != null) {
                super.setSelected(wasSelectedBefore, false);
                isSelected(item); //triggers the deleting of the wasSelectedBefrore element in super class
                super.setSelected(item, selected);
            } else {
                super.setSelected(item, selected);
            }
        }
    }

    /**
     * refreshes the {@link RefreshableMultiSelectionModel SelectionModel} with all elements of the {@link ListDataProvider}
     * <p>
     * FIXME change to private
     */
    public void refreshSelectionModel(Iterable<T> newObjects) {
        dontcheckSelectionState = true;
        final Set<T> selectedSet = getSelectedSet();
        final boolean isNotEmpty = !selectedSet.isEmpty();
        if (isNotEmpty) {
            clear();
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
        dontcheckSelectionState = false;
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
        dontcheckSelectionState = true;
        List<T> oldElements = elements.subList(start, elements.size());
        Set<T> selectedElements = new HashSet<T>();
        for (T it : oldElements) {
            if (isSelected(it)) {
                selectedElements.add(it);
                setSelected(it, false);
            }
        }
        if (!selectedElements.isEmpty()) {
            for (T newElement : newObjects) {
                if (comp != null) {
                    for (T selected : selectedElements) {
                        if (comp.representSameEntity(selected, newElement)) {
                            setSelected(newElement, true);
                            break;
                        }
                    }
                } else {
                    if (selectedElements.contains(newElement)) {
                        setSelected(newElement, true);
                    }
                }
            }
            SelectionChangeEvent.fire(this);
        }
        isSelected(newObjects.iterator().next()); // Triggers a clear of unselected elements in superclass
        dontcheckSelectionState = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setRowData(int start, List<? extends T> values) {
        if (!values.equals(elements.subList(start, elements.size()))) {
            // refresh the selectionModel
            if (start == 0 && (values.size() > 1 || values.size()==0)) {
                refreshSelectionModel((Iterable<T>) values);
            } else {
                refreshSelectionModel(start, (List<T>) values);
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