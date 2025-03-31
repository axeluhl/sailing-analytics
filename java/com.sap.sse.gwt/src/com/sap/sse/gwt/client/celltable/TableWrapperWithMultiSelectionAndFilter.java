package com.sap.sse.gwt.client.celltable;

import java.util.Optional;
import java.util.function.Function;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;

/**
 * A {@link TableWrapperWithFilter} that supports multiple selection (selecting zero or more elements in the table).
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T> the object type represented by a row
 * @param <SM> the {@link StringMessages} sub-type to use
 * @param <TR> the table resources defining the styles for the elements shown
 */
public abstract class TableWrapperWithMultiSelectionAndFilter<T, SM extends StringMessages, TR extends CellTableWithCheckboxResources>
extends TableWrapperWithFilter<T, RefreshableMultiSelectionModel<T>, SM, TR> {
    public TableWrapperWithMultiSelectionAndFilter(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator,
            Optional<Function<T, Boolean>> updatePermissionFilterForCheckbox, Optional<String> filterLabel,
            String filterCheckboxLabel) {
        this(stringMessages, errorReporter, enablePager, entityIdentityComparator,
                GWT.create(CellTableWithCheckboxResources.class), updatePermissionFilterForCheckbox, filterLabel,
                filterCheckboxLabel);
    }

    public TableWrapperWithMultiSelectionAndFilter(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator,
            TR tableRes, Optional<Function<T, Boolean>> updatePermissionFilterForCheckbox, Optional<String> filterLabel,
            String filterCheckboxLabel) {
        super(stringMessages, errorReporter, /* multiSelection */ true, enablePager, entityIdentityComparator, tableRes,
                updatePermissionFilterForCheckbox, filterLabel, filterCheckboxLabel);
    }
}
