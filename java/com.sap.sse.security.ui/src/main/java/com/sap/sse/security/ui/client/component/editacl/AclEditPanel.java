package com.sap.sse.security.ui.client.component.editacl;

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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.security.shared.AccessControlList;
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

    private final StringListEditorComposite allowedActionsEditor;
    private final StringListEditorComposite deniedActionsEditor;

    private final SingleSelectionModel<UserGroup> userGroupSingleSelectionModel = new SingleSelectionModel<>();
    private CellList<UserGroup> userGroupList;

    // denied actions start with '!'
    private Map<UserGroup, Set<String>> userGroupsWithAllowedActions = new HashMap<>();
    private Map<UserGroup, Set<String>> userGroupsWithDeniedActions = new HashMap<>();
    private UserManagementServiceAsync userManagementService;

    public AclEditPanel(UserManagementServiceAsync userManagementService, Action[] availableActions,
            StringMessages stringMessages) {
        this.userManagementService = userManagementService;
        AclDialogResources.INSTANCE.css().ensureInjected();

        final Collection<String> actionNames = new ArrayList<>();
        for (Action a : availableActions) {
            actionNames.add(a.name());
        }

        setupUserGroupSuggest(userManagementService);
        initWidget(uiBinder.createAndBindUi(this));
        setupCellLists(stringMessages);

        userGroupSingleSelectionModel.addSelectionChangeHandler(h -> {
            onUserGroupsChange();
        });

        // create action editor for allowed actions
        allowedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), actionNames, "Allowed action name");
        allowedActionsEditor.addStyleName(AclDialogResources.INSTANCE.css().allowedActionsTable());
        // TODO: i18n ^v^v

        allowedActionsEditor.addValueChangeHandler(e -> userGroupsWithAllowedActions
                .put(userGroupSingleSelectionModel.getSelectedObject(), toSet(e.getValue())));
        permissionsCellListPanelUi.add(allowedActionsEditor);

        // create action editor for denied actions
        deniedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), actionNames, "Denied action name");

        deniedActionsEditor.addValueChangeHandler(e -> userGroupsWithDeniedActions
                .put(userGroupSingleSelectionModel.getSelectedObject(), toDeniedSet(e.getValue())));
        deniedActionsEditor.addStyleName(AclDialogResources.INSTANCE.css().deniedActionsTable());
        permissionsCellListPanelUi.add(deniedActionsEditor);
    }

    private <T> Set<T> toSet(Iterable<T> iter) {
        final Set<T> set = new HashSet<>();
        for (T t : iter) {
            set.add(t);
        }
        return set;
    }

    private Set<String> toDeniedSet(Iterable<String> iter) {
        final Set<String> set = new HashSet<>();
        for (String s : iter) {
            set.add(s.startsWith("!") ? s : "!" + s);
        }
        return set;
    }

    private void onUserGroupsChange() {
        removeUserGroupButtonUi.setEnabled(userGroupSingleSelectionModel.getSelectedObject() != null);
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
        userGroupSingleSelectionModel
                .addSelectionChangeHandler(e -> updateActionsUi(userGroupSingleSelectionModel.getSelectedObject()));

    }

    private void updateActionsUi(UserGroup selectedUserGroup) {
        onUserGroupsChange();
        allowedActionsEditor.setValue(userGroupsWithAllowedActions.get(selectedUserGroup), false);
        deniedActionsEditor.setValue(userGroupsWithDeniedActions.get(selectedUserGroup), false);
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
        final Map<UserGroup, Set<String>> combinedActions = (acl != null)
                ? acl.getActionsByUserGroup() != null ? new HashMap<>(acl.getActionsByUserGroup()) : new HashMap<>()
                : new HashMap<>();

        for (Map.Entry<UserGroup, Set<String>> combinedAction : combinedActions.entrySet()) {
            final Set<String> allowedActions = new HashSet<>();
            final Set<String> deniedActions = new HashSet<>();
            for (String action : combinedAction.getValue()) {
                if (action.startsWith("!")) {
                    deniedActions.add(action);
                } else {
                    allowedActions.add(action);
                }
            }
            userGroupsWithAllowedActions.put(combinedAction.getKey(), allowedActions);
            userGroupsWithDeniedActions.put(combinedAction.getKey(), deniedActions);
        }

        refreshUi();
    }

    private void refreshUi() {
        final Set<UserGroup> combinedKeySet = new HashSet<>();
        combinedKeySet.addAll(userGroupsWithAllowedActions.keySet());
        combinedKeySet.addAll(userGroupsWithDeniedActions.keySet());
        userGroupList.setRowCount(combinedKeySet.size(), true);
        userGroupList.setRowData(0, new ArrayList<>(combinedKeySet));

        // select an element
        if (combinedKeySet.size() > 0) {
            userGroupSingleSelectionModel.setSelected(combinedKeySet.iterator().next(), true);
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
                    userGroupsWithAllowedActions.put(result, new HashSet<>());
                    userGroupsWithDeniedActions.put(result, new HashSet<>());
                    refreshUi();
                    suggestUserGroupUi.setText("");
                    userGroupSingleSelectionModel.setSelected(result, true);
                }
            }
        });
    }

    @UiHandler("removeUserGroupButtonUi")
    void onUserGroupRemove(ClickEvent e) {
        userGroupsWithAllowedActions.remove(userGroupSingleSelectionModel.getSelectedObject());
        userGroupsWithDeniedActions.remove(userGroupSingleSelectionModel.getSelectedObject());
        refreshUi();
    }

    public Map<UserGroup, Set<String>> getUserGroupsWithPermissions() {
        final Map<UserGroup, Set<String>> combinedActions = new HashMap<>(userGroupsWithAllowedActions);
        for (Map.Entry<UserGroup, Set<String>> actionEntry : userGroupsWithDeniedActions.entrySet()) {
            if (combinedActions.containsKey(actionEntry.getKey())) {
                final Set<String> set = combinedActions.get(actionEntry.getKey());
                set.addAll(actionEntry.getValue());
            } else {
                combinedActions.put(actionEntry.getKey(), actionEntry.getValue());
            }
        }
        return combinedActions;
    }

}
