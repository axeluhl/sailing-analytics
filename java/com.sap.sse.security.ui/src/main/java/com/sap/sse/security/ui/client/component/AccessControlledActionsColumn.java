package com.sap.sse.security.ui.client.component;

import java.util.function.Function;

import com.sap.sse.gwt.client.celltable.ActionsColumn;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

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

    private final UserService userService;
    private final Function<T, ? extends SecuredDTO> securedObjectFactory;

    private AccessControlledActionsColumn(final S imagesBarCell, final UserService userService,
            final Function<T, ? extends SecuredDTO> securedObjectFactory) {
        super(imagesBarCell);
        this.userService = userService;
        this.securedObjectFactory = securedObjectFactory;
    }

    @Override
    public final String getValue(final T object) {
        return mapActions(action -> userService.hasPermission(securedObjectFactory.apply(object), action));
    }
}
