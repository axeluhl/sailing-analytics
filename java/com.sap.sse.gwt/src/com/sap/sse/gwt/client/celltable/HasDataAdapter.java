package com.sap.sse.gwt.client.celltable;

import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.RangeChangeEvent.Handler;

/**
 * Adapts a {@link RefreshableSelectionModel} to the {@link HasData} interface and forwards calls to
 * {@link HasData#setRowData(int, java.util.List)} to {@link RefreshableSelectionModel#refreshSelectionModel(Iterable)}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class HasDataAdapter<T> implements HasData<T> {
    private final RefreshableSelectionModel<T> selectionModel;
    private final ListDataProvider<T> listDataProvider;

    public HasDataAdapter(RefreshableSelectionModel<T> selectionModel, ListDataProvider<T> listDataProvider) {
        super();
        this.selectionModel = selectionModel;
        this.listDataProvider = listDataProvider;
    }

    /**
     * This method is called when the {@link ListDataProvider} has new elements. It takes the new elements and refreshes
     * the {@link RefreshableSingleSelectionModel selectionmodel} with them.
     */
    @Override
    public void setRowData(int start, List<? extends T> values) {
        selectionModel.refreshSelectionModel(listDataProvider.getList());
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
        selectionModel.clear();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        selectionModel.fireEvent(event);
    }

}
