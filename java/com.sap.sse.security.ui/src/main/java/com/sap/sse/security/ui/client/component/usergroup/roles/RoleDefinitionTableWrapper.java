package com.sap.sse.security.ui.client.component.usergroup.roles;

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
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * A wrapper for a CellTable displaying the role definitions associated with the selected user group. It shows the Role
 * Name and whether the role is enabled for all users. There is also an options to delete the group.
 */
public class RoleDefinitionTableWrapper extends
        TableWrapper<Pair<StrippedRoleDefinitionDTO, Boolean>, RefreshableSingleSelectionModel<Pair<StrippedRoleDefinitionDTO, Boolean>>, StringMessages, CellTableWithCheckboxResources> {

    private final LabeledAbstractFilterablePanel<Pair<StrippedRoleDefinitionDTO, Boolean>> filterField;
    private final SingleSelectionModel<UserGroupDTO> userGroupSelectionModel;

    static class SecuredDTOToPairAMapper<T extends SecuredDTO> {

    }

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

        final ImagesBarColumn<Pair<StrippedRoleDefinitionDTO, Boolean>, ImagesBarCell> actionColumn = new ImagesBarColumn<Pair<StrippedRoleDefinitionDTO, Boolean>, ImagesBarCell>(
                new RoleDefinitionImagesBarCell(stringMessages));
        actionColumn.setFieldUpdater((i, selectedRole, v) -> {
            UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
            if (selectedObject != null) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveRole(selectedRole.getA().getName()))) {
                    userService.getUserManagementService().removeRoleDefintionFromUserGroup(
                            selectedObject.getId().toString(), selectedRole.getA().getId().toString(),
                            new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotDeleteRole(selectedObject.getName()));
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    selectedObject.remove(selectedRole.getA());
                                    refresher.run();
                                }
                            });
                }
            } else {
                Window.alert(stringMessages.pleaseSelect());
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
            public void update(int index, Pair<StrippedRoleDefinitionDTO, Boolean> rolePair, Boolean value) {
                UserGroupDTO selectedObject = userGroupSelectionModel.getSelectedObject();
                if (selectedObject != null) {
                    userService.getUserManagementService().putRoleDefintionToUserGroup(
                            selectedObject.getId().toString(), rolePair.getA().getId().toString(), value,
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert(stringMessages.couldNotAddRoleToGroup(rolePair.getA().getName(),
                                            selectedObject.getName()));
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    selectedObject.put(rolePair.getA(), value);
                                    refreshRoleList();
                                }
                            });
                }
            }
        });
        table.addColumn(checkboxColumn, stringMessages.enableRoleForAllUsers());
        table.addColumn(actionColumn);
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
