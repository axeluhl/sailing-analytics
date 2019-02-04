package com.sap.sse.security.ui.client.component.usergroup.roles;

import static com.sap.sse.security.shared.impl.SecuredSecurityTypes.USER_GROUP;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider.UserGroupListDataProviderChangeHandler;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserGroupRoleDefinitionPanel extends HorizontalPanel
        implements Handler, ChangeHandler, KeyUpHandler, UserGroupListDataProviderChangeHandler {

    private final RoleDefinitionTableWrapper roleDefinitionTableWrapper;

    private final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel;

    private final SuggestBox suggestRole;

    private final UserGroupRoleResources userGroupRoleResources = GWT.create(UserGroupRoleResources.class);

    public UserGroupRoleDefinitionPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter,
            CellTableWithCheckboxResources tableResources,
            final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel,
            UserGroupListDataProvider userGroupListDataProvider) {
        this.userGroupSelectionModel = userGroupSelectionModel;
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        final VerticalPanel west = new VerticalPanel();

        suggestRole = new SuggestBox(new RoleDefinitionSuggestOracle(userManagementService, stringMessages));
        userGroupRoleResources.css().ensureInjected();
        suggestRole.addStyleName(userGroupRoleResources.css().roleDefinitionSuggest());
        suggestRole.getElement().setPropertyString("placeholder", stringMessages.enterRoleName());

        // create button bar
        west.add(createButtonPanel(userService, stringMessages, userManagementService));

        // create UserGroup Table
        userGroupListDataProvider.addChangeHandler(this);
        roleDefinitionTableWrapper = new RoleDefinitionTableWrapper(userService, additionalPermissions, stringMessages,
                errorReporter, /* enablePager */ true, tableResources, () -> updateUserGroups(),
                userGroupSelectionModel);

        final ScrollPanel scrollPanel = new ScrollPanel(roleDefinitionTableWrapper.asWidget());
        final LabeledAbstractFilterablePanel<Pair<StrippedRoleDefinitionDTO, Boolean>> userGroupfilterBox = roleDefinitionTableWrapper
                .getFilterField();
        userGroupfilterBox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());

        west.add(userGroupfilterBox);
        west.add(scrollPanel);

        add(west);

        this.userGroupSelectionModel.addSelectionChangeHandler(e -> updateOracle());
    }

    /**
     * Updates the SuggestOracle associated with {@link #suggestRole}. This method should be called after the selection
     * has changed.
     */
    private void updateOracle() {
        UserGroupDTO selectedObject = this.userGroupSelectionModel.getSelectedObject();
        if (selectedObject != null) {
            ((RoleDefinitionSuggestOracle) suggestRole.getSuggestOracle())
                    .resetAndRemoveExistingRoles(selectedObject.getRoleDefinitionMap().keySet());
        }
    }

    /** Creates the button bar with add/remove/refresh buttons and the SuggestBox. */
    private Widget createButtonPanel(final UserService userService, final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService) {
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, USER_GROUP);
        buttonPanel.addCreateAction(stringMessages.addRole(), () -> {
            final UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
            if (selectedObject != null) {
                StrippedRoleDefinitionDTO role = ((RoleDefinitionSuggestOracle) suggestRole.getSuggestOracle())
                        .fromString(suggestRole.getValue());
                if (role != null) {
                    userManagementService.putRoleDefintionToUserGroup(selectedObject.getId().toString(),
                            role.getId().toString(), false, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotAddRoleToGroup(role.getName(),
                                            selectedObject.getName()));
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    selectedObject.put(role, false);
                                    suggestRole.setValue("");
                                    updateUserGroups();
                                }
                            });
                }
            }
        });
        buttonPanel.addRemoveAction(stringMessages.removeRole(), () -> {
            Pair<StrippedRoleDefinitionDTO, Boolean> selectedRole = roleDefinitionTableWrapper.getSelectionModel()
                    .getSelectedObject();
            if (selectedRole == null) {
                Window.alert(stringMessages.youHaveToSelectAUserGroup());
            } else if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(selectedRole.getA().getName()))) {
                UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
                if (selectedObject != null) {
                    userManagementService.removeRoleDefintionFromUserGroup(selectedObject.getId().toString(),
                            selectedRole.getA().getId().toString(), new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotDeleteRole(selectedRole.getA().getName()));
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    selectedObject.remove(selectedRole.getA());
                                    updateUserGroups();
                                }
                            });
                } else {
                    Window.alert(stringMessages.pleaseSelect());
                }
            }
        });

        buttonPanel.insertWidgetAtPosition(suggestRole, 1);

        return buttonPanel;
    }

    /** Updates the UserGroups. */
    public void updateUserGroups() {
        updateOracle();
        roleDefinitionTableWrapper.refreshRoleList();
    }

    @Override
    public void onChange() {
        updateUserGroups();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        updateUserGroups();
    }

    @Override
    public void onChange(ChangeEvent event) {
        updateUserGroups();
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
        updateUserGroups();
    }
}
