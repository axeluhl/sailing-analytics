package com.sap.sailing.gwt.ui.adminconsole;

import java.util.function.BiConsumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.MigrateGroupOwnershipDialog.MigrateGroupOwnerForHierarchyDialogDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MigrateGroupOwnerForHierarchyDTO;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class MigrateGroupOwnershipDialog extends DataEntryDialog<MigrateGroupOwnerForHierarchyDialogDTO> {

    private final StringMessages stringMessages;
    private final UserManagementServiceAsync userManagementService;
    private final Label currentGroupLabel;
    private final TextBox groupnameBox;
    private boolean resolvingUserGroupName;
    private UserGroupDTO resolvedUserGroup;
    private final RadioButton toExistingGroup;
    private final RadioButton toNewGroup;
    private final CheckBox migrateCompetitors;
    private final CheckBox migrateBoats;

    public static class MigrateGroupOwnerForHierarchyDialogDTO extends MigrateGroupOwnerForHierarchyDTO {
        private static final long serialVersionUID = -7336625438188182079L;

        private boolean resolvingUserGroupName;

        public MigrateGroupOwnerForHierarchyDialogDTO(UserGroupDTO existingUserGroup, boolean resolvingUserGroupName,
                boolean createNewGroup, String newGroupName, boolean updateCompetitors, boolean updateBoats) {
            super(existingUserGroup, createNewGroup, newGroupName, updateCompetitors, updateBoats);
            this.resolvingUserGroupName = resolvingUserGroupName;
        }

        public boolean isResolvingUserGroupName() {
            return resolvingUserGroupName;
        }
    }

    private static class Validator implements DataEntryDialog.Validator<MigrateGroupOwnerForHierarchyDialogDTO> {
        private final StringMessages stringMessages;

        public Validator(StringMessages stringMessages) {
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(MigrateGroupOwnerForHierarchyDialogDTO valueToValidate) {
            final String errorMessage;
            if (!valueToValidate.isCreateNewGroup()) {
                if (valueToValidate.isResolvingUserGroupName()) {
                    errorMessage = stringMessages.pleaseWaitUntilUserGroupNameIsResolved();
                } else if (valueToValidate.getExistingUserGroup() == null) {
                    errorMessage = stringMessages.usergroupNotFound(valueToValidate.getGroupName());
                } else {
                    errorMessage = null;
                }
            } else if (valueToValidate.getGroupName() == null || valueToValidate.getGroupName().isEmpty()) {
                errorMessage = stringMessages.enterUserGroupName();
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }

    private MigrateGroupOwnershipDialog(UserManagementServiceAsync userManagementService,
            UserGroupDTO currentGroupOwner,
            StringMessages stringMessages, DialogCallback<MigrateGroupOwnerForHierarchyDialogDTO> callback) {
        super(stringMessages.ownership(), stringMessages.migrateHierarchyToGroupOwner(), stringMessages.ok(),
                stringMessages.cancel(), new Validator(stringMessages), callback);
        this.userManagementService = userManagementService;
        this.currentGroupLabel = new Label(currentGroupOwner == null ? "n/a" : currentGroupOwner.getName());
        this.groupnameBox = createTextBox(currentGroupOwner == null ? "" : currentGroupOwner.getName(),
                /* visibileLength */ 20);
        this.groupnameBox.addChangeHandler(e -> resolveUserGroup());

        final String radioGroupName = "mode";
        toExistingGroup = createRadioButton(radioGroupName, stringMessages.useExistingUserGroup());
        toNewGroup = createRadioButton(radioGroupName, stringMessages.useNewUserGroup());
        toExistingGroup.setValue(true);

        migrateCompetitors = createCheckbox("");
        migrateBoats = createCheckbox("");
        this.stringMessages = stringMessages;
        resolveUserGroup();
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(5, 2);
        result.setWidget(0, 0, new Label(stringMessages.currentGroupOwner()));
        result.setWidget(0, 1, currentGroupLabel);
        result.setWidget(1, 0, toExistingGroup);
        result.setWidget(1, 1, toNewGroup);
        result.setWidget(2, 0, new Label(stringMessages.group()));
        result.setWidget(2, 1, groupnameBox);
        result.setWidget(3, 0, new Label(stringMessages.migrateCompetitors()));
        result.setWidget(3, 1, migrateCompetitors);
        result.setWidget(4, 0, new Label(stringMessages.migrateBoats()));
        result.setWidget(4, 1, migrateBoats);
        return result;
    }

    private void resolveUserGroup() {
        resolvedUserGroup = null;
        resolvingUserGroupName = true;
        userManagementService.getUserGroupByName(groupnameBox.getText(), new AsyncCallback<UserGroupDTO>() {
            @Override
            public void onSuccess(UserGroupDTO result) {
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

    @Override
    protected MigrateGroupOwnerForHierarchyDialogDTO getResult() {
        final boolean newGroup = toNewGroup.getValue();
        return new MigrateGroupOwnerForHierarchyDialogDTO(newGroup ? null : resolvedUserGroup,
                newGroup ? false : resolvingUserGroupName, newGroup, groupnameBox.getValue().trim(),
                migrateCompetitors.getValue(), migrateBoats.getValue());
    }

    /**
     * Creates a new {@link DialogConfig dialog configuration} instance which can be (re-)used to
     * {@link DialogConfig#openDialog(Named) open} a {@link MigrateGroupOwnershipDialog dialog}.
     */
    public static <T extends Named & SecuredDTO> DialogConfig<T> create(
            final UserManagementServiceAsync userManagementService,
            final BiConsumer<T, MigrateGroupOwnerForHierarchyDTO> updateCallback) {
        return new DialogConfig<>(userManagementService, updateCallback);
    }

    public static class DialogConfig<T extends Named & SecuredDTO> {

        private final UserManagementServiceAsync userManagementService;
        private final BiConsumer<T, MigrateGroupOwnerForHierarchyDTO> updateCallback;

        private DialogConfig(final UserManagementServiceAsync userManagementService,
                final BiConsumer<T, MigrateGroupOwnerForHierarchyDTO> updateCallback) {
            this.userManagementService = userManagementService;
            this.updateCallback = updateCallback;
        }

        /**
         * Opens a {@link MigrateGroupOwnershipDialog dialog} to edit ownerships for the provided secured object
         * instance.
         * 
         * @param securedObject
         *            {@link Named} {@link SecuredObject} instance to migrate group owner for
         */
        public void openDialog(final T securedObject) {
            OwnershipDTO ownership = securedObject.getOwnership();
            UserGroupDTO tenant = ownership == null ? null : ownership.getTenantOwner();
            new MigrateGroupOwnershipDialog(userManagementService,
                    tenant, StringMessages.INSTANCE,
                    new EditOwnershipDialogCallback(securedObject)).show();
        }

        private class EditOwnershipDialogCallback implements DialogCallback<MigrateGroupOwnerForHierarchyDialogDTO> {
            
            private final T securedObject;

            public EditOwnershipDialogCallback(T securedObject) {
                this.securedObject = securedObject;
            }

            @Override
            public void ok(MigrateGroupOwnerForHierarchyDialogDTO editedObject) {
                boolean createNewGroup = editedObject.isCreateNewGroup();
                updateCallback.accept(securedObject, new MigrateGroupOwnerForHierarchyDTO(editedObject.getExistingUserGroup(),
                        createNewGroup, createNewGroup ? editedObject.getGroupName() : null,
                        editedObject.isUpdateCompetitors(), editedObject.isUpdateBoats()));
            }

            @Override
            public final void cancel() {
            }
        }
    }

}
