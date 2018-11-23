package com.sap.sailing.gwt.ui.adminconsole;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.MigrateGroupOwnershipDialog.MigrateGroupOwnerForHierarchyDialogDTO;
import com.sap.sailing.gwt.ui.shared.MigrateGroupOwnerForHierarchyDTO;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecuredObject;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class MigrateGroupOwnershipDialog extends DataEntryDialog<MigrateGroupOwnerForHierarchyDialogDTO> {

    private final StringMessages stringMessages;
    private final UserManagementServiceAsync userManagementService;
    private final Label currentGroupLabel;
    private final TextBox groupnameBox;
    private boolean resolvingUserGroupName;
    private UserGroup resolvedUserGroup;
    private final RadioButton toExistingGroup;
    private final RadioButton toNewGroup;
    private final CheckBox migrateCompetitors;
    private final CheckBox migrateBoats;

    public static class MigrateGroupOwnerForHierarchyDialogDTO extends MigrateGroupOwnerForHierarchyDTO {
        private static final long serialVersionUID = -7336625438188182079L;

        private boolean resolvingUserGroupName;

        public MigrateGroupOwnerForHierarchyDialogDTO(UserGroup existingUserGroup, boolean resolvingUserGroupName,
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
                errorMessage = "TODO no userGroup name given";
            } else {
                errorMessage = null;
            }
            return errorMessage;
        }
    }

    private MigrateGroupOwnershipDialog(UserManagementServiceAsync userManagementService, UserGroup currentGroupOwner,
            StringMessages stringMessages, DialogCallback<MigrateGroupOwnerForHierarchyDialogDTO> callback) {
        super(stringMessages.ownership(), stringMessages.editObjectOwnership(), stringMessages.ok(),
                stringMessages.cancel(), new Validator(stringMessages), callback);
        this.userManagementService = userManagementService;
        this.currentGroupLabel = createLabel(currentGroupOwner == null ? "n/a" : currentGroupOwner.getName());
        this.groupnameBox = createTextBox(currentGroupOwner == null ? "" : currentGroupOwner.getName(),
                /* visibileLength */ 20);
        this.groupnameBox.addChangeHandler(e -> resolveUserGroup());

        final String radioGroupName = "mode";
        toExistingGroup = createRadioButton(radioGroupName, "TOOD use existing group");
        toNewGroup = createRadioButton(radioGroupName, "TOOD create new group");
        toExistingGroup.setValue(true);

        migrateCompetitors = createCheckbox("");
        migrateBoats = createCheckbox("");
        this.stringMessages = stringMessages;
        resolveUserGroup();
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(5, 2);
        result.setWidget(0, 0, new Label("TODO current group"));
        result.setWidget(0, 1, currentGroupLabel);
        result.setWidget(1, 0, toExistingGroup);
        result.setWidget(1, 1, toNewGroup);
        result.setWidget(2, 0, new Label(stringMessages.group()));
        result.setWidget(2, 1, groupnameBox);
        result.setWidget(3, 0, new Label("TODO migrate competitors"));
        result.setWidget(3, 1, migrateCompetitors);
        result.setWidget(4, 0, new Label("TODO migrate boats"));
        result.setWidget(4, 1, migrateBoats);
        return result;
    }

    private void resolveUserGroup() {
        resolvedUserGroup = null;
        resolvingUserGroupName = true;
        userManagementService.getUserGroupByName(groupnameBox.getText(), new AsyncCallback<UserGroup>() {
            @Override
            public void onSuccess(UserGroup result) {
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
    public static <T extends Named & SecuredObject> DialogConfig<T> create(
            final UserManagementServiceAsync userManagementService,
            final BiConsumer<T, MigrateGroupOwnerForHierarchyDTO> updateCallback) {
        return new DialogConfig<>(userManagementService, updateCallback);
    }

    public static class DialogConfig<T extends Named & SecuredObject> {

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
         *            {@link Named} {@link SecuredObject} instance to edit ownerships for
         */
        public void openDialog(final T securedObject) {
            Ownership ownership = securedObject.getOwnership();
            new MigrateGroupOwnershipDialog(userManagementService,
                    ownership == null ? null : ownership.getTenantOwner(), StringMessages.INSTANCE,
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
