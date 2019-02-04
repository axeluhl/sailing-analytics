package com.sap.sse.security.ui.client.component;

import static com.sap.sse.security.shared.impl.SecuredSecurityTypes.USER_GROUP;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.usergroup.roles.UserGroupRoleDefinitionPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * Shows a {@link UserGroupTableWrapper} with an overview over all existing user groups and allows them to be edited via
 * a {@link UserGroupDetailPanel}.
 */
public class UserGroupManagementPanel extends DockPanel {
    private final UserGroupListDataProvider userGroupListDataProvider;
    private UserGroupDetailPanel userGroupDetailPanel;
    private UserGroupRoleDefinitionPanel userGroupRoleDefinitionPanel;

    private final UserGroupTableWrapper userGroupTableWrapper;

    public UserGroupManagementPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter,
            CellTableWithCheckboxResources tableResources) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        final VerticalPanel west = new VerticalPanel();

        // create button bar
        west.add(createButtonPanel(userService, stringMessages, userManagementService));

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
        final HorizontalPanel listsWrapper = createUserGroupDetailsPanel(stringMessages, userManagementService,
                userService, additionalPermissions, errorReporter, tableResources);
        west.add(listsWrapper);

        add(west, DockPanel.WEST);

    }

    /** Creates the button bar with add/remove/refresh buttons. */
    private Widget createButtonPanel(final UserService userService, final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService) {
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, USER_GROUP);
        buttonPanel.addUnsecuredAction(stringMessages.refresh(), () -> updateUserGroups());
        buttonPanel.addCreateAction(stringMessages.createUserGroup(), () -> new CreateUserGroupDialog(stringMessages,
                userService, userManagementService, userGroupListDataProvider).show());
        buttonPanel.addRemoveAction(stringMessages.removeUserGroup(), () -> {
            UserGroupDTO userGroup = userGroupTableWrapper.getSelectionModel().getSelectedObject();
            if (userGroup == null) {
                Window.alert(stringMessages.youHaveToSelectAUserGroup());
            } else if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(userGroup.getName()))) {
                userManagementService.deleteUserGroup(userGroup.getId().toString(), new AsyncCallback<SuccessInfo>() {
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
        });
        return buttonPanel;
    }

    /** Creates the UserGroupDetailsPanel which contains details about the selected user group 
     * @param userService 
     * @param additionalPermissions 
     * @param errorReporter 
     * @param tableResources */
    private HorizontalPanel createUserGroupDetailsPanel(final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService, UserService userService, Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter, CellTableWithCheckboxResources tableResources) {
        final TextBox userFilterBox = new TextBox();
        userFilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        userGroupDetailPanel = new UserGroupDetailPanel(userFilterBox, userGroupTableWrapper.getSelectionModel(),
                userGroupListDataProvider, userManagementService, stringMessages);

        
        userGroupRoleDefinitionPanel = new UserGroupRoleDefinitionPanel(userService,
                stringMessages, additionalPermissions, errorReporter, tableResources,
                userGroupTableWrapper.getSelectionModel(), userGroupListDataProvider);

        final VerticalPanel userListWrapper = new VerticalPanel();
        userListWrapper.add(userFilterBox);
        userListWrapper.add(userGroupDetailPanel);
        final CaptionPanel userListCaption = new CaptionPanel(stringMessages.users());
        userListCaption.add(userListWrapper);

        final VerticalPanel roleWrapper = new VerticalPanel();
        roleWrapper.add(userGroupRoleDefinitionPanel);
        final CaptionPanel roleCaption = new CaptionPanel(stringMessages.roles());
        roleCaption.add(roleWrapper);

        final HorizontalPanel listsWrapper = new HorizontalPanel();
        listsWrapper.add(userListCaption);
        listsWrapper.add(roleCaption);
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
        userGroupRoleDefinitionPanel.updateUserGroups();
    }
}