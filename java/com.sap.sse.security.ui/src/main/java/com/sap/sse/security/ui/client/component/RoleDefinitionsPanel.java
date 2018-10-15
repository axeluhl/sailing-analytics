package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Displays and allows users to edit {@link Role}s. This includes creating and removing
 * roles as well as changing the sets of permissions implied by a role.<p>
 * 
 * The panel is <em>not</em> a standalone top-level AdminConsole panel but just a widget
 * that can be included, e.g., in a user management panel.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RoleDefinitionsPanel extends VerticalPanel {
    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;
    private final FlushableCellTable<RoleDefinition> roleDefinitionsTable;
    private final ErrorReporter errorReporter;
    private final UserManagementServiceAsync userManagementService;
    private final ListDataProvider<RoleDefinition> rolesListDataProvider;
    private final StringMessages stringMessages;
    private final LabeledAbstractFilterablePanel<RoleDefinition> filterablePanelRoleDefinitions;
    private RefreshableMultiSelectionModel<? super RoleDefinition> refreshableRoleDefinitionMultiSelectionModel;
    
    public RoleDefinitionsPanel(StringMessages stringMessages, UserManagementServiceAsync userManagementService, CellTableWithCheckboxResources tableResources,
            ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        this.addButton = new Button(stringMessages.add());
        this.removeButton = new Button(stringMessages.remove());
        this.refreshButton = new Button(stringMessages.refresh());
        rolesListDataProvider = new ListDataProvider<RoleDefinition>();
        filterablePanelRoleDefinitions = new LabeledAbstractFilterablePanel<RoleDefinition>(new Label(stringMessages.filterRoles()), new ArrayList<>(),
                rolesListDataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(RoleDefinition roleDefinition) {
                return Arrays.asList(roleDefinition.getName(), roleDefinition.getId().toString(), roleDefinition.getPermissions().toString());
            }
            
            @Override
            public AbstractCellTable<RoleDefinition> getCellTable() {
                return roleDefinitionsTable;
            }
        };
        roleDefinitionsTable = createRoleDefinitionsTable(tableResources);
        roleDefinitionsTable.ensureDebugId("RolesCellTable");
        filterablePanelRoleDefinitions.getTextBox().ensureDebugId("RolesFilterTextBox");
        addButton.addClickHandler(e->createRoleDefinition());
        refreshableRoleDefinitionMultiSelectionModel = (RefreshableMultiSelectionModel<? super RoleDefinition>) roleDefinitionsTable.getSelectionModel();
        removeButton.addClickHandler(e->{
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(String.join(", ", Util.map(getSelectedRoleDefinitions(), r->r.getName()))))) {
                final Set<RoleDefinition> selectedRoles = new HashSet<>(getSelectedRoleDefinitions());
                filterablePanelRoleDefinitions.removeAll(selectedRoles);
            }
        });
        refreshButton.addClickHandler(e->updateRoleDefinitions());
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel);
        add(filterablePanelRoleDefinitions);
        add(roleDefinitionsTable);
        updateRoleDefinitions();
    }
    
    private void createRoleDefinition() {
        new RoleDefinitionCreationDialog(stringMessages, getAllPermissions(), getAllRoleDefinitions(), new DialogCallback<RoleDefinition>() {
            @Override
            public void ok(RoleDefinition editedObject) {
                userManagementService.createRoleDefinition(editedObject.getId().toString(), editedObject.getName(), new AsyncCallback<RoleDefinition>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorCreatingRole(editedObject.getName(), caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(RoleDefinition result) {
                        userManagementService.updateRoleDefinition(editedObject, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorEditingRoles(editedObject.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                updateRoleDefinitions();
                            }
                        });
                    }
                });
            }

            @Override
            public void cancel() {
                //no-op
            }
        }).show();
    }

    private Set<RoleDefinition> getSelectedRoleDefinitions() {
        @SuppressWarnings("unchecked")
        final Set<RoleDefinition> result = (Set<RoleDefinition>) refreshableRoleDefinitionMultiSelectionModel.getSelectedSet();
        return result;
    }
    
    private FlushableCellTable<RoleDefinition> createRoleDefinitionsTable(CellTableWithCheckboxResources tableResources) {
        final FlushableCellTable<RoleDefinition> table = new FlushableCellTable<>(/* pageSize */ 50, tableResources);
        rolesListDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        SelectionCheckboxColumn<RoleDefinition> roleSelectionCheckboxColumn = new SelectionCheckboxColumn<RoleDefinition>(
                tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell(), new EntityIdentityComparator<RoleDefinition>() {
                    @Override
                    public boolean representSameEntity(RoleDefinition roleDefinition1, RoleDefinition roleDefinition2) {
                        return roleDefinition1.getId().equals(roleDefinition2.getId());
                    }
                    @Override
                    public int hashCode(RoleDefinition t) {
                        return t.getId().hashCode();
                    }
                }, filterablePanelRoleDefinitions.getAllListDataProvider(), table);
        ListHandler<RoleDefinition> columnSortHandler = new ListHandler<>(rolesListDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);
        columnSortHandler.setComparator(roleSelectionCheckboxColumn, roleSelectionCheckboxColumn.getComparator());
        TextColumn<RoleDefinition> roleDefinitionNameColumn = new AbstractSortableTextColumn<RoleDefinition>(role->role.getName(), columnSortHandler);
        Column<RoleDefinition, SafeHtml> permissionsColumn = new Column<RoleDefinition, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(RoleDefinition role) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                for (Iterator<WildcardPermission> permissionIter=role.getPermissions().iterator(); permissionIter.hasNext(); ) {
                    final WildcardPermission permission = permissionIter.next();
                    builder.appendEscaped(permission.toString());
                    if (permissionIter.hasNext()) {
                        builder.appendHtmlConstant("<br>");
                    }
                }
                return builder.toSafeHtml();
            }
        };
        permissionsColumn.setSortable(true);
        columnSortHandler.setComparator(permissionsColumn, new Comparator<RoleDefinition>() {
            @Override
            public int compare(RoleDefinition r1, RoleDefinition r2) {
                return new NaturalComparator().compare(r1.getPermissions().toString(), r2.getPermissions().toString());
            }
        });
        ImagesBarColumn<RoleDefinition, EditAndRemoveImagesBarCell> regattaActionColumn = new ImagesBarColumn<RoleDefinition, EditAndRemoveImagesBarCell>(
                new EditAndRemoveImagesBarCell(stringMessages));
        regattaActionColumn.setFieldUpdater(new FieldUpdater<RoleDefinition, String>() {
            @Override
            public void update(int index, RoleDefinition roleDefinition, String value) {
                if (EditAndRemoveImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRole(roleDefinition);
                } else if (EditAndRemoveImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(roleDefinition.getName()))) {
                        removeRole(roleDefinition);
                    }
                }
            }
        });
        table.addColumn(roleSelectionCheckboxColumn, roleSelectionCheckboxColumn.getHeader());
        table.addColumn(roleDefinitionNameColumn, stringMessages.name());
        table.addColumn(permissionsColumn, stringMessages.permissions());
        table.addColumn(regattaActionColumn, stringMessages.actions());
        table.setSelectionModel(roleSelectionCheckboxColumn.getSelectionModel(), roleSelectionCheckboxColumn.getSelectionManager());
        return table;
    }

    private void removeRole(RoleDefinition roleDefinition) {
        userManagementService.deleteRoleDefinition(roleDefinition.getId().toString(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToDeleteRole(roleDefinition.getName(), caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                updateRoleDefinitions();
            }
        });
    }

    private void editRole(RoleDefinition role) {
        final Set<RoleDefinition> allOtherRoles = getAllOtherRoles(role);
        Set<WildcardPermission> allPermissionsAsStrings = getAllPermissions();
        new RoleDefinitionEditDialog(role, stringMessages, allPermissionsAsStrings, allOtherRoles, new DialogCallback<RoleDefinition>() {
                    @Override
                    public void ok(RoleDefinition editedObject) {
                        userManagementService.updateRoleDefinition(editedObject, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorEditingRoles(editedObject.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                updateRoleDefinitions();
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();
    }

    private Set<WildcardPermission> getAllPermissions() {
        Set<WildcardPermission> allPermissionsAsStrings = new HashSet<>();
        for (final RoleDefinition roleFromAllRoles : getAllRoleDefinitions()) {
            Util.addAll(roleFromAllRoles.getPermissions(), allPermissionsAsStrings);
        }
        return allPermissionsAsStrings;
    }

    private Set<RoleDefinition> getAllOtherRoles(RoleDefinition role) {
        final Set<RoleDefinition> allOtherRoles = getAllRoleDefinitions();
        allOtherRoles.remove(role);
        return allOtherRoles;
    }

    private Set<RoleDefinition> getAllRoleDefinitions() {
        final Set<RoleDefinition> allOtherRoles = new HashSet<>();
        Util.addAll(filterablePanelRoleDefinitions.getAll(), allOtherRoles);
        return allOtherRoles;
    }

    public void updateRoleDefinitions() {
        userManagementService.getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinition>>() {
            @Override
            public void onSuccess(ArrayList<RoleDefinition> allRoles) {
                filterablePanelRoleDefinitions.updateAll(allRoles);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToLoadRoles(caught.getMessage()));
            }
        });
    }
}
