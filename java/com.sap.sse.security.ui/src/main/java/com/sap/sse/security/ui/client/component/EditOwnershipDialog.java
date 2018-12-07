package com.sap.sse.security.ui.client.component;

import static com.sap.sse.gwt.client.Notification.NotificationType.ERROR;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog.OwnershipDialogResult;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class EditOwnershipDialog extends DataEntryDialog<OwnershipDialogResult> {

    private final StringMessages stringMessages;
    private final UserManagementServiceAsync userManagementService;
    private final TextBox usernameBox;
    private final TextBox groupnameBox;
    private boolean resolvingUsername;
    private boolean resolvingUserGroupName;
    private StrippedUserDTO resolvedUser;
    private StrippedUserGroupDTO resolvedUserGroup;
    
    static class OwnershipDialogResult {
        private final OwnershipDTO ownership;
        private final String username;
        private final String userGroupName;
        private final boolean resolvingUsername;
        private final boolean resolvingUserGroupName;

        private OwnershipDialogResult(final OwnershipDTO ownership, final String username, final String userGroupName,
                final boolean resolvingUsername, final boolean resolvingUserGroupName) {
            this.ownership = ownership;
            this.username = username;
            this.userGroupName = userGroupName;
            this.resolvingUsername = resolvingUsername;
            this.resolvingUserGroupName = resolvingUserGroupName;
        }

        private OwnershipDTO getOwnership() {
            return ownership;
        }

        private boolean isResolvingUsername() {
            return resolvingUsername;
        }

        private boolean isResolvingUserGroupName() {
            return resolvingUserGroupName;
        }

        private String getUsername() {
            return username;
        }

        private String getUserGroupName() {
            return userGroupName;
        }
    }
    
    private static class Validator implements DataEntryDialog.Validator<OwnershipDialogResult> {
        private final StringMessages stringMessages;
        
        public Validator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(OwnershipDialogResult valueToValidate) {
            final String errorMessage;
            final OwnershipDTO ownership = valueToValidate.getOwnership();
            if (valueToValidate.isResolvingUsername()) {
                errorMessage = stringMessages.pleaseWaitUntilUsernameIsResolved();
            } else if (valueToValidate.isResolvingUserGroupName()) {
                errorMessage = stringMessages.pleaseWaitUntilUserGroupNameIsResolved();
            } else if (!valueToValidate.getUsername().trim().isEmpty() && ownership.getUserOwner() == null) {
                errorMessage = stringMessages.userNotFound(valueToValidate.getUsername());
            } else if (!valueToValidate.getUserGroupName().trim().isEmpty() && ownership.getTenantOwner() == null) {
                errorMessage = stringMessages.usergroupNotFound(valueToValidate.getUserGroupName());
            } else if (ownership.getUserOwner() == null && ownership.getTenantOwner() == null) {
                errorMessage = stringMessages.enterAtLeastOneOwner();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }
    
    private EditOwnershipDialog(UserManagementServiceAsync userManagementService, OwnershipDTO ownership,
            StringMessages stringMessages, DialogCallback<OwnershipDialogResult> callback) {
        super(stringMessages.ownership(), stringMessages.editObjectOwnership(), stringMessages.ok(),
                stringMessages.cancel(), new Validator(stringMessages), callback);
        this.userManagementService = userManagementService;
        this.usernameBox = createTextBox(
                ownership == null || ownership.getUserOwner() == null ? "" : ownership.getUserOwner().getName(),
                /* visibleLength */ 20);
        this.groupnameBox = createTextBox(
                ownership == null || ownership.getTenantOwner() == null ? "" : ownership.getTenantOwner().getName(),
                /* visibileLength */ 20);
        this.usernameBox.addChangeHandler(e->resolveUser());
        this.groupnameBox.addChangeHandler(e->resolveUserGroup());
        this.stringMessages = stringMessages;
        resolveUser();
        resolveUserGroup();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(2, 2);
        result.setWidget(0, 0, new Label(stringMessages.user()));
        result.setWidget(0, 1, usernameBox);
        result.setWidget(1, 0, new Label(stringMessages.group()));
        result.setWidget(1, 1, groupnameBox);
        return result;
    }

    private void resolveUserGroup() {
        resolvedUserGroup = null;
        resolvingUserGroupName = true;
        userManagementService.getStrippedUserGroupByName(groupnameBox.getText(), new AsyncCallback<StrippedUserGroupDTO>() {
            @Override
            public void onSuccess(StrippedUserGroupDTO result) {
                resolvedUserGroup = result;
                resolvingUserGroupName = false;
                validateAndUpdate();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUserGroup(caught.getMessage()), NotificationType.ERROR);
            }
        });
    }

    private void resolveUser() {
        resolvedUser = null;
        resolvingUsername = true;
        userManagementService.getUserByName(usernameBox.getText(), new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                resolvedUser = result;
                resolvingUsername = false;
                validateAndUpdate();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUser(caught.getMessage()), NotificationType.ERROR);
            }
        });
    }

    @Override
    protected OwnershipDialogResult getResult() {
        return new OwnershipDialogResult(new OwnershipDTO(resolvedUser, resolvedUserGroup), usernameBox.getText(),
                groupnameBox.getText(), resolvingUsername, resolvingUserGroupName);
    }

    /**
     * Creates a new {@link DialogConfig dialog configuration} instance which can be (re-)used to
     * {@link DialogConfig#openDialog(Named) open} a {@link EditOwnershipDialog dialog}.
     * 
     * @param userManagementService
     *            {@link UserManagementServiceAsync} to use to set the secured object's ownership
     * @param type
     *            {@link SecuredDomainType} specifying the type of required permissions to modify the secured object
     * @param typeRelativeIdFactory
     *            {@link Function factory} to get a {@link String type relative identifier} for the secured object
     * @param updateCallback
     *            {@link Consumer callback} to execute when the dialog is confirmed and ownership update succeeded
     * @param errorCallback
     *            {@link Consumer callback} to execute when the dialog is confirmed and ownership update fails
     */
    public static <T extends Named & SecuredDTO> DialogConfig<T> create(
            final UserManagementServiceAsync userManagementService, final HasPermissions type,
            final Consumer<T> updateCallback,
            final StringMessages stringMessages) {
        return new DialogConfig<>(userManagementService, type, updateCallback, stringMessages);
    }

    public static class DialogConfig<T extends Named & SecuredDTO> {

        private final UserManagementServiceAsync userManagementService;
        private final Consumer<T> updateCallback;
        private final Function<T, QualifiedObjectIdentifier> identifierFactory;
        private final StringMessages stringMessages;

        private DialogConfig(final UserManagementServiceAsync userManagementService, final HasPermissions type,
                final Consumer<T> updateCallback,
                final StringMessages stringMessages) {
            this.userManagementService = userManagementService;
            this.identifierFactory = type::getQualifiedObjectIdentifier;
            this.updateCallback = updateCallback;
            this.stringMessages = stringMessages;
        }

        /**
         * Opens a {@link EditOwnershipDialog dialog} to edit ownerships for the provided secured object instance.
         * 
         * @param securedObject
         *            {@link Named} {@link SecuredObject} instance to edit ownerships for
         */
        public void openDialog(final T securedObject) {
            new EditOwnershipDialog(userManagementService, securedObject.getOwnership(),
                    StringMessages.INSTANCE, new EditOwnershipDialogCallback(securedObject)).show();
        }

        private class EditOwnershipDialogCallback implements DialogCallback<OwnershipDialogResult> {

            private final T securedObject;

            private EditOwnershipDialogCallback(T securedObject) {
                this.securedObject = securedObject;
            }

            @Override
            public void ok(OwnershipDialogResult editedObject) {
                final QualifiedObjectIdentifier objectIdentifier = identifierFactory.apply(securedObject);
                userManagementService.setOwnership(editedObject.getOwnership(), objectIdentifier,
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
                    Notification.notify(stringMessages.errorUpdatingOwnership(securedObject.getName()), ERROR);
                }
            }
        }
    }

}
