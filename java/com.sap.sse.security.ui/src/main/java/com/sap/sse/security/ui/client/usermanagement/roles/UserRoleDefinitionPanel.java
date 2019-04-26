package com.sap.sse.security.ui.client.usermanagement.roles;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.dto.RoleWithSecurityDTO;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider.UserGroupListDataProviderChangeHandler;
import com.sap.sse.security.ui.client.component.usergroup.roles.RoleDefinitionSuggestOracle;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.RoleAndPermissionDetailsResources;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * Details panel for displaying the roles associated with a user. The panel contains input fields for adding a role to a
 * user and a table with all roles the user currently has.
 */
public class UserRoleDefinitionPanel extends HorizontalPanel
        implements Handler, ChangeHandler, KeyUpHandler, UserGroupListDataProviderChangeHandler {

    private final RoleWithSecurityDTOTableWrapper roleWithSecurityDTOTableWrapper;
    private final SingleSelectionModel<UserDTO> userSelectionModel;
    private final SuggestBox suggestRole;
    private final RoleAndPermissionDetailsResources roleAndPermissionDetailsResources = GWT
            .create(RoleAndPermissionDetailsResources.class);

    public UserRoleDefinitionPanel(final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final CellTableWithCheckboxResources tableResources,
            final MultiSelectionModel<UserDTO> userSelectionModel, final Runnable updateUsers) {

        // create multi to single selection adapter
        final SingleSelectionModel<UserDTO> multiToSingleSelectionModelAdapter = new SingleSelectionModel<>();
        userSelectionModel.addSelectionChangeHandler(event -> {
            multiToSingleSelectionModelAdapter.clear();
            if (userSelectionModel.getSelectedSet().size() != 1) {
                this.setVisible(false);
            } else {
                // has exactly one element in the set
                multiToSingleSelectionModelAdapter.setSelected(userSelectionModel.getSelectedSet().iterator().next(),
                        true);
                this.setVisible(true);
                updateOracle();
                updateRoles();
            }
        });
        this.userSelectionModel = multiToSingleSelectionModelAdapter;

        // create role suggest
        final RoleDefinitionSuggestOracle oracle = new RoleDefinitionSuggestOracle(
                userService.getUserManagementService(), stringMessages);
        suggestRole = new SuggestBox(oracle);
        roleAndPermissionDetailsResources.css().ensureInjected();
        suggestRole.addStyleName(roleAndPermissionDetailsResources.css().enterRoleNameSuggest());
        suggestRole.getElement().setPropertyString("placeholder", stringMessages.enterRoleName());
        this.initPlaceholder(suggestRole, stringMessages.enterRoleName());

        // create role input panel + add controls
        final HorizontalPanel roleInputPanel = new HorizontalPanel();

        final TextBox tenantInput = new TextBox();
        this.initPlaceholder(tenantInput, stringMessages.groupName());

        final TextBox userInput = new TextBox();
        this.initPlaceholder(userInput, stringMessages.username());

        roleInputPanel.add(suggestRole);
        roleInputPanel.add(tenantInput);
        roleInputPanel.add(userInput);

        final Button addRoleButton = new Button(stringMessages.add(), (ClickHandler) event -> {
            StrippedRoleDefinitionDTO role = oracle.fromString(suggestRole.getText());
            if (role != null) {
                UserDTO selectedUser = this.userSelectionModel.getSelectedObject();
                if (selectedUser != null) {
                    userService.getUserManagementService().addRoleToUser(selectedUser.getName(), userInput.getText(),
                            role.getId(), tenantInput.getText(), new AsyncCallback<SuccessInfo>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(caught.getMessage());
                                }

                                @Override
                                public void onSuccess(SuccessInfo result) {
                                    if (result.isSuccessful()) {
                                        updateUsers.run();
                                    } else {
                                        Window.alert(result.getMessage());
                                    }
                                }
                            });
                }
            }
            suggestRole.setText("");
            tenantInput.setText("");
            userInput.setText("");
        });
        final Command addRoleButtonUpdater = () -> addRoleButton.setEnabled(!suggestRole.getValue().isEmpty());
        suggestRole.addKeyUpHandler(event -> addRoleButtonUpdater.execute());
        suggestRole.addSelectionHandler(event -> addRoleButtonUpdater.execute());
        roleInputPanel.add(addRoleButton);

        // create role table
        roleWithSecurityDTOTableWrapper = new RoleWithSecurityDTOTableWrapper(userService, stringMessages,
                errorReporter, /* enablePager */ true, tableResources, this.userSelectionModel, updateUsers);
        final ScrollPanel scrollPanel = new ScrollPanel(roleWithSecurityDTOTableWrapper.asWidget());
        final LabeledAbstractFilterablePanel<RoleWithSecurityDTO> userFilterbox = roleWithSecurityDTOTableWrapper
                .getFilterField();
        userFilterbox.getElement().setPropertyString("placeholder", stringMessages.filterUserGroups());

        // add elements to role panel + add caption
        final VerticalPanel rolePanel = new VerticalPanel();
        rolePanel.add(roleInputPanel);
        rolePanel.add(userFilterbox);
        rolePanel.add(scrollPanel);

        final CaptionPanel captionPanel = new CaptionPanel(stringMessages.roles());
        captionPanel.add(rolePanel);

        this.setVisible(false);

        add(captionPanel);
    }

    private void initPlaceholder(final UIObject target, final String placeholder) {
        target.getElement().setAttribute("placeholder", placeholder);
    }

    /**
     * Updates the SuggestOracle associated with {@link #suggestRole}. This method should be called after the selection
     * has changed.
     */
    private void updateOracle() {
        UserDTO selectedObject = this.userSelectionModel.getSelectedObject();
        if (selectedObject != null) {
            Iterable<StrippedRoleDefinitionDTO> roles = StreamSupport
                    .stream(selectedObject.getRoles().spliterator(), false).map(RoleWithSecurityDTO::getRoleDefinition)
                    .collect(Collectors.toList());
            ((RoleDefinitionSuggestOracle) suggestRole.getSuggestOracle()).resetAndRemoveExistingRoles(roles);
        }
    }

    /** Refreshes the roles in the table. */
    public void updateRoles() {
        roleWithSecurityDTOTableWrapper.refreshRoleList();
    }

    @Override
    public void onChange() {
        updateRoles();
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        updateRoles();
    }

    @Override
    public void onChange(ChangeEvent event) {
        updateRoles();
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
        updateRoles();
    }
}
