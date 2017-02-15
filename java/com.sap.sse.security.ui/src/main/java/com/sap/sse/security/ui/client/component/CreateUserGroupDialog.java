package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.CreateUserGroupDialog.UserGroupData;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.TenantDTO;
import com.sap.sse.security.ui.shared.UserGroupDTO;

public class CreateUserGroupDialog extends DataEntryDialog<UserGroupData> {
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final TextBox ownerBox;
    private final CheckBox tenant;
    private final UserManagementServiceAsync userManagementService;
    
    public static class UserGroupData {
        private final String name;
        private final String owner;
        private final boolean tenant;
        
        protected UserGroupData(String name, String owner, boolean tenant) {
            super();
            this.name = name;
            this.owner = owner;
            this.tenant = tenant;
        }
        public String getName() {
            return name;
        }
        public String getOwner() {
            return owner;
        }
        
        public boolean isTenant() {
            return tenant;
        }
    }
    
    public CreateUserGroupDialog(final StringMessages stringMessages, final UserManagementServiceAsync userManagementService, 
            final UserGroupListDataProvider tenantListDataProvider) {
        this(stringMessages, "Create a user group", "Enter user group name and owner", userManagementService, null, new DialogCallback<UserGroupData>() {
            @Override
            public void ok(UserGroupData userGroupData) {
                if (userGroupData.isTenant()) {
                    userManagementService.createTenant(userGroupData.name, userGroupData.owner, new AsyncCallback<TenantDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error creating tenant.");
                        }
                        @Override
                        public void onSuccess(TenantDTO result) {
                            tenantListDataProvider.updateDisplays();
                        }
                    });
                } else {
                    userManagementService.createUserGroup(userGroupData.name, userGroupData.owner, new AsyncCallback<UserGroupDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error creating user group.");
                        }
                        @Override
                        public void onSuccess(UserGroupDTO result) {
                            tenantListDataProvider.updateDisplays();
                        }
                    });
                }
            }
            @Override
            public void cancel() {
            }
        });
    }
    
    private CreateUserGroupDialog(final StringMessages stringMessages, final String title, final String message,
                final UserManagementServiceAsync userManagementService, final UserGroupDTO userGroup
                , final DialogCallback<UserGroupData> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<UserGroupData>() {
                    @Override
                    public String getErrorMessage(UserGroupData valueToValidate) {
                        return null; // TODO: Check if owner is really a tenant and name is unique?
                    }
                }, callback);
        nameBox = createTextBox("", 30);
        nameBox.setName("name");
        ownerBox = createTextBox("", 30);
        ownerBox.setName("owner");
        tenant = new CheckBox();
        tenant.setName("tenant");
        if (userGroup != null) {
            nameBox.setText(userGroup.getName());
            ownerBox.setText(userGroup.getAccessControlList().getOwner() == null ? "" : userGroup.getAccessControlList().getOwner());
            tenant.setEnabled(userGroup instanceof TenantDTO);
        }
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
    }
    
    protected UserManagementServiceAsync getUserManagementService() {
        return userManagementService;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameBox;
    }
    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    protected TextBox getNameBox() {
        return nameBox;
    }

    protected TextBox getOwnerBox() {
        return ownerBox;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(3, 2);
        result.setWidget(0, 0, new Label("Name"));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label("Owner"));
        result.setWidget(1, 1, getOwnerBox());
        result.setWidget(2, 0, new Label("Is Tenant"));
        result.setWidget(2, 1, tenant);
        return result;
    }

    @Override
    protected UserGroupData getResult() {
        return new UserGroupData(nameBox.getText(), ownerBox.getText(), tenant.getValue());
    }
}
