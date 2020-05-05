package com.sap.sse.security.ui.client.component;

import java.util.function.Function;

import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

/**
 * Visibility of the name-based actions linked to a non-{@code null} {@link Action} using the
 * {@link #addAction(String, Action, java.util.function.Consumer)} method is decided based on whether the user is
 * permitted to execute that action on the object shown in the row with the action icons ("per-instance"
 * permission validation).
 */
public class AccessControlledActionsColumn<T, S extends ImagesBarCell> extends ActionsColumn<T, S> {
    /**
     * Creates a new {@link AccessControlledActionsColumn} instance for {@link SecuredDTO} objects where permission
     * checks are performed against the respective table entries {@link Function#identity() themselves}, if required.
     */
    public static <T extends SecuredDTO, S extends ImagesBarCell> AccessControlledActionsColumn<T, S> create(
            final S imagesBarCell, final UserService userService) {
        return new AccessControlledActionsColumn<T, S>(imagesBarCell, userService, Function.identity());
    }

    /**
     * Creates a new {@link AccessControlledActionsColumn} instance for arbitrary objects where permission checks are
     * performed against {@link SecuredDTO} object provided by the given {@link Function factory}, if required.
     */
    public static <T, S extends ImagesBarCell> AccessControlledActionsColumn<T, S> create(final S imagesBarCell,
            final UserService userService, final Function<T, ? extends SecuredDTO> securedObjectFactory) {
        return new AccessControlledActionsColumn<T, S>(imagesBarCell, userService, securedObjectFactory);
    }

    private AccessControlledActionsColumn(final S imagesBarCell, final UserService userService,
            final Function<T, ? extends SecuredDTO> securedObjectFactory) {
        super(imagesBarCell, (T object, Action action)->userService.hasPermission(securedObjectFactory.apply(object), action));
    }
}
