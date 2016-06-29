package com.sap.sse.gwt.client.panels;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.user.client.ui.Label;

public abstract class LabeledAbstractFilterablePanelCompatibleWithRefreshableSelectionModel<T>
        extends AbstractFilterablePanelCompatibleWithRefreshableSelectionModel<T> {
    public LabeledAbstractFilterablePanelCompatibleWithRefreshableSelectionModel(Label label,
            ListDataProvider<T> buffer, ListDataProvider<T> displayedData, AbstractCellTable<T> table) {
        super(buffer, displayedData, table);
        insert(label, 0);
    }
}
