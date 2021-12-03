package com.sap.sse.security.ui.client.component;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

/**
 * A {@link TableWrapperWithFilterForSecuredDTOs} that supports multi selection (selecting zero or more elements in the table).
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T> the object type represented by a row
 * @param <SM> the {@link StringMessages} sub-type to use
 * @param <TR> the table resources defining the styles for the elements shown
 */
public abstract class TableWrapperWithMultiSelectionAndFilterForSecuredDTO<T extends SecuredDTO, SM extends StringMessages, TR extends CellTableWithCheckboxResources>
        extends TableWrapperWithFilterForSecuredDTOs<T, RefreshableMultiSelectionModel<T>, SM, TR> {
    public TableWrapperWithMultiSelectionAndFilterForSecuredDTO(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, UserService userService,
            Optional<String> filterLabel) {
        this(stringMessages, errorReporter, enablePager, entityIdentityComparator,
                GWT.create(CellTableWithCheckboxResources.class), userService, filterLabel);
    }

    public TableWrapperWithMultiSelectionAndFilterForSecuredDTO(SM stringMessages, ErrorReporter errorReporter,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, TR tableRes,
            UserService userService, Optional<String> filterLabel) {
        super(stringMessages, errorReporter, /* multiSelection */ true, enablePager, entityIdentityComparator, tableRes,
                userService, filterLabel);
    }

}
