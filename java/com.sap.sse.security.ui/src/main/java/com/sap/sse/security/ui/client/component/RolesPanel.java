package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
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
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableSortedCellTableWithStylableHeaders;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.SecurityTableResources;
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
public class RolesPanel extends VerticalPanel {
    private final Button addButton;
    private final Button removeButton;
    private final Button refreshButton;
    private final FlushableSortedCellTableWithStylableHeaders<Role> rolesTable;
    private final ErrorReporter errorReporter;
    private final UserManagementServiceAsync userManagementService;
    private final ListDataProvider<Role> rolesListDataProvider;
    private final List<Role> allRoles;
    private final StringMessages stringMessages;
    private final LabeledAbstractFilterablePanel<Role> filterablePanelRoles;
    private RefreshableMultiSelectionModel<? super Role> refreshableRoleMultiSelectionModel;
    
    public RolesPanel(StringMessages stringMessages, UserManagementServiceAsync userManagementService, SecurityTableResources tableResources,
            ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.userManagementService = userManagementService;
        this.addButton = new Button(stringMessages.add());
        this.removeButton = new Button(stringMessages.remove());
        this.refreshButton = new Button(stringMessages.refresh());
        this.allRoles = new ArrayList<>();
        rolesListDataProvider = new ListDataProvider<Role>();
        filterablePanelRoles = new LabeledAbstractFilterablePanel<Role>(new Label(stringMessages.filterRoles()), allRoles,
                new CellTable<Role>(), rolesListDataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(Role role) {
                return Arrays.asList(role.getName(), role.getId().toString(), role.getPermissions().toString());
            }
        };
        filterablePanelRoles.getTextBox().ensureDebugId("RolesFilterTextBox");
        rolesTable = createRolesTable(tableResources);
        rolesTable.ensureDebugId("RolesCellTable");
        filterablePanelRoles.setTable(rolesTable);
        addButton.addClickHandler(e->createRole());
        refreshableRoleMultiSelectionModel = (RefreshableMultiSelectionModel<? super Role>) rolesTable.getSelectionModel();
        removeButton.addClickHandler(e->{
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(String.join(", ", Util.map(getSelectedRoles(), r->r.getName()))))) {
                final Set<Role> selectedRoles = new HashSet<Role>(getSelectedRoles());
                filterablePanelRoles.removeAll(selectedRoles);
            }
        });
        refreshButton.addClickHandler(e->updateRoles());
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel);
        add(filterablePanelRoles);
        add(rolesTable);
        updateRoles();
    }
    
    private void createRole() {
        new RoleCreationDialog(stringMessages, getAllPermissions(), allRoles, new DialogCallback<Role>() {
            @Override
            public void ok(Role editedObject) {
                userManagementService.createRole(editedObject.getId().toString(), editedObject.getName(), new AsyncCallback<Role>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorCreatingRole(editedObject.getName(), caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Role result) {
                        // no-op
                    }
                });
            }

            @Override
            public void cancel() {
                // TODO Auto-generated method stub
                
            }
        }).show();
    }

    private Set<Role> getSelectedRoles() {
        @SuppressWarnings("unchecked")
        final Set<Role> result = (Set<Role>) refreshableRoleMultiSelectionModel.getSelectedSet();
        return result;
    }
    
    private FlushableSortedCellTableWithStylableHeaders<Role> createRolesTable(SecurityTableResources tableResources) {
        final FlushableSortedCellTableWithStylableHeaders<Role> table = new FlushableSortedCellTableWithStylableHeaders<>(/* pageSize */ 50, tableResources);
        rolesListDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        SelectionCheckboxColumn<Role> roleSelectionCheckboxColumn = new SelectionCheckboxColumn<Role>(
                tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell(), new EntityIdentityComparator<Role>() {
                    @Override
                    public boolean representSameEntity(Role role1, Role role2) {
                        return role1.getId().equals(role2.getId());
                    }
                    @Override
                    public int hashCode(Role t) {
                        return t.getId().hashCode();
                    }
                }, filterablePanelRoles.getAllListDataProvider(), table);
        ListHandler<Role> columnSortHandler = new ListHandler<>(rolesListDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);
        columnSortHandler.setComparator(roleSelectionCheckboxColumn, roleSelectionCheckboxColumn.getComparator());

        TextColumn<Role> roleNameColumn = new TextColumn<Role>() {
            @Override
            public String getValue(Role role) {
                return role.getName();
            }
        };
        roleNameColumn.setSortable(true);
        columnSortHandler.setComparator(roleNameColumn, new Comparator<Role>() {
            @Override
            public int compare(Role r1, Role r2) {
                return new NaturalComparator().compare(r1.getName(), r2.getName());
            }
        });

        Column<Role, SafeHtml> permissionsColumn = new Column<Role, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Role role) {
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
        columnSortHandler.setComparator(permissionsColumn, new Comparator<Role>() {
            @Override
            public int compare(Role r1, Role r2) {
                return new NaturalComparator().compare(r1.getPermissions().toString(), r2.getPermissions().toString());
            }
        });
        ImagesBarColumn<Role, RoleImagesBarCell> regattaActionColumn = new ImagesBarColumn<Role, RoleImagesBarCell>(
                new RoleImagesBarCell(stringMessages));
        regattaActionColumn.setFieldUpdater(new FieldUpdater<Role, String>() {
            @Override
            public void update(int index, Role role, String value) {
                if (RoleImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRole(role);
                } else if (RoleImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(role.getName()))) {
                        removeRole(role);
                    }
                }
            }
        });

        table.addColumn(roleSelectionCheckboxColumn, roleSelectionCheckboxColumn.getHeader());
        table.addColumn(roleNameColumn, stringMessages.name());
        table.addColumn(permissionsColumn, stringMessages.permissions());
        table.addColumn(regattaActionColumn, stringMessages.actions());
        table.setSelectionModel(roleSelectionCheckboxColumn.getSelectionModel(), roleSelectionCheckboxColumn.getSelectionManager());
        return table;
    }

    private void removeRole(Role role) {
        userManagementService.deleteRole(role.getId().toString(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToDeleteRole(role.getName(), caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                updateRoles();
            }
        });
    }

    private void editRole(Role role) {
        final Set<Role> allOtherRoles = getAllOtherRoles(role);
        Set<WildcardPermission> allPermissionsAsStrings = getAllPermissions();
        new RoleEditDialog(role, stringMessages, allPermissionsAsStrings, allOtherRoles, new DialogCallback<Role>() {
                    @Override
                    public void ok(Role editedObject) {
                        userManagementService.updateRole(editedObject, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorUpdatingRoles(editedObject.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                // no-op
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
        for (final Role roleFromAllRoles : allRoles) {
            Util.addAll(roleFromAllRoles.getPermissions(), allPermissionsAsStrings);
        }
        return allPermissionsAsStrings;
    }

    private Set<Role> getAllOtherRoles(Role role) {
        final Set<Role> allOtherRoles = new HashSet<>(allRoles);
        allOtherRoles.remove(role);
        return allOtherRoles;
    }

    public void updateRoles() {
        userManagementService.getRoles(new AsyncCallback<ArrayList<Role>>() {
            @Override
            public void onSuccess(ArrayList<Role> allRoles) {
                filterablePanelRoles.updateAll(allRoles);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToLoadRoles(caught.getMessage()));
            }
        });
    }
}
