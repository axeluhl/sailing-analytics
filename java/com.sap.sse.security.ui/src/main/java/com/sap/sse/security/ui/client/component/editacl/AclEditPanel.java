package com.sap.sse.security.ui.client.component.editacl;

import static com.sap.sse.gwt.client.Notification.NotificationType.ERROR;
import static com.sap.sse.gwt.client.Notification.NotificationType.SUCCESS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.UserGroupImpl;
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
    @UiField
    Label lblId;
    @UiField
    Label lblType;

    private final Button removeUserGroupButtonUi;
    private final Button addUserGroupButtonUi;
    private final SuggestBox suggestUserGroupUi;

    private final StringMessages stringMessages;

    private final StringListEditorComposite allowedActionsEditor, deniedActionsEditor;
    private final CaptionPanel allowedActionsContainer, deniedActionsContainer;

    private final SingleSelectionModel<StrippedUserGroupDTO> userGroupSelectionModel = new SingleSelectionModel<>();
    private final ListDataProvider<StrippedUserGroupDTO> userGroupDataProvider = new ListDataProvider<>();

    // denied actions start with '!'
    private final Map<StrippedUserGroupDTO, Set<String>> userGroupsWithAllowedActions = new HashMap<>();
    private final Map<StrippedUserGroupDTO, Set<String>> userGroupsWithDeniedActions = new HashMap<>();
    private final UserManagementServiceAsync userManagementService;

    private final StrippedUserGroupDTO nullUserGroup;

    public AclEditPanel(final UserManagementServiceAsync userManagementService, final Action[] availableActions,
            final StringMessages stringMessages, String typeIdentifier, String id) {
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        this.nullUserGroup = new StrippedUserGroupDTO(null, stringMessages.nullUserGroup());
        AclDialogResources.INSTANCE.css().ensureInjected();

        suggestUserGroupUi = createUserGroupSuggest(userManagementService);
        addUserGroupButtonUi = new Button(stringMessages.add(), (ClickHandler) event -> onUserGroupAdd());
        removeUserGroupButtonUi = new Button(stringMessages.remove(), (ClickHandler) event -> onUserGroupRemove());
        removeUserGroupButtonUi.setEnabled(false);

        initWidget(uiBinder.createAndBindUi(this));
        final CellList<StrippedUserGroupDTO> userGroupList = createUserGroupCellList();
        userGroupDataProvider.addDataDisplay(userGroupList);
        userGroupCellListPanelUi.add(wrapIntoCaptionPanel(userGroupList, stringMessages.userGroups(),
                suggestUserGroupUi, addUserGroupButtonUi, removeUserGroupButtonUi));

        // retrieve set of available action names
        final List<String> actionNames = Stream.of(availableActions).map(Action::name).collect(Collectors.toList());

        // create action editor for allowed actions
        allowedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                IconResources.INSTANCE.removeIcon(), actionNames, stringMessages.allowedActionName());
        allowedActionsEditor.addValueChangeHandler(event -> userGroupsWithAllowedActions
                .put(userGroupSelectionModel.getSelectedObject(), toSet(event.getValue())));
        permissionsCellListPanelUi.add(allowedActionsContainer = createActionsContainer(stringMessages.allowedActions(),
                allowedActionsEditor, AclDialogResources.INSTANCE.css().allowedActionsTable()));

        // create action editor for denied actions
        deniedActionsEditor = new StringListEditorComposite(new ArrayList<>(), stringMessages,
                IconResources.INSTANCE.removeIcon(), actionNames, stringMessages.deniedActionName());
        deniedActionsEditor.addValueChangeHandler(e -> userGroupsWithDeniedActions
                .put(userGroupSelectionModel.getSelectedObject(), toDeniedActionSet(e.getValue())));
        permissionsCellListPanelUi.add(deniedActionsContainer = createActionsContainer(stringMessages.deniedActions(),
                deniedActionsEditor, AclDialogResources.INSTANCE.css().deniedActionsTable()));

        lblId.setText(id);
        lblType.setText(typeIdentifier);
    }

    private CaptionPanel createActionsContainer(final SafeHtml caption, final Widget content, final String styleName) {
        final CaptionPanel container = new CaptionPanel(caption);
        container.add(content);
        container.addStyleName(styleName);
        container.setVisible(false);
        return container;
    }

    /** @return UI element for selection of {@link UserGroupImpl} elements. */
    private SuggestBox createUserGroupSuggest(UserManagementServiceAsync userManagementService) {
        final MultiWordSuggestOracle userGroupOracle = new MultiWordSuggestOracle();
        userManagementService.getUserGroups(new AsyncCallback<Collection<UserGroupDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(Collection<UserGroupDTO> result) {
                final List<String> suggestionList = result.stream().map(UserGroupDTO::getName)
                        .collect(Collectors.toList());
                suggestionList.add(stringMessages.nullUserGroup());
                userGroupOracle.clear();
                userGroupOracle.addAll(suggestionList);
                userGroupOracle.setDefaultSuggestionsFromText(suggestionList);
            }
        });
        SuggestBox suggestBox = new SuggestBox(userGroupOracle, new TextBox());
        suggestBox.getElement().setPropertyString("placeholder", stringMessages.enterUserGroupName());
        suggestBox.addStyleName(AclDialogResources.INSTANCE.css().userGroupTextBox());
        return suggestBox;
    }

    /** @return the UI element for visualizing {@link UserGroupImpl} elements. */
    private CellList<StrippedUserGroupDTO> createUserGroupCellList() {
        final CellList<StrippedUserGroupDTO> userGroupCellList = new CellList<>(new AbstractCell<StrippedUserGroupDTO>() {
            @Override
            public void render(Context context, StrippedUserGroupDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.getName());
                }
            }
        });

        userGroupCellList.setSelectionModel(userGroupSelectionModel);
        userGroupSelectionModel
                .addSelectionChangeHandler(event -> updateActionEditors(userGroupSelectionModel.getSelectedObject()));
        return userGroupCellList;
    }

    /**
     * Updates the {@link #allowedActionsEditor} and {@link #deniedActionsEditor} when the selected UserGroup changed.
     */
    private void updateActionEditors(StrippedUserGroupDTO selectedUserGroup) {
        final boolean userGroupSelected = selectedUserGroup != null;
        removeUserGroupButtonUi.setEnabled(userGroupSelected);
        allowedActionsContainer.setVisible(userGroupSelected);
        deniedActionsContainer.setVisible(userGroupSelected);
        if (userGroupSelected) {
            allowedActionsEditor.setValue(userGroupsWithAllowedActions.get(selectedUserGroup), false);
            deniedActionsEditor.setValue(
                    removeExclamationMarkFromStrings(userGroupsWithDeniedActions.get(selectedUserGroup)), false);
        }
    }

    /**
     * Wraps the CellList together with a title and additional widgets (e.g. add/remove buttons) into a
     * {@link CaptionPanel}.
     */
    private CaptionPanel wrapIntoCaptionPanel(CellList<?> cellList, String title, Widget... additionalWidgets) {
        cellList.setPageSize(10);
        final SimplePager pager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        pager.setDisplay(cellList);
        final ScrollPanel tenantPanel = new ScrollPanel(cellList);
        final VerticalPanel tenantListWrapper = new VerticalPanel();
        tenantListWrapper.getElement().getStyle().setWidth(100, Unit.PCT);

        // add additional widgets
        final FlowPanel additionalWidgetsPanel = new FlowPanel();
        for (Widget additionalWidget : additionalWidgets) {
            additionalWidgetsPanel.add(additionalWidget);
            additionalWidget.addStyleName(AclDialogResources.INSTANCE.css().additionalWidget());
        }
        tenantListWrapper.add(additionalWidgetsPanel);
        tenantListWrapper.add(tenantPanel);
        tenantListWrapper.add(pager);

        final CaptionPanel tenantListCaption = new CaptionPanel(title);
        tenantListCaption.add(tenantListWrapper);
        return tenantListCaption;
    }

    /**
     * Updates the ACL edited in the EditAclDialog. Splits combinedActions retrieved by
     * {@link AccessControlList#getActionsByUserGroup()} into {@link #allowedActionsEditor} and
     * {@link #deniedActionsEditor}.
     */
    public void updateAcl(AccessControlListDTO acl) {
        final Map<StrippedUserGroupDTO, Set<String>> combinedActions = (acl != null)
                ? acl.getActionsByUserGroup() != null ? new HashMap<>(acl.getActionsByUserGroup()) : new HashMap<>()
                : new HashMap<>();

        for (Map.Entry<StrippedUserGroupDTO, Set<String>> combinedAction : combinedActions.entrySet()) {
            final Set<String> allowedActions = new HashSet<>();
            final Set<String> deniedActions = new HashSet<>();
            for (String action : combinedAction.getValue()) {
                if (action.startsWith("!")) {
                    deniedActions.add(action);
                } else {
                    allowedActions.add(action);
                }
            }
            if (combinedAction.getKey() == null) {
                userGroupsWithAllowedActions.put(nullUserGroup, allowedActions);
                userGroupsWithDeniedActions.put(nullUserGroup, deniedActions);
            }
            else {
            userGroupsWithAllowedActions.put(combinedAction.getKey(), allowedActions);
            userGroupsWithDeniedActions.put(combinedAction.getKey(), deniedActions);
            }
        }

        refreshUi();
    }

    /**
     * Updates the UI by refreshing the {@link #userGroupList} and selects an element to trigger the update of
     * {@link #allowedActionsEditor} and {@link #deniedActionsEditor}.
     */
    private void refreshUi() {
        this.userGroupDataProvider.getList().clear();
        final Set<StrippedUserGroupDTO> combinedKeySet = new HashSet<>();
        combinedKeySet.addAll(userGroupsWithAllowedActions.keySet());
        combinedKeySet.addAll(userGroupsWithDeniedActions.keySet());
        userGroupDataProvider.getList().addAll(combinedKeySet);
        Collections.sort(userGroupDataProvider.getList(), Comparator.comparing(Named::getName));

        // select an element
        if (userGroupDataProvider.getList().isEmpty()) {
            userGroupSelectionModel.clear();
        } else {
            userGroupSelectionModel.setSelected(userGroupDataProvider.getList().iterator().next(), true);
        }
    }

    /** Called when the user clicks on the 'Add' button */
    private void onUserGroupAdd() {
        final String userGroupName = suggestUserGroupUi.getValue();

        // get UserGroup object corresponding to user group name
        userManagementService.getStrippedUserGroupByName(userGroupName, new AsyncCallback<StrippedUserGroupDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(stringMessages.errorMessageUserGroupNameNotFound(userGroupName), ERROR);
            }

            @Override
            public void onSuccess(StrippedUserGroupDTO result) {
                if (result == null) {
                    result = nullUserGroup;
                }
                Notification.notify(stringMessages.successMessageAddedUserGroup(userGroupName), SUCCESS);
                userGroupsWithAllowedActions.put(result, new HashSet<>());
                userGroupsWithDeniedActions.put(result, new HashSet<>());
                refreshUi();
                suggestUserGroupUi.setText("");
                userGroupSelectionModel.setSelected(result, true);
            }
        });
    }

    /** Called when the user clicks on the 'Remove' button */
    private void onUserGroupRemove() {
        StrippedUserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
        if (selectedObject != null) {
            userGroupsWithAllowedActions.remove(selectedObject);
            userGroupsWithDeniedActions.remove(selectedObject);
            Notification.notify(stringMessages.successMessageRemovedUserGroup(selectedObject.getName()), SUCCESS);
            refreshUi();
        }
    }

    /** Merges {@link #userGroupsWithAllowedActions} and {@link #userGroupsWithDeniedActions}. */
    public Map<StrippedUserGroupDTO, Set<String>> getUserGroupsWithCombinedActions() {
        final Map<StrippedUserGroupDTO, Set<String>> combinedActions = new HashMap<>(userGroupsWithAllowedActions);
        for (Map.Entry<StrippedUserGroupDTO, Set<String>> actionEntry : userGroupsWithDeniedActions.entrySet()) {
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

    private Set<String> removeExclamationMarkFromStrings(Iterable<String> iter) {
        final Set<String> set = new HashSet<>();
        for (String s : iter) {
            set.add(s.startsWith("!") ? s.substring(1) : s);
        }
        return set;
    }

}
