package com.sap.sse.security.ui.client.component;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class UserGroupManagementPanel extends DockPanel {
    // private SingleSelectionModel<UserGroupDTO> userGroupSingleSelectionModel;
    private UserGroupListDataProvider userGroupListDataProvider;
    private UserGroupDetailPanel userGroupDetailPanel;

    private final UserGroupTableWrapper userGroupTableWrapper;

    private UserService userService;

    public UserGroupManagementPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter,
            CellTableWithCheckboxResources tableResources) {
        this.userService = userService;
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
                new CreateUserGroupDialog(stringMessages, userService, userManagementService, userGroupListDataProvider)
                        .show();
            }
        }));
        buttonPanel.add(new Button(stringMessages.removeUserGroup(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroupDTO userGroup = userGroupTableWrapper.getSelectionModel().getSelectedObject();
                if (userGroup == null) {
                    Window.alert(stringMessages.youHaveToSelectAUserGroup());
                } else if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(userGroup.getName()))) {
                    userManagementService.deleteUserGroup(userGroup.getId().toString(),
                            new AsyncCallback<SuccessInfo>() {
                                @Override
                                public void onSuccess(SuccessInfo result) {
                                    userGroupListDataProvider.updateDisplays();
                                    Window.alert(result.getMessage());
                                    updateUserGroupsAndUsers();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotDeleteUserGroup());
                                }
                            });
                }
            }
        }));
        final TextBox filterBox = new TextBox();
        filterBox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());
        userGroupListDataProvider = new UserGroupListDataProvider(userManagementService, new TextBox());
        TextBox userFilterBox = new TextBox();
        userFilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());

        // User Group Table
        userGroupTableWrapper = new UserGroupTableWrapper(userService, additionalPermissions, stringMessages,
                errorReporter, /* enablePager */ true, tableResources, () -> updateUserGroupsAndUsers());

        ScrollPanel scrollPanel = new ScrollPanel(userGroupTableWrapper.asWidget());
        LabeledAbstractFilterablePanel<UserGroupDTO> usergroupfilterBox = userGroupTableWrapper
                .getFilterField();
        usergroupfilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());
        west.add(usergroupfilterBox);
        west.add(scrollPanel);
        add(west, DockPanel.WEST);

        userGroupDetailPanel = new UserGroupDetailPanel(userFilterBox, userGroupTableWrapper.getSelectionModel(),
                userGroupListDataProvider, userManagementService, stringMessages);

        VerticalPanel userListWrapper = new VerticalPanel();
        userListWrapper.add(userFilterBox);
        userListWrapper.add(userGroupDetailPanel);
        CaptionPanel userListCaption = new CaptionPanel(stringMessages.users());
        userListCaption.add(userListWrapper);
        HorizontalPanel listsWrapper = new HorizontalPanel();
        listsWrapper.add(userListCaption);
        west.add(listsWrapper);

        listsWrapper.setVisible(false);
        userGroupTableWrapper.getSelectionModel().addSelectionChangeHandler(
                h -> listsWrapper.setVisible(userGroupTableWrapper.getSelectionModel().getSelectedObject() != null));
        add(west, DockPanel.WEST);

    }

    public void updateUserGroupsAndUsers() {
        userService.getUserManagementService()
                .getUserGroups(new AsyncCallback<Collection<UserGroupDTO>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Collection<UserGroupDTO> result) {
                        userGroupTableWrapper.refreshUserGroups(result);
                    }
                });
        userGroupListDataProvider.updateDisplays();
        userGroupDetailPanel.updateLists();
    }
}