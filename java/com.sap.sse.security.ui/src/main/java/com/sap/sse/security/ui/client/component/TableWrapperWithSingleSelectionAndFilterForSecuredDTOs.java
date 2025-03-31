package com.sap.sse.security.ui.client.component;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

/**
 * A {@link TableWrapperWithFilterForSecuredDTOs} that supports single selection (selecting at most one element in the table).
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T> the object type represented by a row
 * @param <SM> the {@link StringMessages} sub-type to use
 * @param <TR> the table resources defining the styles for the elements shown
 */
public abstract class TableWrapperWithSingleSelectionAndFilterForSecuredDTOs<T extends SecuredDTO, SM extends StringMessages, TR extends CellTableWithCheckboxResources>
        extends TableWrapperWithFilterForSecuredDTOs<T, RefreshableSingleSelectionModel<T>, SM, TR> {
    public TableWrapperWithSingleSelectionAndFilterForSecuredDTOs(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, UserService userService,
            Optional<String> filterLabel) {
        this(stringMessages, errorReporter, enablePager, entityIdentityComparator,
                GWT.create(CellTableWithCheckboxResources.class), userService, filterLabel);
    }

    public TableWrapperWithSingleSelectionAndFilterForSecuredDTOs(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, TR tableRes,
            UserService userService, Optional<String> filterLabel) {
        super(stringMessages, errorReporter, /* multiSelection */ false, enablePager, entityIdentityComparator,
                tableRes, userService, filterLabel);
    }

}
