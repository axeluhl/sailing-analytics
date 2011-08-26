package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;

public abstract class SortableColumn<T> extends TextColumn<T> {
    public SortableColumn() {
        setSortable(true);
    }
    
    public abstract Comparator<T> getComparator();
    
    public abstract Header<String> getHeader();
}
