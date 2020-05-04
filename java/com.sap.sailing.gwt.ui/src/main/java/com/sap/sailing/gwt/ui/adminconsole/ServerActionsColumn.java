package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.shared.HasPermissions.Action;

/**
 * Implementation of actions column for actions that requires checking only server permissions. The
 * individual instance shown by the respective row is ignored in this case, and only the server action
 * is checked for permission by the current user. See also {@link UserService#hasServerPermission(Action)}.
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

    private ServerActionsColumn(final S imagesBarCell, final UserService userService) {
        super(imagesBarCell, (T object, Action action)->userService.hasServerPermission(action));
    }
}
