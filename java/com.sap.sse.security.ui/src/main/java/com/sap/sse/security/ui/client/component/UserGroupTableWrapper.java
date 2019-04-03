package com.sap.sse.security.ui.client.component;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * A wrapper for a CellTable displaying an overview over the existing UserGroups. It shows the User-Group name, the
 * group and user each user group is owned by and options to delete the group, change the ownership or edit the
 * associated ACL.
 */
public class UserGroupTableWrapper extends
        TableWrapper<UserGroupDTO, RefreshableSingleSelectionModel<UserGroupDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<UserGroupDTO> filterField;
    private final UserService userService;

    public UserGroupTableWrapper(UserService userService, Iterable<HasPermissions> additionalPermissions,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean enablePager,
            CellTableWithCheckboxResources tableResources, Runnable refresher) {
        super(stringMessages, errorReporter, false, enablePager, new EntityIdentityComparator<UserGroupDTO>() {
            @Override
            public boolean representSameEntity(UserGroupDTO dto1, UserGroupDTO dto2) {
                return dto1.getId().toString().equals(dto2.getId().toString());
            }

            @Override
            public int hashCode(UserGroupDTO t) {
                return t.getId().hashCode();
            }
        }, tableResources);
        this.userService = userService;
        final ListHandler<UserGroupDTO> userColumnListHandler = getColumnSortHandler();

        // users table
        final TextColumn<UserGroupDTO> userGroupUUidColumn = new AbstractSortableTextColumn<UserGroupDTO>(
                group -> group.getId() == null ? "<null>" : group.getId().toString(), userColumnListHandler);

        final TextColumn<UserGroupDTO> UserGroupWithSecurityDTONameColumn = new AbstractSortableTextColumn<UserGroupDTO>(
                UserGroupDTO -> UserGroupDTO.getName(), userColumnListHandler);

        final HasPermissions type = SecuredSecurityTypes.USER_GROUP;
        final AccessControlledActionsColumn<UserGroupDTO, DefaultActionsImagesBarCell> actionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionColumn.addAction(ACTION_DELETE, DELETE, userGroupDTO -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(userGroupDTO.getName()))) {
                userService.getUserManagementService().deleteUserGroup(userGroupDTO.getId().toString(),
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                deletingUserGroupWithSecurityDTOFailed(userGroupDTO, caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (result.isSuccessful()) {
                                    filterField.remove(userGroupDTO);
                                    refresher.run();
                                } else {
                                    deletingUserGroupWithSecurityDTOFailed(userGroupDTO, result.getMessage());
                                }
                            }

                            private void deletingUserGroupWithSecurityDTOFailed(UserGroupDTO user, String message) {
                                errorReporter
                                        .reportError(stringMessages.errorDeletingUserGroup(user.getName(), message));
                            }
                        });
            }
        });

        final EditOwnershipDialog.DialogConfig<UserGroupDTO> configOwnership = EditOwnershipDialog.create(
                userService.getUserManagementService(), type,
                user -> refreshUserList(null), stringMessages);

        final EditACLDialog.DialogConfig<UserGroupDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, user -> user.getAccessControlList(),
                stringMessages);

        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP, DefaultActions.CHANGE_OWNERSHIP,
                configOwnership::openDialog);
        actionColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                u -> configACL.openDialog(u));

        filterField = new LabeledAbstractFilterablePanel<UserGroupDTO>(new Label(stringMessages.filterUserGroups()),
                new ArrayList<UserGroupDTO>(), dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(UserGroupDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                return string;
            }

            @Override
            public AbstractCellTable<UserGroupDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());

        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(userColumnListHandler);
        table.addColumn(userGroupUUidColumn, getStringMessages().id());
        table.addColumn(UserGroupWithSecurityDTONameColumn, getStringMessages().groupName());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, userColumnListHandler, stringMessages);
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("UserGroupWithSecurityDTOTable");
    }

    public LabeledAbstractFilterablePanel<UserGroupDTO> getFilterField() {
        return filterField;
    }

    /**
     * @param callback
     *            optional; may be {@code null}
     */
    public void refreshUserList(final AsyncCallback<Iterable<UserGroupDTO>> callback) {
        final AsyncCallback<Collection<UserGroupDTO>> myCallback = new AsyncCallback<Collection<UserGroupDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(
                        "Remote Procedure Call getUserGroupWithSecurityDTOs() - Failure: " + caught.getMessage());
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Collection<UserGroupDTO> result) {
                filterField.updateAll(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        userService.getUserManagementService().getUserGroups(myCallback);
    }
}
