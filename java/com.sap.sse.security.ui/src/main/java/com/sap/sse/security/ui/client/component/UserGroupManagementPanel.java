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
import com.sap.sse.security.shared.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class UserGroupManagementPanel extends DockPanel {
    private SingleSelectionModel<UserGroupDTO> userGroupSingleSelectionModel;
    private UserGroupListDataProvider userGroupListDataProvider;
    private UserGroupDetailPanel userGroupDetailPanel;
    
    public UserGroupManagementPanel(final UserService userService, final StringMessages stringMessages) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        VerticalPanel west = new VerticalPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        west.add(buttonPanel);
        
        buttonPanel.add(new Button(stringMessages.refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateUserGroupsAndUsers();
            }
        }));
        buttonPanel.add(new Button(stringMessages.createUserGroup(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserGroupDialog(stringMessages, userService, userManagementService, userGroupListDataProvider).show();
            }
        }));
        buttonPanel.add(new Button(stringMessages.removeUserGroup(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroupDTO userGroup = userGroupSingleSelectionModel.getSelectedObject();
                if (userGroup == null) {
                    Window.alert(stringMessages.youHaveToSelectAUserGroup());
                } else if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(userGroup.getName()))) {
                    userManagementService.deleteUserGroup(userGroup.getId().toString(), new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            userGroupListDataProvider.updateDisplays();
                            Window.alert(result.getMessage());
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(stringMessages.couldNotDeleteUserGroup());
                        }
                    });
                }
            }
        }));
        userGroupSingleSelectionModel = new SingleSelectionModel<>();
        TextBox filterBox = new TextBox();
        filterBox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());
        final CellList<UserGroupDTO> userGroupList = new CellList<UserGroupDTO>(new AbstractCell<UserGroupDTO>() {
            @Override
            public void render(Context context, UserGroupDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.getName());
                }
            }
        });
        userGroupList.setSelectionModel(userGroupSingleSelectionModel);
        userGroupListDataProvider = new UserGroupListDataProvider(userManagementService, filterBox);
        userGroupList.setPageSize(20);
        userGroupListDataProvider.addDataDisplay(userGroupList);
        SimplePager tenantPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        tenantPager.setDisplay(userGroupList);
        ScrollPanel tenantPanel = new ScrollPanel(userGroupList);
        VerticalPanel tenantListWrapper = new VerticalPanel();
        tenantListWrapper.add(filterBox);
        tenantListWrapper.add(tenantPanel);
        tenantListWrapper.add(tenantPager);
        CaptionPanel tenantListCaption = new CaptionPanel(stringMessages.userGroups());
        tenantListCaption.add(tenantListWrapper);
        TextBox userFilterBox = new TextBox();
        userFilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        VerticalPanel userListWrapper = new VerticalPanel();
        userGroupDetailPanel = new UserGroupDetailPanel(userFilterBox, userGroupSingleSelectionModel, userGroupListDataProvider, userManagementService, stringMessages);
        userListWrapper.add(userFilterBox);
        userListWrapper.add(userGroupDetailPanel);
        CaptionPanel userListCaption = new CaptionPanel(stringMessages.users());
        userListCaption.add(userListWrapper);
        HorizontalPanel listsWrapper = new HorizontalPanel();
        listsWrapper.add(tenantListCaption);
        listsWrapper.add(userListCaption);
        west.add(listsWrapper);
        add(west, DockPanel.WEST);
    }

    public void updateUserGroupsAndUsers() {
        userGroupListDataProvider.updateDisplays();
        userGroupDetailPanel.updateLists();
    }
}