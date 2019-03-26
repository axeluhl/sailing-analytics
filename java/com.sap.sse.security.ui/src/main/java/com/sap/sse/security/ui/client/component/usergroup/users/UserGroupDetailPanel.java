package com.sap.sse.security.ui.client.component.usergroup.users;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.UPDATE;
import static com.sap.sse.security.shared.impl.SecuredSecurityTypes.USER_GROUP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider.UserGroupListDataProviderChangeHandler;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserGroupDetailPanel extends Composite
        implements Handler, ChangeHandler, KeyUpHandler, UserGroupListDataProviderChangeHandler {

    private final UserGroupUserResources userGroupUserResources = GWT.create(UserGroupUserResources.class);
    private final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel;
    private final UserGroupUsersTableWrapper tenantUsersTable;
    private final TenantUsersListDataProvider tenantUsersListDataProvider;

    private UserGroupSuggestOracle oracle;

    private class TenantUsersListDataProvider extends AbstractDataProvider<StrippedUserDTO> {

        private final TextBox filterBox;

        private TenantUsersListDataProvider(TextBox filterBox) {
            this.filterBox = filterBox;
        }

        @Override
        protected void onRangeChanged(HasData<StrippedUserDTO> display) {
            final UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
            final List<StrippedUserDTO> result = new ArrayList<>();
            final List<StrippedUserDTO> show = new ArrayList<>();
            final Range range = display.getVisibleRange();
            int start = range.getStart();
            int end = range.getStart() + range.getLength();
            if (tenant != null) {
                for (final StrippedUserDTO user : tenant.getUsers()) {
                    if (user.getName().contains(filterBox.getText())) {
                        result.add(user);
                    }
                }
                for (int i = start; i < end && i < result.size(); i++) {
                    final StrippedUserDTO username = result.get(i);
                    show.add(username);
                }

            }
            updateRowData(start, show);
            updateRowCount(result.size(), true);
        }

        public void updateDisplays() {
            getDataDisplays().forEach(this::onRangeChanged);
        }
    }

    public UserGroupDetailPanel(SingleSelectionModel<UserGroupDTO> refreshableSelectionModel,
            UserGroupListDataProvider tenantListDataProvider, UserService userService, StringMessages stringMessages,
            ErrorReporter errorReporter, CellTableWithCheckboxResources tableResources) {
        userGroupUserResources.css().ensureInjected();

        // setup filter
        final HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        filterPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        final TextBox filterBox = new TextBox();
        filterBox.addChangeHandler(this);
        filterBox.addKeyUpHandler(this);
        final Label labelFilter = new Label(stringMessages.filterUsers());

        filterPanel.add(labelFilter);
        filterPanel.add(filterBox);

        refreshableSelectionModel.addSelectionChangeHandler(this);
        this.userGroupSelectionModel = refreshableSelectionModel;
        tenantListDataProvider.addChangeHandler(this);

        this.tenantUsersTable = new UserGroupUsersTableWrapper(stringMessages, errorReporter, tableResources,
                userService, userGroupSelectionModel, () -> updateUserList());

        tenantUsersListDataProvider = new TenantUsersListDataProvider(filterBox);
        tenantUsersListDataProvider.addDataDisplay(tenantUsersTable.getTable());

        // add buttons, filter and listbox to panel
        final VerticalPanel addUserToGroupPanel = new VerticalPanel();
        final Widget buttonPanel = createButtonPanel(userService, stringMessages);
        this.userGroupSelectionModel.addSelectionChangeHandler(event -> {
            final UserGroupDTO selectedUserGroup = this.userGroupSelectionModel.getSelectedObject();
            buttonPanel.setVisible(userService.hasPermission(selectedUserGroup, UPDATE));
        });
        addUserToGroupPanel.add(buttonPanel);
        addUserToGroupPanel.add(filterPanel);
        // addUserToGroupPanel.add(tenantUsersPanelCaption);
        addUserToGroupPanel.add(tenantUsersTable);

        initWidget(addUserToGroupPanel);
    }

    /** Creates the button bar with add/remove/refresh buttons and the SuggestBox. */
    private Widget createButtonPanel(final UserService userService, final StringMessages stringMessages) {
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, USER_GROUP);
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        // setup suggest
        this.oracle = new UserGroupSuggestOracle(userManagementService, stringMessages);
        final SuggestBox suggestUser = new SuggestBox(oracle);
        suggestUser.addStyleName(userGroupUserResources.css().userDefinitionSuggest());
        suggestUser.getElement().setPropertyString("placeholder", stringMessages.enterUsername());

        // add suggest
        buttonPanel.insertWidgetAtPosition(suggestUser, 0);

        // add add button
        buttonPanel.addCreateAction(stringMessages.addUser(), () -> {
            final String selectedUsername = suggestUser.getValue();
            if (!getSelectedUserGroupUsernames().contains(selectedUsername)) {
                final UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
                userManagementService.addUserToUserGroup(tenant.getId().toString(), selectedUsername,
                        new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.couldNotAddUserToUserGroup(selectedUsername,
                                        tenant.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                tenant.add(new StrippedUserDTO(selectedUsername));
                                updateUserList();
                                suggestUser.setText("");
                            }
                        });
            }
        });

        // add remove button
        final Button removeButton = buttonPanel.addRemoveAction(stringMessages.removeRole(), () -> {
            UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
            Set<StrippedUserDTO> users = tenantUsersTable.getSelectionModel().getSelectedSet();
            if (tenant == null) {
                Window.alert(stringMessages.youHaveToSelectAUserGroup());
                return;
            }
            for (StrippedUserDTO user : users) {
                final String username = user.getName();
                userManagementService.removeUserFromUserGroup(tenant.getId().toString(), username,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.couldNotRemoveUserFromUserGroup(username, tenant.getName(),
                                        caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                StrippedUserDTO userToRemoveFromTenant = null;
                                for (final StrippedUserDTO userInTenant : tenant.getUsers()) {
                                    if (Util.equalsWithNull(userInTenant.getName(), username)) {
                                        userToRemoveFromTenant = userInTenant;
                                        break;
                                    }
                                }
                                if (userToRemoveFromTenant != null) {
                                    tenant.remove(userToRemoveFromTenant);
                                }
                                updateUserList();
                            }
                        });
            }
        });
        tenantUsersTable.getSelectionModel().addSelectionChangeHandler(
                event -> removeButton.setEnabled(!tenantUsersTable.getSelectionModel().getSelectedSet().isEmpty()));
        removeButton.setEnabled(false);
        return buttonPanel;
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
        updateUserList();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        updateUserList();
    }

    @Override
    public void onChange(ChangeEvent event) {
        updateUserList();
    }

    @Override
    public void onChange() {

    }

    public void updateUserList() {
        tenantUsersListDataProvider.updateDisplays();
        oracle.resetAndRemoveExistingUsers(getSelectedUserGroupUsernames());
    }

    private List<String> getSelectedUserGroupUsernames() {
        final UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
        return tenant == null ? Collections.emptyList()
                : Util.asList(tenant.getUsers()).stream().map(StrippedUserDTO::getName).collect(Collectors.toList());
    }
}
