package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sap.sse.security.shared.HasPermissions.Action;

/**
 * A special image bar column that can assign security-related {@link com.sap.sse.security.Action} objects
 * to the otherwise name-based actions managed by the {@link ImagedBarCell} used to render the actions.
 */
public abstract class ActionsColumn<T, S extends ImagesBarCell> extends ImagesBarColumn<T, S> {

    protected final Map<String, Consumer<T>> nameToCallbackMap = new HashMap<>();
    protected final Map<String, Action> nameToActionMap = new HashMap<>();
    
    /**
     * Checks whether the user has permission to execute the action that is the second argument to the function
     * on the object that is the first argument of the function; the result of the check is returned by the function.
     * It is the basis for the {@link #getValue(Object)} implementation, currying this function with the object
     * parameter of the value function to obtain a function to pass to the {@link #mapActions(Function)} method.
     */
    private final BiFunction<T, Action, Boolean> permissionChecker;

    /**
     * @param permissionChecker
     *            the function that returns for a given object (passed as its first parameter) whether the action
     *            (passed as the second parameter) is considered permitted by the current user on that object. The output
     *            of this function decides whether name-based actions linked to a non-{@code null} {@link Action} using
     *            the {@link #addAction(String, Action, Consumer)} method shall be displayed to the user.
     */
    protected ActionsColumn(final S imagesBarCell, BiFunction<T, Action, Boolean> permissionChecker) {
        super(imagesBarCell);
        this.permissionChecker = permissionChecker;
        this.setFieldUpdater((index, object, value) -> nameToCallbackMap.get(value).accept(object));
    }

    @Override
    public final String getValue(final T object) {
        return mapActions(action->permissionChecker.apply(object, action));
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
     *            {@link Action action} specifying the permission which is required to access the action; you may use
     *            {@code null} here which then has the same effect as calling {@link #addAction(String, Consumer)},
     *            namely that the string-based action {@code name} will not undergo any checks and will always be shown
     *            as available
     * @param callback
     *            {@link Consumer callback} to execute when the action is triggered
     */
    public void addAction(final String name, final Action action, final Consumer<T> callback) {
        this.nameToActionMap.put(name, action);
        this.addAction(name, callback);
    }
    
    /**
     * Those actions that were added using {@link #addAction(String, Action, Consumer)} with a non-{@code null}
     * {@link com.sap.sse.security.Action} will undergo a check using the {@code checker} function and will be
     * added only to the comma-separated list of actions returned by this method if the checker accepts them.
     */
    protected String mapActions(Function<Action, Boolean> checker) {
        final List<String> allowedActions = new ArrayList<>();
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
     * {@code null} action assumes that it's always accessible; all name-based actions added using
     * {@link #addAction(String, Consumer)} are usually always considered accessible. Those that have an
     * {@link com.sap.sse.security.Action} linked because they were added using
     * {@link #addAction(String, Action, Consumer)} will undergo a check using the {@code checker} function passed.
     */
    private boolean isNotRestrictedOrHasPermission(final Action action, final Function<Action, Boolean> checker) {
        return action == null ? true : checker.apply(action);
    }
}
