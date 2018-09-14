package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.security.shared.HasPermissions.DefaultModes;

public abstract class AccessControlledActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {
    public AccessControlledActionsColumn(S imagesBarCell) {
        super(imagesBarCell);
    }
    
    public abstract Iterable<DefaultModes> getAllowedActions(T object);

    @Override
    public String getValue(T object) {
        ArrayList<String> allowedActions = new ArrayList<>();
        for (DefaultModes action : getAllowedActions(object)) {
            String actionName = action.name();
            actionName.replace("\\", "\\\\");
            actionName.replace(",", "\\,");
            allowedActions.add(actionName);
        }
        return String.join(",", allowedActions);
    }
}
