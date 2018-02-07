package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

/**
 * The {@link #getTable() table} created and wrapped by this object offers already a {@link ListHandler} for sorting.
 * Subclasses can obtain the table's default column sort handler created by this class's constructor by calling
 * {@link #getColumnSortHandler}. The table is wrapped by a panel that can be obtained using {@link #asWidget()}
 * and which contains, if requested, the pager widget underneath the table.
 */
public abstract class TableWrapper<T, S extends RefreshableSelectionModel<T>>
extends com.sap.sse.gwt.client.celltable.TableWrapper<T, S, StringMessages, AdminConsoleTableResources> {
    protected final SailingServiceAsync sailingService;

    public TableWrapper(SailingServiceAsync sailingService, final StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager, EntityIdentityComparator<T> entityIdentityComparator) {
        super(stringMessages, errorReporter, multiSelection, enablePager, entityIdentityComparator, GWT.create(AdminConsoleTableResources.class));
        this.sailingService = sailingService;
    }
}