package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

public class AccessControlledActionsColumn<T extends SecuredDTO, S extends ImagesBarCell>
        extends ImagesBarColumn<T, S> {

    private final Map<String, Consumer<T>> nameToCallbackMap = new HashMap<>();
    private final Map<String, Action> nameToActionMap = new HashMap<>();

    private final UserService userService;
    private final BiFunction<Action, T, WildcardPermission> permissionFactory;

    public AccessControlledActionsColumn(final S imagesBarCell, final UserService userService,
            final HasPermissions type) {
        super(imagesBarCell);
        this.userService = userService;
        this.permissionFactory = (action, object) -> type.getPermissionForSecuredDTO(action, object);
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
            if (isNotRestrictedOrHasPermission(action, object)) {
                final String escapedName = name.replace("\\", "\\\\").replace(",", "\\,");
                allowedActions.add(escapedName);
            }
        }
        return String.join(",", allowedActions);
    }

    private boolean isNotRestrictedOrHasPermission(final Action action, final T object) {
        final boolean result;
        if (action == null) {
            result = true;
        } else {
            final OwnershipDTO ownership = object.getOwnership();
            final AccessControlListDTO accessControlList = object.getAccessControlList();
            result = userService.hasPermission(permissionFactory.apply(action, object), ownership, accessControlList);
        }
        return result;
    }
}
