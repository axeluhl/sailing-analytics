package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
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
 * This {@link RefreshableSingleSelectionModel} implements the {@link RefreshableSelectionModel} interface. So it
 * register it self as a display on the {@link ListDataProvider} and reacts on the changes of {@link ListDataProvider}.
 * When the {@link ListDataProvider} is changed this {@link RefreshableSingleSelectionModel selectionmodel} will refresh
 * the selection according to the {@link ListDataProvider} changes. To make this class work correct it is very important
 * to set the {@link ListDataProvider}, otherwise it won´t work.
 * <p>
 * For more details on the update process read the {@link RefreshableSelectionModel} Javadoc and see the methods
 * {@link RefreshableSingleSelectionModel#refreshSelectionModel(Iterable)} and
 * {@link RefreshableSingleSelectionModel#setRowData(int, List)}.
 * 
 * @author D064976
 * @param <T>
 *            the type of entries
 */
public class RefreshableSingleSelectionModel<T> extends SingleSelectionModel<T> implements RefreshableSelectionModel<T>, HasData<T> {
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
    public RefreshableSingleSelectionModel(EntityIdentityComparator<T> comp, ListDataProvider<T> listDataProvider) {
        super();
        this.comp=comp;
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
    public RefreshableSingleSelectionModel(ProvidesKey<T> keyProvider, EntityIdentityComparator<T> comp, ListDataProvider<T> listDataProvider) {
        super(keyProvider);
        this.comp =comp;
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
     * Checks the old selection state of the object. If it was selected before, the old version will be replaced with the
     * new one. In all other cases this method behave same as <code>super.setSelected(T item, boolean selected)</code>.
     * <p>
     * When the {@link EntityIdentityComparator} is null this method also behaves like the <code>super</code> method
     */
    @Override
    public void setSelected(T item, boolean selected) {
        if (comp == null || dontcheckSelectionState || item == null || getSelectedObject() == null) {
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

    /**
     * Refreshes the {@link RefreshableSingleSelectionModel} with the <code>newObjects</code>. If the current selected
     * object {@link EntityIdentityComparator#representSameEntity(Object, Object) represent the same entity} as an
     * object from <code>newObjects</code> it will be reselected. All others are de-selected. That means a selected
     * object is not contained in <code>newObjects</code> the object wouldn't be selected anymore. If this selection
     * model has no {@link EntityIdentityComparator} set, this method will use the {@link #equals(Object)} method to
     * compare. If an object is reselected it will be replaced with the new version of it.
     * <p>
     *
     * When the selection is refreshed this method triggers a
     * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent) onSelectionChangedEvent} using
     * {@link AbstractSelectionModel#fireEvent(com.google.gwt.event.shared.GwtEvent)}.
     * 
     * @param newObjects
     *            the new objects to refresh the {@link RefreshableSingleSelectionModel selectionmodel}
     */
    private void refreshSelectionModel(Iterable<T> newObjects) {
        // avoid a new selection state check in setSelected
        dontcheckSelectionState = true;
        final T selected = getSelectedObject();
        if (selected != null) {
            clear();
            for (final T it : newObjects) {
                boolean isEqual = comp == null ? selected.equals(it) : comp.representSameEntity(selected, it);
                if (isEqual) {
                    setSelected(it, true);
                    break;
                }
            }
        }
        SelectionChangeEvent.fire(this);
        dontcheckSelectionState = false;
    }

    /**
     * This method is called when the {@link ListDataProvider} has new elements. It takes the new elements and refreshes
     * the {@link RefreshableSingleSelectionModel selectionmodel} with them.
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