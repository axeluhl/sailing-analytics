package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

public abstract class SortableColumn<T, C> extends Column<T, C> {
    protected SortableColumn(Cell<C> cell) {
        super(cell);
        setSortable(true);
    }
    
    public abstract Comparator<T> getComparator();
    
    public abstract Header<?> getHeader();
}
