package com.sap.sse.security.ui.client.component;

import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.gwt.core.client.Callback;
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
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * @param <S>
 */
public class UserGroupTableWrapper extends
        TableWrapper<UserGroupDTO, RefreshableSingleSelectionModel<UserGroupDTO>, StringMessages, CellTableWithCheckboxResources> {
    private final LabeledAbstractFilterablePanel<UserGroupDTO> filterField;
    private final UserService userService;

    public UserGroupTableWrapper(UserService userService, Iterable<HasPermissions> additionalPermissions,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean enablePager,
            CellTableWithCheckboxResources tableResources) {
        super(stringMessages, errorReporter, false, enablePager,
                new EntityIdentityComparator<UserGroupDTO>() {
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
        ListHandler<UserGroupDTO> userColumnListHandler = getColumnSortHandler();

        // users table
        TextColumn<UserGroupDTO> UserGroupWithSecurityDTONameColumn = new AbstractSortableTextColumn<UserGroupDTO>(
                UserGroupDTO -> UserGroupDTO.getName(), userColumnListHandler);

        final HasPermissions type = SecuredSecurityTypes.USER_GROUP;
        final Function<UserGroupDTO, String> idFactory = UserGroupDTO::getName;
        final AccessControlledActionsColumn<UserGroupDTO, DefaultActionsImagesBarCell> actionColumn = new AccessControlledActionsColumn<>(
                new DefaultActionsImagesBarCell(stringMessages), userService, type, idFactory);
        actionColumn.addAction(ACTION_DELETE, DELETE, userGroupDTO -> {
            if (Window.confirm(stringMessages.doYouReallyWantToRemoveUserGroup(userGroupDTO.getName()))) {
                getUserManagementService().deleteUserGroup(userGroupDTO.getId().toString(),
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                deletingUserGroupWithSecurityDTOFailed(userGroupDTO, caught.getMessage());
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (result.isSuccessful()) {
                                    filterField.remove(userGroupDTO);
                                } else {
                                    deletingUserGroupWithSecurityDTOFailed(userGroupDTO,
                                            result.getMessage());
                                }
                            }

                            private void deletingUserGroupWithSecurityDTOFailed(UserGroupDTO user,
                                    String message) {
                                // TODO: v i18n v
                                errorReporter.reportError(stringMessages.errorDeletingUser(user.getName(), message));
                            }
                        });
            }
        });

        final EditOwnershipDialog.DialogConfig<UserGroupDTO> configOwnership = EditOwnershipDialog.create(
                userService.getUserManagementService(), type, idFactory,
                user -> refreshUserList((Callback<Iterable<UserGroupDTO>, Throwable>) null),
                stringMessages);

        final EditACLDialog.DialogConfig<UserGroupDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, idFactory, user -> user.getAccessControlList(),
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
        table.addColumn(UserGroupWithSecurityDTONameColumn, getStringMessages().groupName());
        table.addColumn(actionColumn, stringMessages.actions());
        table.ensureDebugId("UserGroupWithSecurityDTOTable");
    }

    public Iterable<UserGroupDTO> getAllUsers() {
        return filterField.getAll();
    }

    public LabeledAbstractFilterablePanel<UserGroupDTO> getFilterField() {
        return filterField;
    }

    public void refreshUserGroups(Iterable<UserGroupDTO> UserGroupWithSecurityDTOs) {
        getFilteredUserGroups(UserGroupWithSecurityDTOs);
    }

    /**
     * @param callback
     *            optional; may be {@code null}
     */
    public void refreshUserList(final Callback<Iterable<UserGroupDTO>, Throwable> callback) {
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
                getFilteredUserGroups(result);
                refreshUserGroups(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        getUserManagementService().getUserGroups(myCallback);
    }

    private void getFilteredUserGroups(Iterable<UserGroupDTO> result) {
        filterField.updateAll(result);
    }

    private UserManagementServiceAsync getUserManagementService() {
        return userService.getUserManagementService();
    }
}
