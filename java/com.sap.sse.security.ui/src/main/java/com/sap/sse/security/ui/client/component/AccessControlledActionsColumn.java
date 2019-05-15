package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

public class AccessControlledActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {

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

    private final Map<String, Consumer<T>> nameToCallbackMap = new HashMap<>();
    private final Map<String, Action> nameToActionMap = new HashMap<>();
    private final UserService userService;
    private final Function<T, ? extends SecuredDTO> securedObjectFactory;

    private AccessControlledActionsColumn(final S imagesBarCell, final UserService userService,
            final Function<T, ? extends SecuredDTO> securedObjectFactory) {
        super(imagesBarCell);
        this.userService = userService;
        this.securedObjectFactory = securedObjectFactory;
        this.setFieldUpdater((index, object, value) -> nameToCallbackMap.get(value).accept(object));
    }

    /**
     * Adds an action identified by the provided name which will always be accessible.
     * 
     * @param name
     *            {@link String name} to identify the action
     * @param callback
     *            {@link Consumer callback} to execute when the action is triggered
     */
    public void addAction(final String name, final Consumer<T> callback) {
        this.nameToCallbackMap.put(name, callback);
    }

    /**
     * Adds an action identified by the provided name which will only be accessible, if the current user has the
     * required permission specified by the provided {@link Action action}.
     * 
     * @param name
     *            {@link String name} to identify the action
     * @param action
     *            {@link Action action} specifying the permission which is required to access the action
     * @param callback
     *            {@link Consumer callback} to execute when the action is triggered
     */
    public void addAction(final String name, final Action action, final Consumer<T> callback) {
        this.nameToActionMap.put(name, action);
        this.addAction(name, callback);
    }

    @Override
    public final String getValue(final T object) {
        final ArrayList<String> allowedActions = new ArrayList<>();
        for (final String name : nameToCallbackMap.keySet()) {
            final Action action = nameToActionMap.get(name);
            final SecuredDTO securedObject = securedObjectFactory.apply(object);
            if (isNotRestrictedOrHasPermission(action, securedObject)) {
                final String escapedName = name.replace("\\", "\\\\").replace(",", "\\,");
                allowedActions.add(escapedName);
            }
        }
        return String.join(",", allowedActions);
    }

    private boolean isNotRestrictedOrHasPermission(final Action action, final SecuredDTO object) {
        final boolean result;
        if (action == null) {
            result = true;
        } else {
            result = userService.hasPermission(object, action);
        }
        return result;
    }
}
