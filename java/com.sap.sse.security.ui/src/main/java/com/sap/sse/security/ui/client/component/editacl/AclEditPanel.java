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
    FlowPanel userGroupCellListPanelUi;
    @UiField
    FlowPanel permissionsCellListPanelUi;

    private final Button removeUserGroupButtonUi;
    private final Button addUserGroupButtonUi;
    private final SuggestBox suggestUserGroupUi;

    private final StringMessages stringMessages;

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
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        AclDialogResources.INSTANCE.css().ensureInjected();

        suggestUserGroupUi = createUserGroupSuggest(userManagementService);
        addUserGroupButtonUi = new Button(stringMessages.add());
        removeUserGroupButtonUi = new Button(stringMessages.remove());
        addUserGroupButtonUi.addClickHandler(e -> onUserGroupAdd(e));
        removeUserGroupButtonUi.addClickHandler(e -> onUserGroupRemove(e));

        initWidget(uiBinder.createAndBindUi(this));
        userGroupList = createUserGroupCellList();
        userGroupCellListPanelUi.add(wrapIntoCaptionPanel(userGroupList, stringMessages.userGroups(),
                suggestUserGroupUi, addUserGroupButtonUi, removeUserGroupButtonUi));

        userGroupSingleSelectionModel.addSelectionChangeHandler(h -> {
            onUserGroupsChange();
        });

        // retrieve set of available action names
        final Collection<String> actionNames = new ArrayList<>();
        for (Action a : availableActions) {
            actionNames.add(a.name());
        }

        // create action editor for allowed actions
        allowedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), actionNames,
                stringMessages.allowedActionName());
        allowedActionsEditor.addValueChangeHandler(e -> userGroupsWithAllowedActions
                .put(userGroupSingleSelectionModel.getSelectedObject(), toSet(e.getValue())));

        CaptionPanel allowedActionsPanel = new CaptionPanel(stringMessages.allowedActions());
        allowedActionsPanel.add(allowedActionsEditor);
        allowedActionsPanel.addStyleName(AclDialogResources.INSTANCE.css().allowedActionsTable());
        permissionsCellListPanelUi.add(allowedActionsPanel);

        // create action editor for denied actions
        deniedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), actionNames,
                stringMessages.deniedActionName());
        deniedActionsEditor.addValueChangeHandler(e -> userGroupsWithDeniedActions
                .put(userGroupSingleSelectionModel.getSelectedObject(), toDeniedActionSet(e.getValue())));

        CaptionPanel deniedActionsPanel = new CaptionPanel(stringMessages.deniedActions());
        deniedActionsPanel.add(deniedActionsEditor);
        deniedActionsPanel.addStyleName(AclDialogResources.INSTANCE.css().deniedActionsTable());
        permissionsCellListPanelUi.add(deniedActionsPanel);
    }

    /** Called when the selected {@link UserGroup} changes. */
    private void onUserGroupsChange() {
        removeUserGroupButtonUi.setEnabled(userGroupSingleSelectionModel.getSelectedObject() != null);
    }

    /** @return UI element for selection of {@link UserGroup} elements. */
    private SuggestBox createUserGroupSuggest(UserManagementServiceAsync userManagementService) {
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
        TextBox textBox = new TextBox();
        textBox.getElement().setPropertyString("placeholder", stringMessages.enterUserGroupName());

        SuggestBox suggestBox = new SuggestBox(userGroupOracle, textBox);
        suggestBox.addStyleName(AclDialogResources.INSTANCE.css().userGroupTextBox());
        return suggestBox;
    }

    /** @return the UI element for visualizing {@link UserGroup} elements. */
    private CellList<UserGroup> createUserGroupCellList() {
        final CellList<UserGroup> userGroupCellList = new CellList<>(new AbstractCell<UserGroup>() {
            @Override
            public void render(Context context, UserGroup value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.getName());
                }
            }
        });

        userGroupCellList.setSelectionModel(userGroupSingleSelectionModel);
        userGroupSingleSelectionModel
                .addSelectionChangeHandler(e -> updateActionEditors(userGroupSingleSelectionModel.getSelectedObject()));
        return userGroupCellList;
    }

    /**
     * Updates the {@link #allowedActionsEditor} and {@link #deniedActionsEditor} when the selected UserGroup changed.
     */
    private void updateActionEditors(UserGroup selectedUserGroup) {
        onUserGroupsChange();
        allowedActionsEditor.setValue(userGroupsWithAllowedActions.get(selectedUserGroup), false);
        deniedActionsEditor.setValue(userGroupsWithDeniedActions.get(selectedUserGroup), false);
    }

    /**
     * Wraps the CellList together with a title and additional widgets (e.g. add/remove buttons) into a
     * {@link CaptionPanel}.
     */
    private CaptionPanel wrapIntoCaptionPanel(CellList<?> cellList, String title, Widget... additionalWidgets) {
        cellList.setPageSize(10);
        final SimplePager tenantPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50,
                true);
        tenantPager.setDisplay(cellList);
        final ScrollPanel tenantPanel = new ScrollPanel(cellList);
        final VerticalPanel tenantListWrapper = new VerticalPanel();

        // add additional widgets
        final FlowPanel additionalWidgetsPanel = new FlowPanel();
        for (Widget additionalWidget : additionalWidgets) {
            additionalWidgetsPanel.add(additionalWidget);
            additionalWidget.addStyleName(AclDialogResources.INSTANCE.css().additionalWidget());
        }
        tenantListWrapper.add(additionalWidgetsPanel);
        tenantListWrapper.add(tenantPanel);
        tenantListWrapper.add(tenantPager);

        final CaptionPanel tenantListCaption = new CaptionPanel(title);
        tenantListCaption.add(tenantListWrapper);
        return tenantListCaption;
    }

    /**
     * Updates the ACL edited in the EditAclDialog. Splits combinedActions retrieved by
     * {@link AccessControlList#getActionsByUserGroup()} into {@link #allowedActionsEditor} and
     * {@link #deniedActionsEditor}.
     */
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

    /**
     * Updates the UI by refreshing the {@link #userGroupList} and selects an element to trigger the update of
     * {@link #allowedActionsEditor} and {@link #deniedActionsEditor}.
     */
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

    /** Called when the user clicks on the 'Add' button */
    private void onUserGroupAdd(ClickEvent e) {
        final String userGroupName = suggestUserGroupUi.getValue();

        // get UserGroup object corresponding to user group name
        userManagementService.getUserGroupByName(userGroupName, new AsyncCallback<UserGroup>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorMessageUserGroupNameNotFound(userGroupName),
                        NotificationType.ERROR);
            }

            @Override
            public void onSuccess(UserGroup result) {
                if (result == null) {
                    Notification.notify(stringMessages.errorMessageUserGroupNameNotFound(userGroupName),
                            NotificationType.ERROR);
                } else {
                    Notification.notify(stringMessages.successMessageAddedUserGroup(userGroupName),
                            NotificationType.SUCCESS);
                    userGroupsWithAllowedActions.put(result, new HashSet<>());
                    userGroupsWithDeniedActions.put(result, new HashSet<>());
                    refreshUi();
                    suggestUserGroupUi.setText("");
                    userGroupSingleSelectionModel.setSelected(result, true);
                }
            }
        });
    }

    /** Called when the user clicks on the 'Remove' button */
    private void onUserGroupRemove(ClickEvent e) {
        UserGroup selectedObject = userGroupSingleSelectionModel.getSelectedObject();
        if (selectedObject != null) {
            userGroupsWithAllowedActions.remove(selectedObject);
            userGroupsWithDeniedActions.remove(selectedObject);
            Notification.notify(stringMessages.successMessageRemovedUserGroup(selectedObject.getName()),
                    NotificationType.SUCCESS);
            refreshUi();
        }
    }

    /** Merges {@link #userGroupsWithAllowedActions} and {@link #userGroupsWithDeniedActions}. */
    public Map<UserGroup, Set<String>> getUserGroupsWithCombinedActions() {
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

    /** Converts an {@link Iterable} into a {@link Set}. */
    private <T> Set<T> toSet(Iterable<T> iter) {
        Set<T> resultSet;
        if (iter instanceof Set) {
            resultSet = (Set<T>) iter;
        } else {
            resultSet = new HashSet<>();
            for (T t : iter) {
                resultSet.add(t);
            }
        }
        return resultSet;
    }

    /**
     * Converts an {@link Iterable} into a {@link Set} and adds a '!' in front of each String to mark the action as
     * denied.
     */
    private Set<String> toDeniedActionSet(Iterable<String> iter) {
        final Set<String> set = new HashSet<>();
        for (String s : iter) {
            set.add(s.startsWith("!") ? s : "!" + s);
        }
        return set;
    }

}
