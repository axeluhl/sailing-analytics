package com.sap.sse.security.ui.client.component;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapperWithFilter;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

/**
 * A {@link TableWrapperWithFilter} that displays {@link SecuredDTO} objects and uses the checkbox filter to display by default
 * only those elements that the user can {@link DefaultActions#UPDATE update}.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T> the object type represented by a row
 * @param <S> the selection model type to ues
 * @param <SM> the {@link StringMessages} sub-type to use
 * @param <TR> the table resources defining the styles for the elements shown
 */
public abstract class TableWrapperWithFilterForSecuredDTOs<T extends SecuredDTO, S extends RefreshableSelectionModel<T>, SM extends StringMessages, TR extends CellTableWithCheckboxResources>
extends TableWrapperWithFilter<T, S, SM, TR> {
    public TableWrapperWithFilterForSecuredDTOs(SM stringMessages, ErrorReporter errorReporter, boolean multiSelection,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, UserService userService, Optional<String> filterLabel) {
        this(stringMessages, errorReporter, multiSelection, enablePager, entityIdentityComparator,
                GWT.create(CellTableWithCheckboxResources.class), userService, filterLabel);
    }

    public TableWrapperWithFilterForSecuredDTOs(SM stringMessages, ErrorReporter errorReporter, boolean multiSelection,
            boolean enablePager, Optional<EntityIdentityComparator<T>> entityIdentityComparator, TR tableRes,
            UserService userService, Optional<String> filterLabel) {
        super(stringMessages, errorReporter, multiSelection, enablePager, entityIdentityComparator, tableRes,
                Optional.of(t -> userService.hasPermission(t, DefaultActions.UPDATE)), filterLabel,
                stringMessages.hideElementsWithoutUpdateRights());
    }
}
