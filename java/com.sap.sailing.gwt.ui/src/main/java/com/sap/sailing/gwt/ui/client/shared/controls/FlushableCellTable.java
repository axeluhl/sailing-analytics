package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class FlushableCellTable<T> extends CellTable<T> implements Flushable {
    public FlushableCellTable() {
        super();
    }
    public FlushableCellTable(final int pageSize) {
        super(pageSize);
    }
    public FlushableCellTable(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }
    public FlushableCellTable(int pageSize, Resources resources) {
        super(pageSize, resources);
    }
    public FlushableCellTable(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
    }
    public FlushableCellTable(final int pageSize, Resources resources, ProvidesKey<T> keyPrivider) {
        super(pageSize, resources, keyPrivider);
    }
    public FlushableCellTable(final int pageSize, Resources resources, ProvidesKey<T> keyProvider, Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
    }
    public FlushableCellTable(final int pageSize, Resources resources, ProvidesKey<T> keyProvider, Widget loadingIndicator, boolean enableColGroup, boolean attachLoadingPanel) {
        super(pageSize,resources,keyProvider,loadingIndicator,enableColGroup, attachLoadingPanel);
    }
}