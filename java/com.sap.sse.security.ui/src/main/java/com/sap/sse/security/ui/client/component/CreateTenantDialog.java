package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.CreateTenantDialog.TenantData;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class CreateTenantDialog extends DataEntryDialog<TenantData> {
    private final StringMessages stringMessages;
    private final TextBox nameBox;
    private final UserManagementServiceAsync userManagementService;
    
    public static class TenantData {
        private final String name;
        
        protected TenantData(String name) {
            super();
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    
    public CreateTenantDialog(final StringMessages stringMessages, UserService userService, 
            final UserManagementServiceAsync userManagementService, final TenantListDataProvider tenantListDataProvider) {
        this(stringMessages, stringMessages.createTenant(), stringMessages.enterTenantName(), userManagementService, null, new DialogCallback<TenantData>() {
            @Override
            public void ok(TenantData tenantData) {
                userManagementService.createTenant(tenantData.name, "tenant", new AsyncCallback<Tenant>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error creating tenant.");
                    }
                    @Override
                    public void onSuccess(Tenant result) {
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
                final UserManagementServiceAsync userManagementService, final Tenant tenant
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
        if (tenant != null) {
            nameBox.setText(tenant.getName());
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
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(1, 2);
        result.setWidget(0, 0, new Label(getStringMessages().name()));
        result.setWidget(0, 1, getNameBox());
        return result;
    }

    @Override
    protected TenantData getResult() {
        return new TenantData(nameBox.getText());
    }
}
