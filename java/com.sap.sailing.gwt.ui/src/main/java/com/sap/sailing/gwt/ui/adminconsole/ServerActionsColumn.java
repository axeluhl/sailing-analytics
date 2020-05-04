package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * Implementation of actions column for actions that requires checking only server permissions
 * 
 * @author Aleksandr Lukyanchik
 */
public class ServerActionsColumn<T, S extends ImagesBarCell> extends ActionsColumn<T, S> {

    /**
     * Creates a new {@link ServerActionsColumn} instance for arbitrary objects where permission checks are performed
     * against {@link ServerActions}.
     */
    public static <T, S extends ImagesBarCell> ServerActionsColumn<T, S> create(final S imagesBarCell,
            final UserService userService) {
        return new ServerActionsColumn<T, S>(imagesBarCell, userService);
    }

    private final UserService userService;

    private ServerActionsColumn(final S imagesBarCell, final UserService userService) {
        super(imagesBarCell);
        this.userService = userService;
    }

    @Override
    public final String getValue(final T object) {
        return mapActions(userService::hasServerPermission);
    }
}
