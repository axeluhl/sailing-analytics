package com.sap.sse.gwt.client.celltable;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * This {@link FlushableSortedCellTableWithStylableHeaders} provides the
 * {@link FlushableSortedCellTableWithStylableHeaders#flush()}-method for the {@link SelectionCheckboxColumn}. So the
 * {@link SelectionCheckboxColumn} can ensure that the selection state is displayed correctly.
 * 
 * @author D064976
 * @param <T>
 */
public class FlushableSortedCellTableWithStylableHeaders<T> extends SortedCellTableWithStylableHeaders<T>
        implements Flushable {
    public FlushableSortedCellTableWithStylableHeaders(int pageSize, CellTable.Resources resources) {
        super(pageSize, resources);
    }
}
