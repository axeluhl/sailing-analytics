package com.sap.sse.security.ui.client.component;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class TenantManagementPanel extends DockPanel {
    private SingleSelectionModel<UserGroup> tenantSingleSelectionModel;
    private TenantListDataProvider tenantListDataProvider;
    private TenantDetailPanel tenantDetailPanel;
    
    public TenantManagementPanel(final UserService userService, final StringMessages stringMessages) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        VerticalPanel west = new VerticalPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        west.add(buttonPanel);
        
        buttonPanel.add(new Button(stringMessages.refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tenantListDataProvider.updateDisplays();
                tenantDetailPanel.updateLists();
            }
        }));
        buttonPanel.add(new Button("Create tenant", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateTenantDialog(stringMessages, userManagementService, tenantListDataProvider).show();
            }
        }));
        buttonPanel.add(new Button("Remove tenant", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroup tenant = tenantSingleSelectionModel.getSelectedObject();
                if (tenant == null) {
                    Window.alert("You have to select a tenant.");
                    return;
                }
                if (Window.confirm("Do you really want to remove tenant " + tenant.getName())) {
                    userManagementService.deleteTenant(tenant.getId().toString(), new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            tenantListDataProvider.updateDisplays();
                            Window.alert(result.getMessage());
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not delete tenant.");
                        }
                    });
                }
            }
        }));
        tenantSingleSelectionModel = new SingleSelectionModel<>();
        TextBox filterBox = new TextBox();
        filterBox.getElement().setPropertyString("placeholder", "Filter tenants...");
        final CellList<Tenant> tenantList = new CellList<Tenant>(new AbstractCell<Tenant>() {
            @Override
            public void render(Context context, Tenant value, SafeHtmlBuilder sb) {
                if (value == null) {
                    return;
                }
                sb.appendEscaped(value.getName());
            }
        });
        tenantList.setSelectionModel(tenantSingleSelectionModel);
        tenantListDataProvider = new TenantListDataProvider(userManagementService, filterBox);
        tenantList.setPageSize(20);
        tenantListDataProvider.addDataDisplay(tenantList);
        SimplePager tenantPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        tenantPager.setDisplay(tenantList);
        ScrollPanel tenantPanel = new ScrollPanel(tenantList);
        VerticalPanel tenantListWrapper = new VerticalPanel();
        tenantListWrapper.add(filterBox);
        tenantListWrapper.add(tenantPanel);
        tenantListWrapper.add(tenantPager);
        CaptionPanel tenantListCaption = new CaptionPanel("Tenants");
        tenantListCaption.add(tenantListWrapper);
        TextBox userFilterBox = new TextBox();
        userFilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        VerticalPanel userListWrapper = new VerticalPanel();
        tenantDetailPanel = new TenantDetailPanel(userFilterBox, tenantSingleSelectionModel, tenantListDataProvider, userManagementService);
        userListWrapper.add(userFilterBox);
        userListWrapper.add(tenantDetailPanel);
        CaptionPanel userListCaption = new CaptionPanel("Users");
        userListCaption.add(userListWrapper);
        HorizontalPanel listsWrapper = new HorizontalPanel();
        listsWrapper.add(tenantListCaption);
        listsWrapper.add(userListCaption);
        west.add(listsWrapper);
        add(west, DockPanel.WEST);
    }
}