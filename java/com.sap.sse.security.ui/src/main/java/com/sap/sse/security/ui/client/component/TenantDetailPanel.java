package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.component.TenantListDataProvider.TenantListDataProviderChangeHandler;
import com.sap.sse.security.ui.shared.UserDTO;

public class TenantDetailPanel extends HorizontalPanel implements Handler, ChangeHandler, KeyUpHandler, TenantListDataProviderChangeHandler {
    private final TextBox filterBox;
    private final SingleSelectionModel<UserGroup> tenantSelectionModel;
    
    private final CellList<String> tenantUsersList;
    private final MultiSelectionModel<String> tenantUsersSelectionModel;
    private final TenantUsersListDataProvider tenantUsersListDataProvider;
    private final CellList<UserDTO> allUsersList;
    private final MultiSelectionModel<UserDTO> allUsersSelectionModel;
    private final AllUsersListDataProvider allUsersListDataProvider;
    private UserManagementServiceAsync userManagementService;
    
    private class StringCell extends AbstractCell<String> {
        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }
            sb.appendEscaped(value);
        }
    }
    
    private class UserCell extends AbstractCell<UserDTO> {
        @Override
        public void render(Context context, UserDTO value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }
            sb.appendEscaped(value.getName());
        }
    }
    
    private class TenantUsersListDataProvider extends AbstractDataProvider<String> {
        @Override
        protected void onRangeChanged(HasData<String> display) {
            UserGroup tenant = tenantSelectionModel.getSelectedObject();
            List<String> result = new ArrayList<>();
            List<String> show = new ArrayList<>();
            final Range range = display.getVisibleRange();
            int start = range.getStart();
            int end = range.getStart() + range.getLength();
            if (tenant != null) {
                for (final SecurityUser user : tenant.getUsers()) {
                    if (user.getName().contains(filterBox.getText())) {
                        result.add(user.getName());
                    }
                }
                for (int i = start; i < end && i < result.size(); i++) {
                    final String username = result.get(i);
                    show.add(username);
                }
                
            }
            updateRowData(start, show);
            updateRowCount(result.size(), true);
        }
        
        public void updateDisplays() {
            for (HasData<String> hd : getDataDisplays()) {
                onRangeChanged(hd);
            }
        }
    }
    
    private class AllUsersListDataProvider extends AbstractDataProvider<UserDTO> {
        @Override
        protected void onRangeChanged(HasData<UserDTO> display) {
            UserGroup tenant = tenantSelectionModel.getSelectedObject();
            final List<String> namesOfAlreadyAddedUsers = new ArrayList<>();
            if (tenant != null) {
                for (final SecurityUser tenantUser : tenant.getUsers()) {
                    namesOfAlreadyAddedUsers.add(tenantUser.getName());
                }
            }
            final Range range = display.getVisibleRange();
            userManagementService.getUserList(new AsyncCallback<Collection<UserDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
       
                @Override
                public void onSuccess(Collection<UserDTO> result) {
                    List<UserDTO> resultList = new ArrayList<>();
                    for (UserDTO user : result) {
                        if (!namesOfAlreadyAddedUsers.contains(user.getName()) && 
                                user.getName().contains(filterBox.getText())) {
                            resultList.add(user);
                        }
                    }
                    List<UserDTO> show = new ArrayList<>();
                    int start = range.getStart();
                    int end = range.getStart() + range.getLength();
                    for (int i = start; show.size() < end && i < resultList.size(); i++) {
                        final UserDTO e = resultList.get(i);
                        show.add(e);
                    }
                    updateRowData(start, show);
                    updateRowCount(resultList.size(), true);
                }
            });
        }
        
        public void updateDisplays() {
            for (HasData<UserDTO> hd : getDataDisplays()) {
                onRangeChanged(hd);
            }
        }
    }
    
    public TenantDetailPanel(TextBox filterBox, SingleSelectionModel<UserGroup> tenantSelectionModel, 
            TenantListDataProvider tenantListDataProvider, UserManagementServiceAsync userManagementService) {
        this.filterBox = filterBox;
        filterBox.addChangeHandler(this);
        filterBox.addKeyUpHandler(this);
        tenantSelectionModel.addSelectionChangeHandler(this);
        this.tenantSelectionModel = tenantSelectionModel;
        tenantListDataProvider.addChangeHandler(this);
        this.userManagementService = userManagementService;
        
        final CaptionPanel tenantUsersPanelCaption = new CaptionPanel("Users in tenant");
        final VerticalPanel tenantUsersWrapper = new VerticalPanel();
        tenantUsersList = new CellList<>(new StringCell());
        SimplePager tenantUsersPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        tenantUsersPager.setDisplay(tenantUsersList);
        ScrollPanel tenantUsersPanel = new ScrollPanel(tenantUsersList);
        tenantUsersWrapper.add(tenantUsersPanel);
        tenantUsersWrapper.add(tenantUsersPager);
        tenantUsersPanelCaption.add(tenantUsersWrapper);
        tenantUsersSelectionModel = new MultiSelectionModel<>();
        tenantUsersList.setSelectionModel(tenantUsersSelectionModel);
        tenantUsersListDataProvider = new TenantUsersListDataProvider();
        tenantUsersListDataProvider.addDataDisplay(tenantUsersList);
        final CaptionPanel allUsersPanelCaption = new CaptionPanel("All users");
        final VerticalPanel allUsersWrapper = new VerticalPanel();
        allUsersList = new CellList<>(new UserCell());
        SimplePager allUsersPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        allUsersPager.setDisplay(allUsersList);
        ScrollPanel allUsersPanel = new ScrollPanel(allUsersList);
        allUsersWrapper.add(allUsersPanel);
        allUsersWrapper.add(allUsersPager);
        allUsersPanelCaption.add(allUsersWrapper);
        allUsersSelectionModel = new MultiSelectionModel<>();
        allUsersList.setSelectionModel(allUsersSelectionModel);
        allUsersListDataProvider = new AllUsersListDataProvider();
        allUsersListDataProvider.addDataDisplay(allUsersList);
        
        VerticalPanel movePanel = new VerticalPanel();
        Button addBtn = new Button("<");
        Button removeBtn = new Button(">");
        movePanel.add(addBtn);
        movePanel.add(removeBtn);
        addBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroup tenant = tenantSelectionModel.getSelectedObject();
                Set<UserDTO> users = allUsersSelectionModel.getSelectedSet();
                if (tenant == null) {
                    Window.alert("You have to select a tenant.");
                    return;
                }
                for (UserDTO user : users) {
                    userManagementService.addUserToTenant(tenant.getId().toString(), user.getName(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not add user " + user.getName() + " to tenant.");
                        }
                        @Override
                        public void onSuccess(Void result) {
                            tenantListDataProvider.updateDisplays();
                        }
                    });
                }
            }
        });
        removeBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroup tenant = tenantSelectionModel.getSelectedObject();
                Set<String> users = tenantUsersSelectionModel.getSelectedSet();
                if (tenant == null) {
                    Window.alert("You have to select a tenant.");
                    return;
                }
                for (String username : users) {
                    userManagementService.removeUserFromTenant(tenant.getId().toString(), username, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not remove user " + username + " from tenant.");
                        }
                        @Override
                        public void onSuccess(Void result) {
                            tenantListDataProvider.updateDisplays();
                        }
                    });
                }
            }
        });
        add(tenantUsersPanelCaption);
        add(movePanel);
        setCellVerticalAlignment(movePanel, HasVerticalAlignment.ALIGN_MIDDLE);
        add(allUsersPanelCaption);
    }
    
    public void updateLists() {
        tenantUsersListDataProvider.updateDisplays();
        allUsersListDataProvider.updateDisplays();
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
        updateLists();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        updateLists();
    }

    @Override
    public void onChange(ChangeEvent event) {
        updateLists();
    }
    
    @Override
    public void onChange() {
        updateLists();
    }
}
