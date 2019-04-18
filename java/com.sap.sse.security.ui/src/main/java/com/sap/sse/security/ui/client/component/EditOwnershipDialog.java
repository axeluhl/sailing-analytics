package com.sap.sse.security.ui.client.component;

import static com.sap.sse.gwt.client.Notification.NotificationType.ERROR;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DialogUtils;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog.OwnershipDialogResult;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class EditOwnershipDialog extends DataEntryDialog<OwnershipDialogResult> {

    private final StringMessages stringMessages;
    private final UserManagementServiceAsync userManagementService;
    private final SuggestBox suggestUserName;
    private final SuggestBox suggestUserGroupName;
    private boolean resolvingUsername;
    private boolean resolvingUserGroupName;
    private StrippedUserGroupDTO resolvedUserGroup;
    private boolean userExists;
    private final String permissionType;
    private final String securedObjectId;
    
    static class OwnershipDialogResult {
        private final UUID userGroupId;
        private final String username;
        private final String userGroupName;
        private final boolean resolvingUsername;
        private final boolean resolvingUserGroupName;
        private final boolean userExist;

        private OwnershipDialogResult(final String username, final String userGroupName,
                final boolean resolvingUsername, final boolean resolvingUserGroupName,
                final UUID userGroupId, final boolean userExist) {
            this.username = username;
            this.userGroupName = userGroupName;
            this.resolvingUsername = resolvingUsername;
            this.resolvingUserGroupName = resolvingUserGroupName;
            this.userGroupId = userGroupId;
            this.userExist = userExist;
        }

        public UUID getUserGroupId() {
            return userGroupId;
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

        public boolean isUserExist() {
            return userExist;
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
            if (valueToValidate.isResolvingUsername()) {
                errorMessage = stringMessages.pleaseWaitUntilUsernameIsResolved();
            } else if (valueToValidate.isResolvingUserGroupName()) {
                errorMessage = stringMessages.pleaseWaitUntilUserGroupNameIsResolved();
            } else if (!valueToValidate.getUsername().trim().isEmpty() && !valueToValidate.isUserExist()) {
                errorMessage = stringMessages.userNotFound(valueToValidate.getUsername());
            } else if (!valueToValidate.getUserGroupName().trim().isEmpty() && valueToValidate.getUserGroupId() == null) {
                errorMessage = stringMessages.usergroupNotFound(valueToValidate.getUserGroupName());
            } else if ((valueToValidate.getUsername() == null || valueToValidate.getUsername().isEmpty())
                    && valueToValidate.getUserGroupId() == null) {
                errorMessage = stringMessages.enterAtLeastOneOwner();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }
    
    private EditOwnershipDialog(UserManagementServiceAsync userManagementService, OwnershipDTO ownership,
            StringMessages stringMessages, DialogCallback<OwnershipDialogResult> callback, final String permissionType,
            final String securedObjectId) {
        super(stringMessages.ownership(), stringMessages.editObjectOwnership(), stringMessages.ok(),
                stringMessages.cancel(), new Validator(stringMessages), callback);
        this.userManagementService = userManagementService;
        this.stringMessages = stringMessages;
        this.permissionType = permissionType;
        this.securedObjectId = securedObjectId;
        final StrippedUserDTO userOwner = ownership == null ? null : ownership.getUserOwner();
        this.resolvedUserGroup = ownership == null ? null : ownership.getTenantOwner();

        // User Suggest
        final MultiWordSuggestOracle suggestUserOracle = new MultiWordSuggestOracle();
        this.userManagementService.getUserList(new AsyncCallback<Collection<UserDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUser(caught.getMessage()), NotificationType.ERROR);
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                final Collection<String> userNames = result.stream().map(UserDTO::getName).collect(Collectors.toList());
                suggestUserOracle.addAll(userNames);
                suggestUserOracle.setDefaultSuggestionsFromText(userNames);
            }
        });

        this.suggestUserName = createSuggestBox(suggestUserOracle);
        this.suggestUserName.setText(userOwner == null ? "" : userOwner.getName());

        // User Group Suggest
        final MultiWordSuggestOracle suggestUserGroupOracle = new MultiWordSuggestOracle();
        this.userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroupDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorObtainingUserGroup(caught.getMessage()),
                        NotificationType.ERROR);
            }

            @Override
            public void onSuccess(Collection<UserGroupDTO> result) {
                final Collection<String> userGroupNames = result.stream().map(UserGroupDTO::getName)
                        .collect(Collectors.toList());
                suggestUserGroupOracle.addAll(userGroupNames);
                suggestUserGroupOracle.setDefaultSuggestionsFromText(userGroupNames);
            }
        });
        this.suggestUserGroupName = createSuggestBox(suggestUserGroupOracle);
        this.suggestUserGroupName.setText(resolvedUserGroup == null ? "" : resolvedUserGroup.getName());

        this.suggestUserName.addValueChangeHandler(e -> checkIfUserExists());
        this.suggestUserGroupName.addValueChangeHandler(e -> resolveUserGroup());

        this.suggestUserName.addSelectionHandler(e -> checkIfUserExists());
        this.suggestUserGroupName.addSelectionHandler(e -> resolveUserGroup());

        DialogUtils.addFocusUponKeyUpToggler(this.suggestUserName);
        DialogUtils.addFocusUponKeyUpToggler(this.suggestUserGroupName);

        checkIfUserExists();
        resolveUserGroup();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(stringMessages.id() + ": "));
        result.setWidget(0, 1, new Label(securedObjectId));
        result.setWidget(1, 0, new Label(stringMessages.permissionType() + ": "));
        result.setWidget(1, 1, new Label(permissionType));

        result.setWidget(2, 0, new Label(stringMessages.user()));
        result.setWidget(2, 1, suggestUserName);
        result.setWidget(3, 0, new Label(stringMessages.group()));
        result.setWidget(3, 1, suggestUserGroupName);
        return result;
    }

    private void resolveUserGroup() {
        if (resolvedUserGroup == null || !suggestUserGroupName.getText().equals(resolvedUserGroup.getName())) {
            resolvedUserGroup = null;
            resolvingUserGroupName = true;
            userManagementService.getStrippedUserGroupByName(suggestUserGroupName.getText(),
                    new AsyncCallback<StrippedUserGroupDTO>() {
                        @Override
                        public void onSuccess(StrippedUserGroupDTO result) {
                            resolvedUserGroup = result;
                            resolvingUserGroupName = false;
                            validateAndUpdate();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.errorObtainingUserGroup(caught.getMessage()),
                                    NotificationType.ERROR);
                        }
                    });
        }
    }

    private void checkIfUserExists() {
        if (!resolvingUsername) {
            resolvingUsername = true;
            userManagementService.userExists(suggestUserName.getText(), new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable caught) {
                    Notification.notify(stringMessages.errorObtainingUser(caught.getMessage()), NotificationType.ERROR);
                }

                @Override
                public void onSuccess(Boolean result) {
                    resolvingUsername = false;
                    userExists = result;
                    validateAndUpdate();
                }
            });
        }
    }

    @Override
    protected OwnershipDialogResult getResult() {
        return new OwnershipDialogResult(suggestUserName.getText(),
                resolvedUserGroup == null ? "" : resolvedUserGroup.getName(), resolvingUsername,
                resolvingUserGroupName, resolvedUserGroup == null ? null : resolvedUserGroup.getId(), userExists);
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
            this.identifierFactory = SecuredDTO::getIdentifier;
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
            final QualifiedObjectIdentifier identifier = securedObject.getIdentifier();
            final String permissionType = identifier.getTypeIdentifier();
            final String id = identifier.getTypeRelativeObjectIdentifier().toString();
            // new EditOwnershipDialog(userManagementService, null, // securedObject.getOwnership(),
            // StringMessages.INSTANCE, new EditOwnershipDialogCallback(securedObject), permissionType, id).show();
            new EditOwnershipDialog(userManagementService, securedObject.getOwnership(), StringMessages.INSTANCE,
                    new EditOwnershipDialogCallback(securedObject), permissionType, id).show();
        }

        private class EditOwnershipDialogCallback implements DialogCallback<OwnershipDialogResult> {

            private final T securedObject;

            private EditOwnershipDialogCallback(T securedObject) {
                this.securedObject = securedObject;
            }

            @Override
            public void ok(OwnershipDialogResult editedObject) {
                final QualifiedObjectIdentifier objectIdentifier = identifierFactory.apply(securedObject);
                userManagementService.setOwnership(editedObject.getUsername(), editedObject.getUserGroupId(),
                        objectIdentifier, securedObject.getName(), new UpdateOwnershipAsyncCallback());
            }

            @Override
            public final void cancel() {
            }

            private class UpdateOwnershipAsyncCallback implements AsyncCallback<OwnershipDTO> {

                private UpdateOwnershipAsyncCallback() {
                }

                @Override
                public final void onSuccess(OwnershipDTO result) {
                    securedObject.setOwnership(result);
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
