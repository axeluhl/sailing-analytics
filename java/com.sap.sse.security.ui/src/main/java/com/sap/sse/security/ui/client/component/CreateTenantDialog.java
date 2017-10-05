package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.CreateTenantDialog.TenantData;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.TenantDTO;

public class CreateTenantDialog extends DataEntryDialog<TenantData> {
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final TextBox ownerBox;
    private final UserManagementServiceAsync userManagementService;
    
    public static class TenantData {
        private final String name;
        private final String owner;
        
        protected TenantData(String name, String owner) {
            super();
            this.name = name;
            this.owner = owner;
        }
        public String getName() {
            return name;
        }
        public String getOwner() {
            return owner;
        }
    }
    
    public CreateTenantDialog(final StringMessages stringMessages, final UserManagementServiceAsync userManagementService, 
            final TenantListDataProvider tenantListDataProvider) {
        this(stringMessages, "Create a tenant", "Enter tenant name and owner", userManagementService, null, new DialogCallback<TenantData>() {
            @Override
            public void ok(TenantData tenantData) {
                userManagementService.createTenant(tenantData.name, tenantData.owner, new AsyncCallback<TenantDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error creating tenant.");
                    }
                    @Override
                    public void onSuccess(TenantDTO result) {
                        tenantListDataProvider.updateDisplays();
                    }
                });
            }
            @Override
            public void cancel() {
            }
        });
    }
    
    private CreateTenantDialog(final StringMessages stringMessages, final String title, final String message,
                final UserManagementServiceAsync userManagementService, final TenantDTO tenant
                , final DialogCallback<TenantData> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(),
                new DataEntryDialog.Validator<TenantData>() {
                    @Override
                    public String getErrorMessage(TenantData valueToValidate) {
                        return null; // TODO: Check if owner is really a tenant and name is unique?
                    }
                }, callback);
        nameBox = createTextBox("", 30);
        nameBox.setName("name");
        ownerBox = createTextBox("", 30);
        ownerBox.setName("owner");
        if (tenant != null) {
            nameBox.setText(tenant.getName());
            ownerBox.setText(tenant.getOwner() == null ? "" : tenant.getOwner().getOwner() + " | " + tenant.getOwner().getTenantOwner());
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
        Grid result = new Grid(2, 2);
        result.setWidget(0, 0, new Label("Name"));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label("Owner"));
        result.setWidget(1, 1, getOwnerBox());
        return result;
    }

    @Override
    protected TenantData getResult() {
        return new TenantData(nameBox.getText(), ownerBox.getText());
    }
}
