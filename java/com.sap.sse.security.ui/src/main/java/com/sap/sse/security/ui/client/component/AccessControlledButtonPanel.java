package com.sap.sse.security.ui.client.component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel where several buttons can be added which are either {@link #addUnsecuredAction(String, Command) unsecured} or
 * restricted for users with {@link #addCreateAction(String, Command) create} and /or
 * {@link #addRemoveAction(String, Command) remove} permissions. The {@link Button#setVisible(boolean) visibility} of
 * secured buttons depend on the permissions of the currently logged-in user and changes dynamically.
 */
public class AccessControlledButtonPanel extends Composite {

    private final HorizontalPanel panel = new HorizontalPanel();
    private final Map<Button, Supplier<Boolean>> buttonToPermissions = new HashMap<>();

    private final Supplier<Boolean> createPermissionCheck, removePermissionCheck;
    private final BiConsumer<Button, Supplier<Boolean>> visibilityUpdater = (btn, check) -> btn.setVisible(check.get());

    /**
     * Creates an {@link AccessControlledButtonPanel} instance for the given {@link HasPermissions type} using the
     * provided {@link UserService} to check permissions and register for user status changes.
     * 
     * @param userService
     *            the {@link UserService} to check permissions and register for user status changes
     * @param type
     *            the {@link HasPermissions} representing the type of objects to be secured by this panel
     */
    public AccessControlledButtonPanel(final UserService userService, final HasPermissions type) {
        this.createPermissionCheck = () -> userService.hasCurrentUserPermissionToCreateObjectOfType(type);
        this.removePermissionCheck = () -> userService.hasCurrentUserPermissionToDeleteAnyObjectOfType(type);
        userService.addUserStatusEventHandler((user, preAuth) -> updateVisibility(), true);
        initWidget(panel);
    }

    /**
     * Adds an unsecured action button, which's visibility is independent from the current user's permissions.
     * 
     * @param text
     *            the {@link String text} to show on the button
     * @param callback
     *            the {@link Command callback} to execute on button click
     * @return the created {@link Button} instance
     */
    public Button addUnsecuredAction(final String text, final Command callback) {
        return addAction(text, () -> true, callback);
    }

    /**
     * Adds a secured action button, which is only visible if the current user has the
     * {@link UserService#hasCurrentUserPermissionToCreateObjectOfType(HasPermissions) create permission} for the
     * {@link HasPermissions type} provided in this {@link AccessControlledButtonPanel}'s constructor.
     * 
     * @param text
     *            the {@link String text} to show on the button
     * @param callback
     *            the {@link Command callback} to execute on button click, if permission is granted
     * @return the created {@link Button} instance
     */
    public Button addCreateAction(final String text, final Command callback) {
        return addAction(text, createPermissionCheck, callback);
    }

    /**
     * Adds a secured action button, which is only visible if the current user has any
     * {@link UserService#hasCurrentUserPermissionToDeleteAnyObjectOfType(HasPermissions) delete permission} for the
     * {@link HasPermissions type} provided in this {@link AccessControlledButtonPanel}'s constructor.
     * 
     * @param text
     *            the {@link String text} to show on the button
     * @param callback
     *            the {@link Command callback} to execute on button click, if permission is granted
     * @return the created {@link Button} instance
     */
    public Button addRemoveAction(final String text, final Command callback) {
        return addAction(text, removePermissionCheck, callback);
    }

    /**
     * Adds an action button, which's visibility depends on the provided {@link Supplier permission check}.
     * 
     * @param text
     *            the {@link String text} to show on the button
     * @param permissionCheck
     *            the {@link Supplier permission check} to decide if the action button is visible or not
     * @param callback
     *            the {@link Command callback} to execute on button click, if permission is granted
     * @return the created {@link Button} instance
     */
    public Button addAction(final String text, final Supplier<Boolean> permissionCheck, final Command callback) {
        final Button button = new Button(text, (ClickHandler) event -> {
            if (permissionCheck.get()) {
                callback.execute();
            }
        });
        this.buttonToPermissions.put(button, permissionCheck);
        button.getElement().getStyle().setMarginRight(5, Unit.PX);
        this.panel.add(button);
        this.visibilityUpdater.accept(button, permissionCheck);
        return button;
    }

    /**
     * Updates the visibility of all previously added actions based on their {@link Supplier permission check}s.
     */
    public void updateVisibility() {
        buttonToPermissions.forEach(visibilityUpdater);
    }

    /**
     * Inserts a widget (e.g. a text box) into the button bar at a give index
     */
    public void insertWidgetAtPosition(Widget widget, int index) {
        this.panel.insert(widget, index);
    }

}
