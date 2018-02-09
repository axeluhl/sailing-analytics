package com.sap.sse.gwt.client.celltable;

import java.util.function.Function;

import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sse.common.util.NaturalComparator;

/**
 * A simple way to construct a {@link TextColumn} that can display texts for objects of a specific type {@code T} in a
 * {@link CellTable}. A function must be provided that projects the {@code T} object to a {@link String}. If the
 * {@link #AbstractSortableTextColumn(Function, ListHandler)} or
 * {@link #AbstractSortableTextColumn(Function, ListHandler, boolean)} constructor is used, the column will be
 * {@link #setSortable(boolean) made sortable}, and a {@link NaturalComparator} will be used for sorting.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public class AbstractSortableTextColumn<T> extends TextColumn<T> {
    private final Function<T, String> stringSupplier;

    /**
     * Clients need to specify a function that maps an object of type {@code T} to a {@link String}.
     * That string will be used for display and is the basis for sorting the column, using a
     * {@link NaturalComparator}.
     */
    public AbstractSortableTextColumn(Function<T, String> stringSupplier) {
        super();
        this.stringSupplier = stringSupplier;
    }
    
    public AbstractSortableTextColumn(Function<T, String> stringSupplier, ListHandler<T> listHandler, boolean caseSensitive) {
        this(stringSupplier);
        setSortable(true);
        listHandler.setComparator(this, (a, b) -> new NaturalComparator(caseSensitive).compare(stringSupplier.apply(a), stringSupplier.apply(b)));
    }

    /**
     * Same as {@link #AbstractSortableTextColumn(Function, ListHandler, boolean)}, setting case sensitivity to
     * {@code false}, meaning that during sorting case will be distinguished by default.
     */
    public AbstractSortableTextColumn(Function<T, String> stringSupplier, ListHandler<T> listHandler) {
        this(stringSupplier, listHandler, /* caseSensitive */ false);
    }
    
    @Override
    public String getValue(T object) {
        return stringSupplier.apply(object);
    }
}
