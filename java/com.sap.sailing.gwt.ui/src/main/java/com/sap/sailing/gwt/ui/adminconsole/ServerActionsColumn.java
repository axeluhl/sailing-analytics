package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * Implementation of actions column for actions that requires checking only server permissions 
 * 
 * @author Aleksandr Lukyanchik
 */
public class ServerActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {

    /**
     * Creates a new {@link ServerActionsColumn} instance for arbitrary objects where permission checks are performed
     * against {@link ServerActions}.
     */
    public static <T, S extends ImagesBarCell> ServerActionsColumn<T, S> create(final S imagesBarCell,
            final UserService userService) {
        return new ServerActionsColumn<T, S>(imagesBarCell, userService);
    }

    private final Map<String, Consumer<T>> nameToCallbackMap = new HashMap<>();
    private final Map<String, Action> nameToActionMap = new HashMap<>();
    private final UserService userService;

    private ServerActionsColumn(final S imagesBarCell, final UserService userService) {
        super(imagesBarCell);
        this.userService = userService;
        this.setFieldUpdater((index, object, value) -> nameToCallbackMap.get(value).accept(object));
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
        this.nameToCallbackMap.put(name, callback);
    }

    @Override
    public final String getValue(final T object) {
        final ArrayList<String> allowedActions = new ArrayList<>();
        for (final String name : nameToCallbackMap.keySet()) {
            final Action action = nameToActionMap.get(name);
            if (action == null || userService.hasServerPermission(action)) {
                final String escapedName = name.replace("\\", "\\\\").replace(",", "\\,");
                allowedActions.add(escapedName);
            }
        }
        return String.join(",", allowedActions);
    }
}
