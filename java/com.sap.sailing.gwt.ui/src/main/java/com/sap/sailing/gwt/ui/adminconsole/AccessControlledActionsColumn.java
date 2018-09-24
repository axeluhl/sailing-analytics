package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public abstract class AccessControlledActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {

    public AccessControlledActionsColumn(S imagesBarCell) {
        super(imagesBarCell);
    }
    
    protected abstract Iterable<DefaultActions> getAllowedActions(T object);

    @Override
    public String getValue(T object) {
        final ArrayList<String> allowedActions = new ArrayList<>();
        for (final DefaultActions action : getAllowedActions(object)) {
            final String actionName = action.name();
            actionName.replace("\\", "\\\\");
            actionName.replace(",", "\\,");
            allowedActions.add(actionName);
        }
        return String.join(",", allowedActions);
    }
}
