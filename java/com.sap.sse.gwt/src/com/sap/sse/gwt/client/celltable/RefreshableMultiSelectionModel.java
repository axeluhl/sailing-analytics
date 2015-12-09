package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * This {@link RefreshableMultiSelectionModel} implements the {@link RefreshableSelectionModel} interface. So it
 * register it self as a display on the {@link ListDataProvider} and reacts on the changes of {@link ListDataProvider}.
 * When the {@link ListDataProvider} is changed this {@link RefreshableMultiSelectionModel selectionmodel} will refresh
 * the selection according to the {@link ListDataProvider} changes. To make this class work correct it is very important
 * to set the {@link ListDataProvider}, otherwise it won´t work.
 * <p>
 * For more details on the update process read the {@link RefreshableSelectionModel} Javadoc and see the methods
 * {@link RefreshableMultiSelectionModel#refreshSelectionModel(Iterable)} and
 * {@link RefreshableMultiSelectionModel#setRowData(int, List)}.
 * 
 * @author D064976
 * @param <T>
 *            the type of entries
 */
public class RefreshableMultiSelectionModel<T> extends MultiSelectionModel<T>
        implements RefreshableSelectionModel<T>, HasData<T> {
    private final EntityIdentityComparator<T> comp;
    private boolean dontcheckSelectionState = false;
    private final ListDataProvider<T> listDataProvider;

    /**
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     * @param listDataProvider
     *            {@link ListDataProvider} to add this {@link RefreshableSingleSelectionModel selectionmodel} as an
     *            display on {@link ListDataProvider}
     */
    public RefreshableMultiSelectionModel(EntityIdentityComparator<T> comp, ListDataProvider<T> listDataProvider) {
        super();
        this.comp = comp;
        this.listDataProvider = listDataProvider;
        this.listDataProvider.addDataDisplay(this);
    }

    /**
     * @param keyProvider
     *            {@link ProvidesKey} for the super class constructor
     * @param comp
     *            {@link EntityIdentityComparator} to compare the identity of the objects
     * @param listDataProvider
     *            {@link ListDataProvider} to add this {@link RefreshableSingleSelectionModel selectionmodel} as an
     *            display on {@link ListDataProvider}
     */
    public RefreshableMultiSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp, ListDataProvider<T> listDataProvider) {
        super(keyProvider);
        this.comp = comp;
        this.listDataProvider = listDataProvider;
        this.listDataProvider.addDataDisplay(this);
    }

    /**
     * @return the {@link EntityIdentityComparator} for the {@link RefreshableSingleSelectionModel}. If the
     *         {@link EntityIdentityComparator} is not set this method will return <code>null</code>.
     */
    @Override
    public EntityIdentityComparator<T> getEntityIdentityComparator() {
        return comp;
    }

    /**
     * Checks the old selection state of the object. If it was selected before, the old version will be replaced with
     * the new one. In all other cases this method behave same as
     * <code>super.setSelected(T item, boolean selected)</code>.
     * <p>
     * When the {@link EntityIdentityComparator} is null this method also behaves like the <code>super</code> method
     */
    @Override
    public void setSelected(T item, boolean selected) {
        if (comp == null || dontcheckSelectionState || item == null || getSelectedSet() == null) {
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
     * Refreshes the {@link RefreshableMultiSelectionModel} with the <code>newObjects</code>.All objects from the
     * current selection that {@link EntityIdentityComparator#representSameEntity(Object, Object) represent the same
     * entity} as an object from <code>newObjects</code> will be reselected. All others are de-selected. That means a
     * selected object is not contained in <code>newObjects</code> the object wouldn't be selected anymore. If this
     * selection model has no {@link EntityIdentityComparator} set, this method will use the {@link #equals(Object)}
     * method to compare. If an object is reselected it will be replaced with the new version of it.
     * <p>
     *
     * When the selection is refreshed this method triggers a
     * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent) onSelectionChangedEvent} using
     * {@link AbstractSelectionModel#fireEvent(com.google.gwt.event.shared.GwtEvent)}.
     * 
     * @param newObjects
     *            the new objects to refresh the {@link RefreshableMultiSelectionModel selectionmodel}
     */
    private void refreshSelectionModel(Iterable<T> newObjects) {
        dontcheckSelectionState = true;
        final Set<T> selectedSet = getSelectedSet();
        final boolean isEmpty = selectedSet.isEmpty();
        if (!isEmpty) {
            clear();
            for (T it : newObjects) {
                if (comp == null) {
                    setSelected(it, selectedSet.contains(it));
                } else {
                    boolean isSelected = false;
                    for (T selected : selectedSet) {
                        isSelected = comp.representSameEntity(selected, it);
                        if (isSelected) {
                            setSelected(it, isSelected);
                            break;
                        }
                    }
                }
            }
            SelectionChangeEvent.fire(this);
        }
        dontcheckSelectionState = false;
    }
    
    /**
     * This method is called when the {@link ListDataProvider} has new elements. It takes the new elements and refreshes
     * the {@link RefreshableMultiSelectionModel selectionmodel} with them.
     */
    @Override
    public void setRowData(int start, List<? extends T> values) {
        refreshSelectionModel(new ArrayList<>(listDataProvider.getList()));
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