package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.NamedSecuredObjectDTO;
import com.sap.sailing.gwt.ui.adminconsole.EditOwnershipDialog.OwnershipDialogResult;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Helper class to setup the configuration of a composite for {@link NamedSecuredObjectDTO secured object}s.
 *
 * @param <T>
 *            the actual type of the secured object
 */
public class SecuredObjectCompositeConfig<T extends NamedSecuredObjectDTO> {

    private final com.sap.sse.security.ui.client.i18n.StringMessages securityStringMessages = GWT
            .create(com.sap.sse.security.ui.client.i18n.StringMessages.class);
    private final Map<DefaultActions, Consumer<T>> actionCallbacks = new HashMap<>();
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final Function<T, QualifiedObjectIdentifier> identifierFactory;
    private final BiFunction<DefaultActions, T, WildcardPermission> permissionFactory;

    /**
     * Creates a new {@link SecuredObjectCompositeConfig} instance with the given parameters.
     * 
     * @param userService
     *            {@link UserService} to determine current user and {@link UserManagementServiceAsync} interface
     * @param errorReporter
     *            {@link ErrorReporter} to report failures, e.g. during ownership updates
     * @param stringMessages
     *            {@link StringMessages} instance to use
     * @param permission
     *            {@link SecuredDomainTypes} specifying the scope of required permissions to modify the secured object
     * @param idFactory
     *            {@link Function factory} to get a {@link String type relative identifier} for the secured object
     */
    public SecuredObjectCompositeConfig(final UserService userService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final HasPermissions permission, final Function<T, String> idFactory) {
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.identifierFactory = idFactory.andThen(permission::getQualifiedObjectIdentifier);
        this.permissionFactory = (action, obj) -> permission.getPermissionForObjects(action, idFactory.apply(obj));
    }

    /**
     * Adds an {@link DefaultActions action} for the secured object which will be contained in the
     * {@link #addActionColumn(AbstractCellTable, ImagesBarCell) action column} if the current user has the respectively
     * required permissions.
     * 
     * @param action
     *            {@link DefaultActions action} to add to the {@link AccessControlledActionsColumn action column}
     * @param actionCallback
     *            {@link Consumer callback} to execute when the action is triggered
     */
    public void addAction(final DefaultActions action, final Consumer<T> actionCallback) {
        this.actionCallbacks.put(action, actionCallback);
    }

    /**
     * Adds the an {@link AccessControlledActionsColumn action column} using the given {@link ImagesBarCell image bar
     * cell} to the provided {@link AbstractCellTable cell table} which contains previously
     * {@link #addAction(DefaultActions, Consumer) added actions} where the current user has permissions for.
     * 
     * @param table
     *            {@link AbstractCellTable table} to add the action column to
     * @param imageBarCell
     *            {@link ImagesBarCell cell} to use to render the action column
     */
    public <C extends ImagesBarCell> void addActionColumn(final AbstractCellTable<T> table, final C imageBarCell) {
        final AccessControlledActionsColumn<T, C> actionColumn = new AccessControlledActionsColumn<T, C>(imageBarCell) {
            @Override
            public Iterable<DefaultActions> getAllowedActions(T object) {
                final UserDTO user = userService.getCurrentUser();
                return actionCallbacks.keySet().stream()
                        .filter(action -> user.hasPermission(permissionFactory.apply(action, object),
                                object.getOwnership(), object.getAccessControlList()))
                        .collect(Collectors.toSet());
            }
        };
        actionColumn.setFieldUpdater((index, securedObject, value) -> {
            final DefaultActions action = DefaultActions.valueOf(value);
            actionCallbacks.get(action).accept(securedObject);
        });
        table.addColumn(actionColumn, stringMessages.actions());
    }

    /**
     * Adds sortable {@link Column column}s to the provided {@link AbstractCellTable cell table} showing group owner and
     * user owner information of secured object table entries, if any.
     * 
     * @param table
     *            {@link AbstractCellTable table} to add the column to
     * @param columnSortHandler
     *            {@link ListHandler handler} to register the column's sort {@link Comparator comparator}
     */
    public void addOwnerColumns(final AbstractCellTable<T> table, final ListHandler<T> columnSortHandler) {
        final OwnerColumn groupColumn = new OwnerColumn(Ownership::getTenantOwner);
        table.addColumn(groupColumn, securityStringMessages.group());
        columnSortHandler.setComparator(groupColumn, groupColumn.getComparator());
        final OwnerColumn userColumn = new OwnerColumn(Ownership::getUserOwner);
        table.addColumn(userColumn, securityStringMessages.user());
        columnSortHandler.setComparator(userColumn, userColumn.getComparator());
    }

    /**
     * Opens a {@link EditOwnershipDialog dialog} to edit ownerships for the provided secured object instance.
     * 
     * @param securedObject
     *            {@link NamedSecuredObjectDTO secured object} instance to edit ownerships for
     * @param updateCallback
     *            {@link Consumer callback} to execute when the dialog is confirmed
     */
    public void openOwnershipDialog(final T securedObject, final Consumer<T> updateCallback) {
        new EditOwnershipDialog(userService.getUserManagementService(), securedObject.getOwnership(),
                securityStringMessages, new EditOwnershipDialogCallback(securedObject, updateCallback)).show();
    }

    private class EditOwnershipDialogCallback implements DialogCallback<OwnershipDialogResult> {

        private final T securedObject;
        private final Consumer<T> updateCallback;

        private EditOwnershipDialogCallback(T securedObject, Consumer<T> updateCallback) {
            this.securedObject = securedObject;
            this.updateCallback = updateCallback;
        }

        @Override
        public void ok(OwnershipDialogResult editedObject) {
            final QualifiedObjectIdentifier objectIdentifier = identifierFactory.apply(securedObject);
            userService.getUserManagementService().setOwnership(editedObject.getOwnership(), objectIdentifier,
                    securedObject.getName(), new UpdateOwnershipAsyncCallback(editedObject));
        }

        @Override
        public final void cancel() {
        }

        private class UpdateOwnershipAsyncCallback implements AsyncCallback<QualifiedObjectIdentifier> {

            private final OwnershipDialogResult editResult;

            private UpdateOwnershipAsyncCallback(final OwnershipDialogResult result) {
                this.editResult = result;
            }

            @Override
            public final void onSuccess(QualifiedObjectIdentifier result) {
                securedObject.setOwnership(editResult.getOwnership());
                updateCallback.accept(securedObject);
            }

            @Override
            public final void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorUpdatingOwnership(securedObject.getName()));
            }
        }
    }


    private class OwnerColumn extends TextColumn<T> {

        private final Function<T, Named> ownerResolver;

        private OwnerColumn(Function<Ownership, Named> ownerResolver) {
            final Function<T, Ownership> ownershipResolver = NamedSecuredObjectDTO::getOwnership;
            this.ownerResolver = ownershipResolver.andThen(ownerResolver);
            this.setSortable(true);
        }

        @Override
        public final String getValue(T object) {
            return Optional.ofNullable(ownerResolver.apply(object)).map(Named::getName).orElse("");
        }

        private Comparator<T> getComparator() {
            return (o1, o2) -> getValue(o1).compareTo(getValue(o2));
        }

    }

}
