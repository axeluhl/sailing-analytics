package com.sap.sse.security.ui.client.component;

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
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * Shows a {@link UserGroupTableWrapper} with an overview over all existing user groups and allows them to be edited via
 * a {@link UserGroupDetailPanel}.
 */
public class UserGroupManagementPanel extends DockPanel {
    private final UserGroupListDataProvider userGroupListDataProvider;
    private UserGroupDetailPanel userGroupDetailPanel;
    private Button editACLForNullUsergroup = null;

    private final UserGroupTableWrapper userGroupTableWrapper;

    public UserGroupManagementPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter,
            CellTableWithCheckboxResources tableResources) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        final VerticalPanel west = new VerticalPanel();

        // create button bar
        final HorizontalPanel buttonPanel = createButtonPanel(userService, stringMessages, userManagementService);
        west.add(buttonPanel);

        // create UserGroup Table
        userGroupListDataProvider = new UserGroupListDataProvider(userManagementService, new TextBox());
        userGroupTableWrapper = new UserGroupTableWrapper(userService, additionalPermissions, stringMessages,
                errorReporter, /* enablePager */ true, tableResources, () -> updateUserGroups());

        final ScrollPanel scrollPanel = new ScrollPanel(userGroupTableWrapper.asWidget());
        final LabeledAbstractFilterablePanel<UserGroupDTO> userGroupfilterBox = userGroupTableWrapper.getFilterField();
        userGroupfilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());

        west.add(userGroupfilterBox);
        west.add(scrollPanel);

        // create Details Panel
        final HorizontalPanel listsWrapper = createUserGroupDetailsPanel(stringMessages, userManagementService);
        west.add(listsWrapper);

        add(west, DockPanel.WEST);

    }

    /** Creates the button bar with add/remove/refresh buttons. */
    private HorizontalPanel createButtonPanel(final UserService userService, final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService) {
        final HorizontalPanel buttonPanel = new HorizontalPanel();

        buttonPanel.add(new Button(stringMessages.refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateUserGroups();
            }
        }));
        buttonPanel.add(new Button(stringMessages.createUserGroup(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserGroupDialog(stringMessages, userService, userManagementService, userGroupListDataProvider)
                        .show();
            }
        }));
        editACLForNullUsergroup = new Button("Edit ACL for Null Usergroup", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                userManagementService.getAccessControlList(null, new AsyncCallback<AccessControlListAnnotationDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSuccess(AccessControlListAnnotationDTO result) {
                        final EditACLDialog.DialogConfig<UserGroupDTO> configACL = EditACLDialog.create(
                                userService.getUserManagementService(), SecuredSecurityTypes.USER_GROUP,
                                user -> result.getAnnotation(), stringMessages);
                        configACL.openDialog(null);
                    }
                });
            }
        });
        buttonPanel.add(editACLForNullUsergroup);
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
                                    updateUserGroups();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotDeleteUserGroup());
                                }
                            });
                }
            }
        }));
        return buttonPanel;
    }

    /** Creates the UserGroupDetailsPanel which contains details about the selected user group */
    private HorizontalPanel createUserGroupDetailsPanel(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService) {
        final TextBox userFilterBox = new TextBox();
        userFilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        userGroupDetailPanel = new UserGroupDetailPanel(userFilterBox, userGroupTableWrapper.getSelectionModel(),
                userGroupListDataProvider, userManagementService, stringMessages);

        final VerticalPanel userListWrapper = new VerticalPanel();
        userListWrapper.add(userFilterBox);
        userListWrapper.add(userGroupDetailPanel);
        final CaptionPanel userListCaption = new CaptionPanel(stringMessages.users());
        userListCaption.add(userListWrapper);
        final HorizontalPanel listsWrapper = new HorizontalPanel();
        listsWrapper.add(userListCaption);
        listsWrapper.setVisible(false);
        userGroupTableWrapper.getSelectionModel().addSelectionChangeHandler(
                h -> listsWrapper.setVisible(userGroupTableWrapper.getSelectionModel().getSelectedObject() != null));
        return listsWrapper;
    }

    /** Updates the UserGroups. */
    public void updateUserGroups() {
        userGroupTableWrapper.refreshUserList(null);
        userGroupListDataProvider.updateDisplays();
        userGroupDetailPanel.updateLists();
    }
}