package com.sap.sse.security.ui.client.component.usergroup.users;

import static com.sap.sse.security.shared.impl.SecuredSecurityTypes.USER_GROUP;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider;
import com.sap.sse.security.ui.client.component.UserGroupListDataProvider.UserGroupListDataProviderChangeHandler;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserGroupDetailPanel extends HorizontalPanel
        implements Handler, ChangeHandler, KeyUpHandler, UserGroupListDataProviderChangeHandler {

    private final UserGroupUserResources userGroupUserResources = GWT.create(UserGroupUserResources.class);
    private final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel;
    private final CellList<String> tenantUsersList;
    private final MultiSelectionModel<String> tenantUsersSelectionModel;
    private final TenantUsersListDataProvider tenantUsersListDataProvider;

    private UserGroupSuggestOracle oracle;

    private class StringCell extends AbstractCell<String> {
        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }
            sb.appendEscaped(value);
        }
    }

    private class TenantUsersListDataProvider extends AbstractDataProvider<String> {

        private final TextBox filterBox;

        public TenantUsersListDataProvider(TextBox filterBox) {
            this.filterBox = filterBox;
        }

        @Override
        protected void onRangeChanged(HasData<String> display) {
            final UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
            final List<String> result = new ArrayList<>();
            final List<String> show = new ArrayList<>();
            final Range range = display.getVisibleRange();
            int start = range.getStart();
            int end = range.getStart() + range.getLength();
            if (tenant != null) {
                for (final StrippedUserDTO user : tenant.getUsers()) {
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

    public UserGroupDetailPanel(SingleSelectionModel<UserGroupDTO> refreshableSelectionModel,
            UserGroupListDataProvider tenantListDataProvider, UserService userService, StringMessages stringMessages) {
        userGroupUserResources.css().ensureInjected();

        // setup filter
        final FlowPanel filterPanel = new FlowPanel();
        final TextBox filterBox = new TextBox();
        filterBox.addStyleName(userGroupUserResources.css().filterUsers());
        filterBox.addChangeHandler(this);
        filterBox.addKeyUpHandler(this);
        final InlineLabel labelFilter = new InlineLabel(stringMessages.filterUsers());

        filterPanel.add(labelFilter);
        filterPanel.add(filterBox);

        refreshableSelectionModel.addSelectionChangeHandler(this);
        this.userGroupSelectionModel = refreshableSelectionModel;
        tenantListDataProvider.addChangeHandler(this);

        // setup caption, scrolling and paging
        final CaptionPanel tenantUsersPanelCaption = new CaptionPanel(stringMessages.usersInUserGroup());
        final VerticalPanel tenantUsersWrapper = new VerticalPanel();
        tenantUsersList = new CellList<>(new StringCell());
        final SimplePager tenantUsersPager = new SimplePager(TextLocation.CENTER, false,
                /* fast forward step size */ 50, true);
        tenantUsersPager.setDisplay(tenantUsersList);
        final ScrollPanel tenantUsersPanel = new ScrollPanel(tenantUsersList);
        tenantUsersWrapper.add(tenantUsersPanel);
        tenantUsersWrapper.add(tenantUsersPager);
        tenantUsersPanelCaption.add(tenantUsersWrapper);

        // setup user list
        tenantUsersSelectionModel = new MultiSelectionModel<>();
        tenantUsersList.setSelectionModel(tenantUsersSelectionModel);
        tenantUsersListDataProvider = new TenantUsersListDataProvider(filterBox);
        tenantUsersListDataProvider.addDataDisplay(tenantUsersList);

        // add buttons, filter and listbox to panel
        final VerticalPanel addUserToGroupPanel = new VerticalPanel();
        addUserToGroupPanel.add(createButtonPanel(userService, stringMessages, userService.getUserManagementService()));
        addUserToGroupPanel.add(filterPanel);
        addUserToGroupPanel.add(tenantUsersPanelCaption);

        add(addUserToGroupPanel);
        setCellVerticalAlignment(addUserToGroupPanel, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    /** Creates the button bar with add/remove/refresh buttons and the SuggestBox. */
    private Widget createButtonPanel(final UserService userService, final StringMessages stringMessages,
            final UserManagementServiceAsync userManagementService) {
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, USER_GROUP);

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
            if (!tenantUsersList.getVisibleItems().contains(selectedUsername)) {
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
        buttonPanel.addRemoveAction(stringMessages.removeRole(), () -> {
            UserGroupDTO tenant = userGroupSelectionModel.getSelectedObject();
            Set<String> users = tenantUsersSelectionModel.getSelectedSet();
            if (tenant == null) {
                Window.alert(stringMessages.youHaveToSelectAUserGroup());
                return;
            }
            for (String username : users) {
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
        oracle.resetAndRemoveExistingUsers(tenantUsersList.getVisibleItems());
    }
}
