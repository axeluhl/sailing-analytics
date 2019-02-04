package com.sap.sse.security.ui.client.component;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * A wrapper for a CellTable displaying an overview over the existing UserGroups. It shows the User-Group name, the
 * group and user each user group is owned by and options to delete the group, change the ownership or edit the
 * associated ACL.
 */
public class RoleDefinitionTableWrapper extends
        TableWrapper<Pair<StrippedRoleDefinitionDTO, Boolean>, RefreshableSingleSelectionModel<Pair<StrippedRoleDefinitionDTO, Boolean>>, StringMessages, CellTableWithCheckboxResources> {

    private final LabeledAbstractFilterablePanel<Pair<StrippedRoleDefinitionDTO, Boolean>> filterField;
    private final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel;

    public RoleDefinitionTableWrapper(UserService userService, Iterable<HasPermissions> additionalPermissions,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean enablePager,
            CellTableWithCheckboxResources tableResources, Runnable refresher,
            SingleSelectionModel<UserGroupDTO> userGroupSelectionModel) {
        super(stringMessages, errorReporter, false, enablePager,
                new EntityIdentityComparator<Pair<StrippedRoleDefinitionDTO, Boolean>>() {
                    @Override
                    public boolean representSameEntity(Pair<StrippedRoleDefinitionDTO, Boolean> dto1,
                            Pair<StrippedRoleDefinitionDTO, Boolean> dto2) {
                        return dto1.getA().getId().toString().equals(dto2.getA().getId().toString());
                    }

                    @Override
                    public int hashCode(Pair<StrippedRoleDefinitionDTO, Boolean> t) {
                        return t.getA().getId().hashCode();
                    }
                }, tableResources);
        this.userGroupSelectionModel = userGroupSelectionModel;
        this.userGroupSelectionModel.addSelectionChangeHandler(e -> refreshRoleList());

        final ListHandler<Pair<StrippedRoleDefinitionDTO, Boolean>> userColumnListHandler = getColumnSortHandler();

        // users table
        final TextColumn<Pair<StrippedRoleDefinitionDTO, Boolean>> userGroupWithSecurityDTONameColumn = new AbstractSortableTextColumn<Pair<StrippedRoleDefinitionDTO, Boolean>>(
                dto -> dto.getA().getName(), userColumnListHandler);

        final AccessControlledActionsColumn<RoleDefinitionDTO, DefaultActionsImagesBarCell> actionColumn = new AccessControlledActionsColumn<>(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionColumn.addAction(ACTION_DELETE, DELETE, role -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(role.getName()))) {
                userService.getUserManagementService().removeRoleDefintionFromUserGroup(
                        userGroupSelectionModel.getSelectedObject().getId().toString(), role.getId().toString(),
                        new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void onSuccess(Void result) {
                                refresher.run();
                            }
                        });
            }
        });
        filterField = new LabeledAbstractFilterablePanel<Pair<StrippedRoleDefinitionDTO, Boolean>>(
                new Label(stringMessages.filterRoles()), new ArrayList<Pair<StrippedRoleDefinitionDTO, Boolean>>(),
                dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(Pair<StrippedRoleDefinitionDTO, Boolean> t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getA().getName());
                return string;
            }

            @Override
            public AbstractCellTable<Pair<StrippedRoleDefinitionDTO, Boolean>> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(userColumnListHandler);
        table.addColumn(userGroupWithSecurityDTONameColumn, stringMessages.roleName());

        Column<Pair<StrippedRoleDefinitionDTO, Boolean>, Boolean> checkboxColumn = new Column<Pair<StrippedRoleDefinitionDTO, Boolean>, Boolean>(
                new CheckboxCell()) {
            @Override
            public Boolean getValue(Pair<StrippedRoleDefinitionDTO, Boolean> object) {
                return object.getB();
            }
        };
        checkboxColumn.setFieldUpdater(new FieldUpdater<Pair<StrippedRoleDefinitionDTO, Boolean>, Boolean>() {
            @Override
            public void update(int index, Pair<StrippedRoleDefinitionDTO, Boolean> object, Boolean value) {
                UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
                if (selectedObject != null) {
                    userService.getUserManagementService().putRoleDefintionToUserGroup(
                            selectedObject.getId().toString(), object.getA().getId().toString(), value,
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    selectedObject.put(object.getA(), value);
                                    refreshRoleList();
                                    // TODO: refresh
                                }
                            });
                }
            }
        });
        table.addColumn(checkboxColumn, stringMessages.enableRoleForAllUsers());
        // TODO: add remove X in action column
        table.ensureDebugId("RoleDefinitionDTOTable");
    }

    public LabeledAbstractFilterablePanel<Pair<StrippedRoleDefinitionDTO, Boolean>> getFilterField() {
        return filterField;
    }

    public void refreshRoleList() {
        UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
        if (selectedObject != null) {
            filterField.updateAll(selectedObject.getRoleDefinitions());
        }
    }
}
