package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.cell.client.Cell;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.ui.leaderboard.SortableColumnWithMinMax;

public abstract class AbstractSortableColumnWithMinMax<T, C> extends SortableColumn<T, C> implements
        SortableColumnWithMinMax<T, C> {
    protected AbstractSortableColumnWithMinMax(Cell<C> cell, SortingOrder preferredSortingOrder) {
        super(cell, preferredSortingOrder);
    }
}
