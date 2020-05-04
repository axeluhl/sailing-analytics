package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sap.sse.security.shared.HasPermissions.Action;

public abstract class ActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {

    protected final Map<String, Consumer<T>> nameToCallbackMap = new HashMap<>();
    protected final Map<String, Action> nameToActionMap = new HashMap<>();

    protected ActionsColumn(final S imagesBarCell) {
        super(imagesBarCell);
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
    
    protected String mapActions(Function<Action, Boolean> checker) {
        final ArrayList<String> allowedActions = new ArrayList<>();
        for (final String name : nameToCallbackMap.keySet()) {
            final Action action = nameToActionMap.get(name);
            if (isNotRestrictedOrHasPermission(action, checker)) {
                final String escapedName = name.replace("\\", "\\\\").replace(",", "\\,");
                allowedActions.add(escapedName);
            }
        }
        return String.join(",", allowedActions);
    }
    
    /**
     * Null action assumes that it's always accessible see {@link #addAction(String, Consumer)}
     */
    private boolean isNotRestrictedOrHasPermission(final Action action, final Function<Action, Boolean> checker) {
        return action == null ? true : checker.apply(action);
    }
}
