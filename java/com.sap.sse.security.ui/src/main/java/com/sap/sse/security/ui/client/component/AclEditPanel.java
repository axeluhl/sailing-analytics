package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class AclEditPanel extends Composite {

    private static AclEditPanelUiBinder uiBinder = GWT.create(AclEditPanelUiBinder.class);

    interface AclEditPanelUiBinder extends UiBinder<Widget, AclEditPanel> {
    }

    @UiField
    Button removeUserGroupButtonUi;
    @UiField
    Button addUserGroupButtonUi;
    @UiField(provided = true)
    SuggestBox suggestUserGroupUi;
    @UiField
    FlowPanel userGroupCellListPanelUi;
    @UiField
    FlowPanel permissionsCellListPanelUi;
    @UiField(provided = true)
    SuggestBox suggestPermissionUi;
    @UiField
    Button removePermissionButtonUi;
    @UiField
    Button addPermissionButtonUi;

    private final SingleSelectionModel<UserGroup> userGroupSingleSelectionModel = new SingleSelectionModel<>();
    private final SingleSelectionModel<String> permissionsSingleSelectionModel = new SingleSelectionModel<>();
    private CellList<UserGroup> userGroupList;
    private CellList<String> permissionsList;

    private Map<UserGroup, Set<String>> userGroupsWithPermissions = new HashMap<>();
    private UserManagementServiceAsync userManagementService;

    public AclEditPanel(UserManagementServiceAsync userManagementService, StringMessages stringMessages) {
        this.userManagementService = userManagementService;
        setupUserGroupSuggest(userManagementService);
        setupPermissionSuggest(stringMessages);
        initWidget(uiBinder.createAndBindUi(this));
        setupCellLists(stringMessages);

        permissionsSingleSelectionModel.addSelectionChangeHandler(h -> {
            removePermissionButtonUi.setEnabled(permissionsSingleSelectionModel.getSelectedObject() != null);
        });

        userGroupSingleSelectionModel.addSelectionChangeHandler(h -> {
            onUserGroupsChange();
        });

        addPermissionButtonUi.setEnabled(false);
    }

    private void onUserGroupsChange() {
        removeUserGroupButtonUi.setEnabled(userGroupSingleSelectionModel.getSelectedObject() != null);
    }

    private void setupPermissionSuggest(StringMessages stringMessages) {
        final MultiWordSuggestOracle permissionOracle = new MultiWordSuggestOracle();

        final List<String> stringPermissions = new ArrayList<>();
        for (final HasPermissions permission : SecuredDomainType.getAllInstances()) {
            for (final Action action : permission.getAvailableActions()) {
                stringPermissions.add(permission.getStringPermission(action));
            }
        }
        permissionOracle.addAll(stringPermissions);
        permissionOracle.setDefaultSuggestionsFromText(stringPermissions);
        suggestPermissionUi = new SuggestBox(permissionOracle, new TextBox(), new DefaultSuggestionDisplay() {
            @Override
            public void hideSuggestions() {
                updatePermissionButtonIfNecessary();
                super.hideSuggestions();
            }
        });
        suggestPermissionUi.addKeyUpHandler(e -> updatePermissionButtonIfNecessary());
    }

    private void updatePermissionButtonIfNecessary() {
        final Set<String> permissionsForSelectedUG = userGroupsWithPermissions
                .get(userGroupSingleSelectionModel.getSelectedObject());
        final String valueToCheck = suggestPermissionUi.getValue();
        final boolean valid = !"".equals(valueToCheck)
                && (permissionsForSelectedUG == null || !permissionsForSelectedUG.contains(valueToCheck));
        addPermissionButtonUi.setEnabled(valid);
    }

    private void setupUserGroupSuggest(UserManagementServiceAsync userManagementService) {
        final MultiWordSuggestOracle userGroupOracle = new MultiWordSuggestOracle();
        userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroup>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Collection<UserGroup> result) {
                final List<String> suggestionList = result.stream().map(UserGroup::getName)
                        .collect(Collectors.toList());
                userGroupOracle.clear();
                userGroupOracle.addAll(suggestionList);
                userGroupOracle.setDefaultSuggestionsFromText(suggestionList);
            }
        });
        suggestUserGroupUi = new SuggestBox(userGroupOracle, new TextBox());
    }

    private void setupCellLists(StringMessages stringMessages) {
        userGroupList = new CellList<UserGroup>(new AbstractCell<UserGroup>() {
            @Override
            public void render(Context context, UserGroup value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.getName());
                }
            }
        });

        userGroupList.setSelectionModel(userGroupSingleSelectionModel);
        userGroupCellListPanelUi.add(wrapListUi(userGroupList, stringMessages.userGroups()));
        userGroupSingleSelectionModel.addSelectionChangeHandler(
                e -> updatePermissionsListUi(userGroupSingleSelectionModel.getSelectedObject()));

        permissionsList = new CellList<String>(new AbstractCell<String>() {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value);
                }
            }
        });

        permissionsList.setSelectionModel(permissionsSingleSelectionModel);
        permissionsCellListPanelUi.add(wrapListUi(permissionsList, stringMessages.permissions()));
    }

    private void updatePermissionsListUi(UserGroup selectedUserGroup) {
        final Set<String> permissions = userGroupsWithPermissions.get(selectedUserGroup);
        permissionsList.setRowCount(permissions.size());
        permissionsList.setRowData(0, new ArrayList<>(permissions));
        onUserGroupsChange();
        updatePermissionButtonIfNecessary();
    }

    private CaptionPanel wrapListUi(CellList<?> cellList, String title) {
        cellList.setPageSize(10);
        final SimplePager tenantPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50,
                true);
        tenantPager.setDisplay(cellList);
        final ScrollPanel tenantPanel = new ScrollPanel(cellList);
        final VerticalPanel tenantListWrapper = new VerticalPanel();
        tenantListWrapper.add(tenantPanel);
        tenantListWrapper.add(tenantPager);
        final CaptionPanel tenantListCaption = new CaptionPanel(title);
        tenantListCaption.add(tenantListWrapper);
        return tenantListCaption;
    }

    public void updateAcl(AccessControlList acl) {
        userGroupsWithPermissions = (acl != null)
                ? acl.getActionsByUserGroup() != null ? new HashMap<>(acl.getActionsByUserGroup()) : new HashMap<>()
                : new HashMap<>();
        refreshUi();
    }

    private void refreshUi() {
        userGroupList.setRowCount(userGroupsWithPermissions.size(), true);
        userGroupList.setRowData(0, new ArrayList<>(userGroupsWithPermissions.keySet()));

        // select an element
        if (userGroupsWithPermissions.size() > 0) {
            userGroupSingleSelectionModel.setSelected(userGroupsWithPermissions.keySet().iterator().next(), true);
        }
    }

    @UiHandler("addPermissionButtonUi")
    void onPermissionAdd(ClickEvent e) {
        final UserGroup selectedUserGroup = userGroupSingleSelectionModel.getSelectedObject();
        if (selectedUserGroup != null && !"".equals(suggestPermissionUi.getText())) {
            Set<String> permissions = userGroupsWithPermissions.get(selectedUserGroup);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            permissions.add(suggestPermissionUi.getText());
            userGroupsWithPermissions.put(selectedUserGroup, permissions);
            updatePermissionsListUi(selectedUserGroup);
            suggestPermissionUi.setText("");
        }
    }

    @UiHandler("removePermissionButtonUi")
    void onPermissionRemove(ClickEvent e) {
        final UserGroup selectedUserGroup = userGroupSingleSelectionModel.getSelectedObject();
        if (selectedUserGroup != null) {
            Set<String> permissions = userGroupsWithPermissions.get(selectedUserGroup);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            final String selectedPermission = permissionsSingleSelectionModel.getSelectedObject();
            permissions.remove(selectedPermission);
            userGroupsWithPermissions.put(selectedUserGroup, permissions);
            updatePermissionsListUi(selectedUserGroup);
        }
    }

    @UiHandler("addUserGroupButtonUi")
    void onUserGroupAdd(ClickEvent e) {
        final String userGroupName = suggestUserGroupUi.getValue();
        userManagementService.getUserGroupByName(userGroupName, new AsyncCallback<UserGroup>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO: i18n
                Notification.notify("Did not find user group by name x" + userGroupName, NotificationType.ERROR);
            }

            @Override
            public void onSuccess(UserGroup result) {
                if (result == null) {
                    Notification.notify("Did not find user group by name x" + userGroupName, NotificationType.ERROR);
                } else {
                    Notification.notify("Added usergroup x" + userGroupName, NotificationType.SUCCESS);
                    userGroupsWithPermissions.put(result, new HashSet<>());
                    refreshUi();
                    suggestUserGroupUi.setText("");
                    userGroupSingleSelectionModel.setSelected(result, true);
                }
            }
        });
    }

    @UiHandler("removeUserGroupButtonUi")
    void onUserGroupRemove(ClickEvent e) {
        userGroupsWithPermissions.remove(userGroupSingleSelectionModel.getSelectedObject());
        refreshUi();
    }

    public Map<UserGroup, Set<String>> getUserGroupsWithPermissions() {
        return userGroupsWithPermissions;
    }

}
